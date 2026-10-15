package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.DTO.*;
import com.example.Loan_Project.Entity.EmiSchedule;
import com.example.Loan_Project.Entity.LoanAccount;
import com.example.Loan_Project.Entity.LoanPayment;
import com.example.Loan_Project.Entity.PenaltyCharge;
import com.example.Loan_Project.Exception.BusinessException;
import com.example.Loan_Project.Exception.ResourceNotFoundException;
import com.example.Loan_Project.Repository.EmiScheduleRepository;
import com.example.Loan_Project.Repository.LoanAccountRepository;
import com.example.Loan_Project.Repository.LoanPaymentRepository;
import com.example.Loan_Project.Repository.PenaltyChargeRepository;
import com.example.Loan_Project.Service.EmiCalculatorService;
import com.example.Loan_Project.Service.EmiScheduleService;
import com.example.Loan_Project.Service.PenaltyService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmiScheduleServiceImpl implements EmiScheduleService {


    private final EmiCalculatorService emiCalculatorService;
    private final PenaltyService penaltyService;
    private final LoanAccountRepository loanAccountRepository;
    private final EmiScheduleRepository emiScheduleRepository;
    private final ModelMapper modelMapper;
    private final PenaltyChargeRepository penaltyChargeRepository;
    private final LoanPaymentRepository loanPaymentRepository;


    // 1. Generate EMI Schedule
    @Override
    @Transactional
    public void generateEmiSchedule(GenerateEmiScheduleRequest request) {

        // 1. Fetch Loan Account
        LoanAccount account = loanAccountRepository
                .findById(request.getLoanAccountId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Loan account not found"));
        // 2. Check Loan Account Status
        if (!"ACTIVE".equalsIgnoreCase(
                String.valueOf(account.getLoanStatus()))) {
            throw new BusinessException(
                    "EMI schedule can only be generated for ACTIVE loan accounts");
        }

        // 3. Prevent Duplicate EMI Schedule
        if (emiScheduleRepository.existsForAccount(
                account.getLoanAccountId())) {
            throw new BusinessException(
                    "EMI schedule already exists for this loan account");
        }

        // 4. Get Loan Details
        BigDecimal principal = account.getLoanAmount();
        BigDecimal annualRate = account.getInterestRate();
        Integer tenureValue = account.getTenureMonths();
        BigDecimal emi = account.getEmiAmount();

        // 5. Validate Loan Details
        if (principal == null
                || principal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Loan principal must be greater than zero");
        }

        if (annualRate == null
                || annualRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Interest rate cannot be negative");
        }
        if (tenureValue == null || tenureValue <= 0) {
            throw new BusinessException("Loan tenure must be greater than zero");
        }
        int tenure = tenureValue;
        if (emi == null
                || emi.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("EMI amount must be greater than zero");
        }

        // 6. Calculate Monthly Interest Rate
        BigDecimal monthlyRate =
                emiCalculatorService.calculateMonthlyRate(annualRate);

        // 7. Calculate First EMI Due Date
        LocalDate firstDueDate =
                calculateFirstDueDate(account.getDisbursementDate());

        // 8. Generate EMI Installments
        BigDecimal openingBalance = principal;

        List<EmiSchedule> schedules = new ArrayList<>(tenure);

        for (int number = 1; number <= tenure; number++) {

            EmiSchedule schedule =
                    emiCalculatorService.calculateInstallment(
                            openingBalance,
                            monthlyRate,
                            emi,
                            number,
                            tenure
                    );

            // Set Loan Account
            schedule.setLoanAccount(account);

            // Set Installment Number
            schedule.setInstallmentNo(number);

            // Set Due Date
            schedule.setDueDate(
                    firstDueDate.plusMonths(number - 1));

            // Set Initial Payment Status
            schedule.setPaymentStatus("Pending");

            // Add EMI to List
            schedules.add(schedule);

            // Update Opening Balance for Next Installment
            openingBalance = schedule.getClosingBalance();
        }
        // 9. Save All EMI Installments
        emiScheduleRepository.saveAll(schedules);
    }
    // 2. Get EMI Schedule
//    @Override
//    @Transactional(readOnly = true)
//    public List<EmiScheduleResponse> getEmiSchedule(Long loanAccountId) {
//
//        List<EmiSchedule> schedules =
//                emiScheduleRepository.findSchedules(loanAccountId);
//
//        boolean pendingFound = false;
//        List<EmiScheduleResponse> responses = new ArrayList<>();
//
//        for (EmiSchedule schedule : schedules) {
//
//            EmiScheduleResponse response =
//                    modelMapper.map(schedule, EmiScheduleResponse.class);
//
//            response.setLoanAccountId(
//                    schedule.getLoanAccount().getLoanAccountId());
//
//            boolean payable = false;
//
//            if ("Pending".equalsIgnoreCase(schedule.getPaymentStatus())
//                    && !pendingFound) {
//
//                payable = true;
//                pendingFound = true;
//            }
//
//            response.setIsPayable(payable);
//            responses.add(response);
//        }
//
//        return responses;
//    }




    @Override
    @Transactional(readOnly = true)
    public List<EmiScheduleResponse> getEmiSchedule(Long loanAccountId) {

        List<EmiSchedule> schedules =
                emiScheduleRepository.findSchedules(loanAccountId);

        // Exception test: no EMI schedules found
        if (schedules.isEmpty()) {
            throw new ResourceNotFoundException(
                    "EMI schedule not found for loan account: " + loanAccountId
            );
        }



        YearMonth currentMonth = YearMonth.now(
                ZoneId.of("Asia/Kolkata")
        );

        boolean pendingFound = false;

        List<EmiScheduleResponse> responses = new ArrayList<>();

        for (EmiSchedule schedule : schedules) {

            EmiScheduleResponse response =
                    modelMapper.map(schedule, EmiScheduleResponse.class);

            response.setLoanAccountId(
                    schedule.getLoanAccount().getLoanAccountId()
            );

            boolean payable = false;

            if ("Pending".equalsIgnoreCase(schedule.getPaymentStatus())
                    && !pendingFound) {

                pendingFound = true;

                if (schedule.getDueDate() != null) {

                    YearMonth emiMonth =
                            YearMonth.from(schedule.getDueDate());

                    // EMI month start ho chuka hai.
                    // Due date cross hone par bhi unpaid EMI payable rahegi.
                    payable = !currentMonth.isBefore(emiMonth);
                }
            }

            response.setIsPayable(payable);
            responses.add(response);
        }

        return responses;
    }


    // 3. Get EMI Summary
    @Override
    @Transactional(readOnly = true)
    public EmiScheduleSummaryResponse getSummary(Long loanAccountId) {

        List<EmiSchedule> schedules =
                emiScheduleRepository.findSchedules(loanAccountId);

        if (schedules.isEmpty()) {
            throw new ResourceNotFoundException(
                    "EMI schedule not found for loan account");
        }

        long paidCount = schedules.stream()
                .filter(e -> "Paid".equalsIgnoreCase(e.getPaymentStatus()))
                .count();

        long pendingCount = schedules.stream()
                .filter(e -> "Pending".equalsIgnoreCase(e.getPaymentStatus()))
                .count();

        BigDecimal totalPaid = schedules.stream()
                .filter(e -> "Paid".equalsIgnoreCase(e.getPaymentStatus()))
                .map(EmiSchedule::getEmi)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPending = schedules.stream()
                .filter(e -> "Pending".equalsIgnoreCase(e.getPaymentStatus()))
                .map(EmiSchedule::getEmi)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingPrincipal = schedules.stream()
                .filter(e -> "Pending".equalsIgnoreCase(e.getPaymentStatus()))
                .map(EmiSchedule::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        EmiSchedule nextPending = schedules.stream()
                .filter(e -> "Pending".equalsIgnoreCase(e.getPaymentStatus()))
                .findFirst()
                .orElse(null);

        EmiScheduleSummaryResponse response =
                new EmiScheduleSummaryResponse();

        response.setLoanAccountId(loanAccountId);
        response.setTotalInstallments(schedules.size());
        response.setPaidInstallments((int) paidCount);
        response.setPendingInstallments((int) pendingCount);
        response.setTotalPaidAmount(totalPaid);
        response.setTotalPendingAmount(totalPending);
        response.setRemainingPrincipal(remainingPrincipal);

        if (nextPending != null) {
            response.setNextInstallmentNo(
                    nextPending.getInstallmentNo());

            response.setNextDueDate(
                    nextPending.getDueDate());

            response.setNextEmiAmount(
                    nextPending.getEmi());
        }

        return response;
    }


    // 4. Calculate First Due Date
    private LocalDate calculateFirstDueDate(LocalDateTime disbursementDate) {

        if (disbursementDate == null) {
            throw new BusinessException("Disbursement date not found");
        }

        return disbursementDate.toLocalDate().plusMonths(1);
    }







    // 6. Verify/record a payment. Production use must call this only after a trusted
    // payment provider has independently confirmed the transaction as successful.

    @Override
    @Transactional
    public void verifyPayment(PaymentVerifyRequest request) {

        // 1. Validate request
        if (request == null
                || request.getEmiScheduleId() == null
                || request.getLoanAccountId() == null
                || request.getAmountPaid() == null
                || request.getTransactionRefNo() == null
                || request.getTransactionRefNo().isBlank()) {
            throw new BusinessException("Invalid payment request");
        }

        // 2. Fetch selected EMI
        EmiSchedule requestedSchedule = emiScheduleRepository
                .findById(request.getEmiScheduleId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("EMI schedule not found"));

        // 3. Fetch loan account
        LoanAccount account = loanAccountRepository
                .findById(request.getLoanAccountId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Loan account not found"));

        // 4. Verify EMI belongs to this loan account
        if (!requestedSchedule.getLoanAccount().getLoanAccountId()
                .equals(account.getLoanAccountId())) {
            throw new BusinessException(
                    "EMI does not belong to this loan account");
        }

        // 5. Prevent duplicate payment
        if (loanPaymentRepository.existsByTransactionRefNo(
                request.getTransactionRefNo())) {
            throw new BusinessException("Transaction already processed");
        }

        // 6. Selected EMI must be pending
        if (!"Pending".equalsIgnoreCase(
                requestedSchedule.getPaymentStatus())) {
            throw new BusinessException("EMI is not pending");
        }

        // 7. Enforce first-pending-EMI payment sequence
        List<EmiSchedule> allSchedules =
                emiScheduleRepository.findSchedules(
                        account.getLoanAccountId());

        EmiSchedule firstPending = allSchedules.stream()
                .filter(e -> "Pending".equalsIgnoreCase(
                        e.getPaymentStatus()))
                .findFirst()
                .orElseThrow(() ->
                        new BusinessException("No pending EMI available"));

        if (!firstPending.getEmiScheduleId()
                .equals(requestedSchedule.getEmiScheduleId())) {
            throw new BusinessException(
                    "Pay the first pending EMI first. "
                            + "Future EMIs cannot be paid out of sequence.");
        }

        // 8. Selected EMI must have reached its due month
        ZoneId businessZone = ZoneId.of("Asia/Kolkata");
        LocalDate today = LocalDate.now(businessZone);

        if (requestedSchedule.getDueDate() == null) {
            throw new BusinessException("EMI due date is missing");
        }

        YearMonth currentMonth = YearMonth.from(today);
        YearMonth emiMonth = YearMonth.from(
                requestedSchedule.getDueDate());

        if (currentMonth.isBefore(emiMonth)) {
            throw new BusinessException(
                    "This EMI is not payable yet. Payment starts in "
                            + emiMonth);
        }

        // 9. Get the exact eligible installments for this payment
        List<EmiSchedule> installmentsToSettle =
                getInstallmentsDueThisMonth(
                        account.getLoanAccountId());

        if (installmentsToSettle.isEmpty()) {
            throw new BusinessException(
                    "No EMI is payable today");
        }

        boolean requestedEmiIncluded = installmentsToSettle.stream()
                .anyMatch(e -> e.getEmiScheduleId()
                        .equals(requestedSchedule.getEmiScheduleId()));

        if (!requestedEmiIncluded) {
            throw new BusinessException(
                    "Selected EMI is not due for payment yet");
        }

        // 10. Calculate payable amount on the server
        PaymentBreakdownResponse breakdown =
                getPaymentBreakdown(
                        requestedSchedule.getEmiScheduleId());

        if (breakdown.getTotalPayable() == null
                || request.getAmountPaid().compareTo(
                breakdown.getTotalPayable()) != 0) {
            throw new BusinessException(
                    "Incorrect payment amount. Expected: "
                            + breakdown.getTotalPayable());
        }

        // 11. Calculate principal reduction and validate EMI amounts
        BigDecimal principalToReduce = BigDecimal.ZERO;

        for (EmiSchedule installment : installmentsToSettle) {

            if (installment.getPrincipalAmount() != null) {
                principalToReduce = principalToReduce.add(
                        installment.getPrincipalAmount());
            }
        }

        // 12. Mark included installments as paid
        LocalDateTime now = LocalDateTime.now(businessZone);

        for (EmiSchedule installment : installmentsToSettle) {
            installment.setPaymentStatus("Paid");
            installment.setPaidDate(now);
        }

        emiScheduleRepository.saveAll(installmentsToSettle);

        // 13. Save payment record
        LoanPayment payment = new LoanPayment();
        payment.setLoanAccount(account);
        payment.setPaymentAmount(request.getAmountPaid());
        payment.setPaymentDate(now);
        payment.setPaymentStatus("SUCCESS");
        payment.setPaymentName(request.getPaymentMode());
        payment.setEmiScheduleId(
                requestedSchedule.getEmiScheduleId());
        payment.setTransactionRefNo(
                request.getTransactionRefNo());

        loanPaymentRepository.save(payment);

        // 14. Update total amount paid
        BigDecimal oldTotalPaid =
                account.getTotalPaidAmount() == null
                        ? BigDecimal.ZERO
                        : account.getTotalPaidAmount();

        account.setTotalPaidAmount(
                oldTotalPaid.add(request.getAmountPaid()));

        // 15. Update outstanding principal
        BigDecimal outstandingPrincipal =
                account.getOutstandingPrincipal() == null
                        ? BigDecimal.ZERO
                        : account.getOutstandingPrincipal();

        BigDecimal newOutstanding =
                outstandingPrincipal.subtract(principalToReduce);

        if (newOutstanding.compareTo(BigDecimal.ZERO) < 0) {
            newOutstanding = BigDecimal.ZERO;
        }

        account.setOutstandingPrincipal(newOutstanding);

        loanAccountRepository.save(account);

        // 16. Settle penalties only for installments included in this payment
        for (EmiSchedule installment : installmentsToSettle) {

            List<PenaltyCharge> pendingPenalties =
                    penaltyChargeRepository
                            .findByEmiSchedule_EmiScheduleId(
                                    installment.getEmiScheduleId())
                            .stream()
                            .filter(p -> "Pending".equalsIgnoreCase(
                                    p.getStatus()))
                            .toList();

            for (PenaltyCharge penalty : pendingPenalties) {
                penalty.setStatus("PAID");
            }

            penaltyChargeRepository.saveAll(pendingPenalties);
        }
    }


    @Override
    @Transactional
    public void recordBouncePenalty(Integer emiScheduleId) {
        // Internal-only hook for a trusted payment gateway/bank callback.
        // There is intentionally no public borrower-facing bounce endpoint.
        penaltyService.recordBouncePenalty(emiScheduleId);
    }


    @Override
    @Transactional(readOnly = true)
    public List<PenaltyChargeResponse> getPenaltiesByLoanAccountId(
            Long loanAccountId) {

        // 1. Check whether loan account exists
        loanAccountRepository.findById(loanAccountId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Loan account not found"));

        // 2. Fetch penalties from database
        List<PenaltyCharge> penalties =
                penaltyChargeRepository
                        .findByLoanAccount_LoanAccountIdOrderByCreatedAtDesc(
                                loanAccountId
                        );

        // 3. Convert Entity into DTO
        return penalties.stream()
                .map(penalty -> {
                    PenaltyChargeResponse response =
                            new PenaltyChargeResponse();

                    response.setPenaltyChargeId(
                            penalty.getPenaltyChargeId());

                    response.setEmiScheduleId(
                            penalty.getEmiSchedule()
                                    .getEmiScheduleId());

                    response.setLoanAccountId(
                            penalty.getLoanAccount()
                                    .getLoanAccountId());

                    response.setPenaltyAmount(
                            penalty.getPenaltyAmount());

                    response.setReason(penalty.getReason());
                    response.setStatus(penalty.getStatus());
                    response.setPenaltyDate(penalty.getPenaltyDate());
                    response.setCreatedAt(penalty.getCreatedAt());

                    return response;
                })
                .toList();
    }


    @Override
    @Transactional
    public PaymentBreakdownResponse getPaymentBreakdown(Integer emiScheduleId) {
        EmiSchedule schedule = emiScheduleRepository.findById(emiScheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("EMI schedule not found"));

        if (!"Pending".equalsIgnoreCase(schedule.getPaymentStatus())) {
            throw new BusinessException("Payment breakdown is available only for a pending EMI");
        }

        List<EmiSchedule> allSchedules =
                emiScheduleRepository.findSchedules(schedule.getLoanAccount().getLoanAccountId());
        EmiSchedule firstPending = allSchedules.stream()
                .filter(e -> "Pending".equalsIgnoreCase(e.getPaymentStatus()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("No pending EMI available"));

        if (!firstPending.getEmiScheduleId().equals(schedule.getEmiScheduleId())) {
            throw new BusinessException("Only the first pending EMI can be paid. Clear previous pending EMIs first.");
        }

        YearMonth currentMonth = YearMonth.now(ZoneId.of("Asia/Kolkata"));
        if (currentMonth.isBefore(YearMonth.from(schedule.getDueDate()))) {
            throw new BusinessException("This EMI is not payable yet. Payment starts in "
                    + YearMonth.from(schedule.getDueDate()));
        }

        List<EmiSchedule> payableInstallments =
                getInstallmentsDueThisMonth(schedule.getLoanAccount().getLoanAccountId());

        // Persist missing charges for every unpaid installment included in the
        // current payable amount (previous arrears + installments due this month).
        payableInstallments.forEach(penaltyService::accruePenaltiesForSchedule);

        BigDecimal baseEmi = schedule.getEmi() == null ? BigDecimal.ZERO : schedule.getEmi();
        BigDecimal carryForwardEmiAmount = payableInstallments.stream()
                .filter(e -> !e.getEmiScheduleId().equals(schedule.getEmiScheduleId()))
                .filter(e -> YearMonth.from(e.getDueDate()).isBefore(currentMonth))
                .map(EmiSchedule::getEmi)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal currentMonthEmiAmount = payableInstallments.stream()
                .filter(e -> !e.getEmiScheduleId().equals(schedule.getEmiScheduleId()))
                .filter(e -> YearMonth.from(e.getDueDate()).equals(currentMonth))
                .map(EmiSchedule::getEmi)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        BigDecimal bounceCharges =
                sumPendingPenalties(payableInstallments, "BOUNCE");

        BigDecimal dailyPenalty =
                sumPendingPenalties(payableInstallments, "LATE_FEE");

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        int overdueDays = 0;
        if (today.isAfter(schedule.getDueDate())) {
            overdueDays = (int) ChronoUnit.DAYS.between(schedule.getDueDate(), today);
        }

        BigDecimal totalPayable = baseEmi
                .add(carryForwardEmiAmount)
                .add(currentMonthEmiAmount)
                .add(bounceCharges)
                .add(dailyPenalty)
                .setScale(2, RoundingMode.HALF_UP);

        PaymentBreakdownResponse response = new PaymentBreakdownResponse();
        response.setEmiScheduleId(schedule.getEmiScheduleId());
        response.setLoanAccountId(schedule.getLoanAccount().getLoanAccountId());
        response.setInstallmentNo(schedule.getInstallmentNo());
        response.setBaseEmi(baseEmi);
        response.setCarryForwardEmiAmount(carryForwardEmiAmount);
        response.setCurrentMonthEmiAmount(currentMonthEmiAmount);
        response.setInstallmentsIncluded(payableInstallments.size());
        response.setDailyPenalty(dailyPenalty);
        response.setBounceCharges(bounceCharges);
        response.setLatePaymentInterest(BigDecimal.ZERO);
        response.setOverdueDays(overdueDays);
        response.setTotalPayable(totalPayable);
        return response;
    }

    private List<EmiSchedule> getInstallmentsDueThisMonth(Long loanAccountId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));

        return emiScheduleRepository.findSchedules(loanAccountId)
                .stream()
                .filter(e -> "Pending".equalsIgnoreCase(e.getPaymentStatus()))
                .filter(e -> e.getDueDate() != null)
                .filter(e -> !e.getDueDate().isAfter(today))
                .toList();
    }

    private BigDecimal sumPendingPenalties(List<EmiSchedule> schedules, String reason) {
        BigDecimal total = BigDecimal.ZERO;
        for (EmiSchedule schedule : schedules) {
            total = total.add(penaltyChargeRepository
                    .findByEmiSchedule_EmiScheduleIdAndReasonAndStatus(
                            schedule.getEmiScheduleId(), reason, "Pending")
                    .stream()
                    .map(PenaltyCharge::getPenaltyAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        return total;
    }

}
