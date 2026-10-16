package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.DTO.CreateForeClosureRequest;
import com.example.Loan_Project.DTO.ReviewForeClosureRequest;
import com.example.Loan_Project.Entity.*;
import com.example.Loan_Project.Exception.ResourceNotFoundException;
import com.example.Loan_Project.Repository.EmiScheduleRepository;
import com.example.Loan_Project.Repository.ForeClosureRepository;
import com.example.Loan_Project.Repository.LoanAccountRepository;
import com.example.Loan_Project.Repository.PenaltyChargeRepository;
import com.example.Loan_Project.Repository.InterestAccrualRepository;
import com.example.Loan_Project.Service.ForeClosureService;
import com.example.Loan_Project.Service.NotificationService;
import com.example.Loan_Project.Service.EmailService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class ForeClosureServiceImpl implements ForeClosureService {

    private final ForeClosureRepository foreClosureRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final EmiScheduleRepository emiScheduleRepository;
    private final PenaltyChargeRepository penaltyChargeRepository;
    private final InterestAccrualRepository interestAccrualRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    // 1. CREATE FORECLOSURE REQUEST


    @Override
    public ForeClosureRequest createRequest(
            Integer customerId,
            CreateForeClosureRequest input) {

        if (customerId == null || input == null
                || input.getLoanAccountId() == null) {
            throw new IllegalArgumentException(
                    "Customer ID and loan account ID are required");
        }

        if (input.getForeClosureType() == null) {
            throw new IllegalArgumentException(
                    "Foreclosure type is required");
        }

        Integer loanId = input.getLoanAccountId();

        LoanAccount loan = loanAccountRepository.findById(loanId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan account not found"));

        validateOwnership(loan, customerId);
        validateActiveLoan(loan);

        BigDecimal outstanding = loan.getOutstandingPrincipal();

        if (outstanding == null || outstanding.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Outstanding principal must be positive");
        }

        boolean requestExists =
                foreClosureRepository
                        .existsByLoanAccount_LoanAccountIdAndStatusIn(
                                loan.getLoanAccountId(),
                                List.of(
                                        ForeClosureStatus.PENDING,
                                        ForeClosureStatus.APPROVED
                                ));

        if (requestExists) {
            throw new IllegalArgumentException(
                    "An unfinished foreclosure request already exists");
        }

        BigDecimal principalSettlement;

        if (input.getForeClosureType() == ForeClosureType.FULL) {

            principalSettlement = money(outstanding);

        } else if (input.getForeClosureType()
                == ForeClosureType.PARTIAL) {

            Integer months = input.getNumberOfMonths();

            if (months == null || months < 1 || months > 600) {
                throw new IllegalArgumentException(
                        "Select a valid number of months");
            }

            BigDecimal emi = loan.getEmiAmount();

            if (emi == null || emi.signum() <= 0) {
                throw new IllegalArgumentException(
                        "Valid EMI is not available for this loan");
            }

            BigDecimal partialAmount = money(
                    emi.multiply(BigDecimal.valueOf(months)));

            if (partialAmount.compareTo(outstanding) >= 0) {
                throw new IllegalArgumentException(
                        "Partial prepayment must be less than outstanding principal");
            }

            principalSettlement = partialAmount;

        } else {
            throw new IllegalArgumentException(
                    "Unsupported foreclosure type");
        }

        ForeClosureRequest request = new ForeClosureRequest();

        request.setLoanAccount(loan);
        request.setForeClosureType(input.getForeClosureType());

        request.setPartialAmount(
                input.getForeClosureType() == ForeClosureType.PARTIAL
                        ? principalSettlement
                        : null
        );

        request.setExpectedClosureDate(input.getExpectedClosureDate());
        request.setReason(input.getReason());
        request.setStatus(ForeClosureStatus.PENDING);
        request.setIsPaid(false);

        applyBreakdown(request, loan, principalSettlement);
        request.setForeClosureAmount(request.getTotalPayable());

        ForeClosureRequest savedRequest =
                foreClosureRepository.save(request);

        notifyForeclosureCustomer(
                loan,
                "Foreclosure request submitted",
                "Your " + savedRequest.getForeClosureType()
                        + " foreclosure request (ID: "
                        + savedRequest.getRequestId()
                        + ") has been submitted successfully. "
                        + "Current status: PENDING."
        );

        return savedRequest;
    }


    // 2. GET CUSTOMER REQUESTS

    @Override
    @Transactional(readOnly = true)
    public List<ForeClosureRequest> getCustomerRequests(
            Integer customerId) {

        return foreClosureRepository
                .findByLoanAccount_Customer_CustomerIdOrderByRequestedDateDesc(
                        customerId);
    }

    // 3. GET PENDING REQUESTS

    @Override
    @Transactional(readOnly = true)
    public List<ForeClosureRequest> getPendingRequests() {

        return foreClosureRepository
                .findByStatusOrderByRequestedDateAsc(
                        ForeClosureStatus.PENDING);
    }

    // 4. GET ALL REQUESTS

    @Override
    @Transactional(readOnly = true)
    public List<ForeClosureRequest> getAllRequests() {

        return foreClosureRepository
                .findAllByOrderByRequestedDateDesc();
    }

    // 5. GET REQUEST BY ID

    @Override
    @Transactional(readOnly = true)
    public ForeClosureRequest getRequest(Integer requestId) {

        return foreClosureRepository.findById(requestId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Foreclosure request not found"));
    }

    // 6. ADMIN REVIEWS REQUEST

    @Override
    public ForeClosureRequest reviewRequest(
            Integer requestId,
            ReviewForeClosureRequest input) {

        if (input == null || input.getApproved() == null) {
            throw new IllegalArgumentException(
                    "Approval decision is required");
        }

        ForeClosureRequest request = getRequest(requestId);

        if (request.getStatus() != ForeClosureStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only pending requests can be reviewed");
        }

        LoanAccount loan = request.getLoanAccount();

        if (loan == null) {
            throw new ResourceNotFoundException(
                    "Loan account not found for request");
        }

        validateActiveLoan(loan);

        BigDecimal settlementPrincipal;

        if (request.getForeClosureType() == ForeClosureType.FULL) {

            settlementPrincipal = loan.getOutstandingPrincipal();

        } else if (request.getForeClosureType()
                == ForeClosureType.PARTIAL) {

            settlementPrincipal = request.getPartialAmount();

            if (settlementPrincipal == null
                    || settlementPrincipal.signum() <= 0
                    || settlementPrincipal.compareTo(
                    loan.getOutstandingPrincipal()) >= 0) {
                throw new IllegalArgumentException(
                        "Invalid partial foreclosure amount");
            }

        } else {
            throw new IllegalArgumentException(
                    "Unsupported foreclosure type");
        }

        applyBreakdown(request, loan, settlementPrincipal);
        request.setForeClosureAmount(request.getTotalPayable());
        request.setApprovedDate(LocalDateTime.now());

        request.setStatus(Boolean.TRUE.equals(input.getApproved())
                ? ForeClosureStatus.APPROVED
                : ForeClosureStatus.REJECTED);

        ForeClosureRequest savedRequest =
                foreClosureRepository.save(request);

        if (savedRequest.getStatus() == ForeClosureStatus.APPROVED) {

            notifyForeclosureCustomer(
                    loan,
                    "Foreclosure request approved",
                    "Your foreclosure request (ID: "
                            + savedRequest.getRequestId()
                            + ") has been APPROVED. Total payable: "
                            + savedRequest.getTotalPayable()
                            + ". Please proceed with payment.");

        } else if (savedRequest.getStatus()
                == ForeClosureStatus.REJECTED) {

            notifyForeclosureCustomer(
                    loan,
                    "Foreclosure request rejected",
                    "Your foreclosure request (ID: "
                            + savedRequest.getRequestId()
                            + ") has been REJECTED.");
        }

        return savedRequest;
    }

    // 7. PAYMENT BREAKDOWN

    private void applyBreakdown(
            ForeClosureRequest request,
            LoanAccount loan,
            BigDecimal principalSettlement) {

        if (principalSettlement == null
                || principalSettlement.signum() < 0) {
            throw new IllegalArgumentException(
                    "Invalid principal settlement");
        }

        BigDecimal interest = interestAccrualRepository
                .findByLoanAccount_LoanAccountId(
                        loan.getLoanAccountId())
                .stream()
                .filter(a -> a.getInterestAmount() != null)
                .filter(a -> !isSettled(a.getStatus()))
                .map(InterestAccrual::getInterestAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal penalty = BigDecimal.ZERO;
        BigDecimal bounce = BigDecimal.ZERO;
        BigDecimal other = BigDecimal.ZERO;

        List<PenaltyCharge> charges =
                penaltyChargeRepository
                        .findByLoanAccount_LoanAccountId(
                                loan.getLoanAccountId());

        for (PenaltyCharge charge : charges) {

            if (charge.getPenaltyAmount() == null
                    || isSettled(charge.getStatus())) {
                continue;
            }

            String reason = charge.getReason() == null
                    ? ""
                    : charge.getReason().toLowerCase(Locale.ROOT);

            String type = charge.getChargeType() == null
                    ? ""
                    : charge.getChargeType().toUpperCase(Locale.ROOT);

            if (type.equals("BOUNCE")
                    || reason.contains("bounce")
                    || reason.contains("dishonour")
                    || reason.contains("dishonor")) {

                bounce = bounce.add(charge.getPenaltyAmount());

            } else if (type.equals("OTHER")
                    || reason.contains("legal")
                    || reason.contains("processing")
                    || reason.contains("other")) {

                other = other.add(charge.getPenaltyAmount());

            } else {
                penalty = penalty.add(charge.getPenaltyAmount());
            }
        }

        BigDecimal principal = money(principalSettlement);
        BigDecimal accruedInterest = money(interest);
        BigDecimal penaltyAmount = money(penalty);
        BigDecimal bounceAmount = money(bounce);
        BigDecimal otherAmount = money(other);

        BigDecimal total = principal
                .add(accruedInterest)
                .add(penaltyAmount)
                .add(bounceAmount)
                .add(otherAmount);

        request.setPrincipalAmount(principal);
        request.setAccruedInterest(accruedInterest);
        request.setPenaltyAmount(penaltyAmount);
        request.setBounceCharges(bounceAmount);
        request.setOtherCharges(otherAmount);
        request.setTotalPayable(money(total));
    }

    private boolean isSettled(String status) {

        if (status == null) {
            return false;
        }

        return List.of(
                "PAID", "SETTLED", "WAIVED", "CANCELLED"
        ).contains(status.trim().toUpperCase(Locale.ROOT));
    }

    private BigDecimal money(BigDecimal amount) {

        return (amount == null ? BigDecimal.ZERO : amount)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void validateOwnership(
            LoanAccount loan,
            Integer customerId) {

        if (loan.getCustomer() == null
                || loan.getCustomer().getCustomerId() == null
                || !loan.getCustomer().getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException(
                    "Loan does not belong to this customer");
        }
    }

    private void validateActiveLoan(LoanAccount loan) {

        if (loan.getLoanStatus() != LoanAccountStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Only active loans can be foreclosed");
        }
    }

    // 8. CONFIRM VERIFIED PAYMENT AND APPLY FORECLOSURE
    // Call this only after the backend verifies payment with the
    // payment gateway. A request ID alone is not proof of payment.

    @Override
    public ForeClosureRequest confirmPayment(
            Integer customerId,
            Integer requestId) {

        ForeClosureRequest request = getRequest(requestId);

        LoanAccount requestLoan = request.getLoanAccount();

        if (requestLoan == null) {
            throw new ResourceNotFoundException(
                    "Loan account not found for request");
        }

        validateOwnership(requestLoan, customerId);

        if (request.getStatus() != ForeClosureStatus.APPROVED) {
            throw new IllegalArgumentException(
                    "Request must be approved before payment");
        }

        if (Boolean.TRUE.equals(request.getIsPaid())) {
            throw new IllegalArgumentException(
                    "Payment has already been recorded");
        }

        LoanAccount loan = loanAccountRepository
                .findById(requestLoan.getLoanAccountId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan account not found"));

        validateActiveLoan(loan);

        if (request.getForeClosureType() == ForeClosureType.FULL) {

            if (money(loan.getOutstandingPrincipal()).compareTo(
                    money(request.getPrincipalAmount())) != 0) {
                throw new IllegalArgumentException(
                        "Outstanding principal changed. Recalculate "
                                + "and approve the foreclosure amount again.");
            }

        } else if (request.getForeClosureType()
                == ForeClosureType.PARTIAL) {

            BigDecimal partial = request.getPartialAmount();

            if (partial == null
                    || partial.signum() <= 0
                    || partial.compareTo(
                    loan.getOutstandingPrincipal()) >= 0) {
                throw new IllegalArgumentException(
                        "Invalid partial foreclosure amount");
            }

        } else {
            throw new IllegalArgumentException(
                    "Unsupported foreclosure type");
        }

        List<EmiSchedule> allSchedules =
                emiScheduleRepository
                        .findByLoanAccount_LoanAccountIdOrderByInstallmentNoAsc(
                                loan.getLoanAccountId());

        List<EmiSchedule> unpaidSchedules = allSchedules.stream()
                .filter(s -> !"PAID".equalsIgnoreCase(
                        s.getPaymentStatus()))
                .filter(s -> !"CANCELLED".equalsIgnoreCase(
                        s.getPaymentStatus()))
                .toList();

        if (request.getForeClosureType() == ForeClosureType.FULL) {

            cancelSchedules(
                    unpaidSchedules,
                    "Cancelled due to full foreclosure");

            loan.setOutstandingPrincipal(
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            loan.setEmiAmount(
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            loan.setLoanStatus(LoanAccountStatus.CLOSED);

        } else {

            BigDecimal oldPrincipal = loan.getOutstandingPrincipal();
            BigDecimal partialAmount = request.getPartialAmount();

            BigDecimal newPrincipal = money(
                    oldPrincipal.subtract(partialAmount));

            if (unpaidSchedules.isEmpty()) {
                throw new IllegalArgumentException(
                        "No unpaid EMI schedules are available "
                                + "for recalculation");
            }

            List<LocalDate> dueDates = unpaidSchedules.stream()
                    .map(EmiSchedule::getDueDate)
                    .toList();

            int nextInstallmentNo = allSchedules.stream()
                    .map(EmiSchedule::getInstallmentNo)
                    .filter(Objects::nonNull)
                    .max(Integer::compareTo)
                    .orElse(0) + 1;

            BigDecimal newEmi = calculateEmi(
                    newPrincipal,
                    loan.getInterestRate(),
                    dueDates.size());

            cancelSchedules(
                    unpaidSchedules,
                    "Replaced after partial foreclosure");

            loan.setOutstandingPrincipal(newPrincipal);
            loan.setEmiAmount(newEmi);

            createRemainingSchedule(
                    loan,
                    dueDates,
                    newPrincipal,
                    newEmi,
                    nextInstallmentNo);
        }

        request.setIsPaid(true);
        request.setPaidDate(LocalDateTime.now());
        request.setStatus(ForeClosureStatus.PAID);

        loanAccountRepository.save(loan);

        ForeClosureRequest savedRequest =
                foreClosureRepository.save(request);

        if (savedRequest.getForeClosureType() == ForeClosureType.FULL) {

            notifyForeclosureCustomer(
                    loan,
                    "Full foreclosure completed",
                    "Full foreclosure payment has been recorded for request ID "
                            + savedRequest.getRequestId()
                            + ". Your loan account "
                            + loan.getLoanAccountNo()
                            + " has been closed.");

        } else {

            notifyForeclosureCustomer(
                    loan,
                    "Partial foreclosure completed",
                    "Partial foreclosure payment has been recorded for request ID "
                            + savedRequest.getRequestId()
                            + ". Amount applied: "
                            + savedRequest.getPartialAmount()
                            + ". Updated outstanding principal: "
                            + loan.getOutstandingPrincipal()
                            + ". Updated EMI: "
                            + loan.getEmiAmount()
                            + ". Your loan remains active.");
        }

        return savedRequest;
    }

    // 9. SEND IN-APP NOTIFICATION AND EMAIL

    private void notifyForeclosureCustomer(
            LoanAccount loan,
            String subject,
            String details) {

        if (loan == null || loan.getCustomer() == null) {
            throw new ResourceNotFoundException(
                    "Customer not found for foreclosure notification");
        }

        Customer customer = loan.getCustomer();
        Integer customerId = customer.getCustomerId();

        if (customerId == null) {
            throw new IllegalArgumentException(
                    "Customer ID is required for foreclosure notification");
        }

        // In-app notification
        notificationService.createNotification(
                customerId,
                subject + "\n" + details);

        // Email notification
        String email = customer.getEmail();

        if (email != null && !email.isBlank()) {

            String customerName = customer.getFirstName() == null
                    ? "Customer"
                    : customer.getFirstName();

            emailService.sendForeclosureUpdateEmail(
                    email,
                    customerName,
                    subject,
                    details);
        }
    }

    // 10. CANCEL OLD EMI SCHEDULES

    private void cancelSchedules(
            List<EmiSchedule> schedules,
            String reason) {

        for (EmiSchedule schedule : schedules) {
            schedule.setPaymentStatus("CANCELLED");
            schedule.setCancellationReason(reason);
        }

        emiScheduleRepository.saveAll(schedules);
    }

    // 11. EMI FORMULA

    private BigDecimal calculateEmi(
            BigDecimal principal,
            BigDecimal annualRate,
            int months) {

        if (principal == null || principal.signum() < 0) {
            throw new IllegalArgumentException(
                    "Invalid principal amount");
        }

        if (annualRate == null || annualRate.signum() < 0) {
            throw new IllegalArgumentException(
                    "Invalid interest rate");
        }

        if (months <= 0) {
            throw new IllegalArgumentException(
                    "Remaining tenure must be positive");
        }

        if (principal.signum() == 0) {
            return BigDecimal.ZERO.setScale(
                    2, RoundingMode.HALF_UP);
        }

        if (annualRate.signum() == 0) {
            return principal.divide(
                    BigDecimal.valueOf(months),
                    2,
                    RoundingMode.HALF_UP);
        }

        double monthlyRate = annualRate.doubleValue() / 1200.0;
        double factor = Math.pow(1.0 + monthlyRate, months);

        double emi = principal.doubleValue()
                * monthlyRate * factor / (factor - 1.0);

        return BigDecimal.valueOf(emi)
                .setScale(2, RoundingMode.HALF_UP);
    }

    // 12. CREATE NEW EMI SCHEDULE

    private void createRemainingSchedule(
            LoanAccount loan,
            List<LocalDate> dueDates,
            BigDecimal principal,
            BigDecimal emi,
            int nextInstallmentNo) {

        BigDecimal balance = money(principal);

        BigDecimal monthlyRate = loan.getInterestRate()
                .divide(
                        BigDecimal.valueOf(1200),
                        12,
                        RoundingMode.HALF_UP);

        List<EmiSchedule> newSchedules = new ArrayList<>();

        for (int i = 0; i < dueDates.size(); i++) {

            if (balance.signum() <= 0) {
                break;
            }

            BigDecimal opening = balance;

            BigDecimal interest = money(
                    opening.multiply(monthlyRate));

            BigDecimal principalPart = money(
                    emi.subtract(interest));

            if (principalPart.signum() <= 0) {
                throw new IllegalArgumentException(
                        "Calculated EMI does not cover monthly interest");
            }

            // The final installment clears the remaining balance.
            if (i == dueDates.size() - 1
                    || principalPart.compareTo(balance) >= 0) {
                principalPart = balance;
            }

            BigDecimal closing = money(
                    balance.subtract(principalPart)
                            .max(BigDecimal.ZERO));

            EmiSchedule schedule = new EmiSchedule();
            schedule.setLoanAccount(loan);
            schedule.setInstallmentNo(nextInstallmentNo + i);
            schedule.setDueDate(dueDates.get(i));
            schedule.setOpeningBalance(opening);
            schedule.setPrincipalAmount(principalPart);
            schedule.setInterestAmount(interest);
            schedule.setEmi(money(principalPart.add(interest)));
            schedule.setClosingBalance(closing);
            schedule.setPaymentStatus("PENDING");

            newSchedules.add(schedule);
            balance = closing;
        }

        emiScheduleRepository.saveAll(newSchedules);
    }
}