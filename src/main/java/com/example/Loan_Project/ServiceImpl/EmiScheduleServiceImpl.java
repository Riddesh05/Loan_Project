package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.DTO.*;
import com.example.Loan_Project.Entity.*;
import com.example.Loan_Project.Repository.*;
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
    private final InterestAccrualRepository interestAccrualRepository;


    // 1. Generate EMI Schedule
    @Override
    @Transactional
    public void generateEmiSchedule(GenerateEmiScheduleRequest request) {

        // 1. Fetch Loan Account
        LoanAccount account = loanAccountRepository
                .findById(request.getLoanAccountId())
                .orElseThrow(() ->
                        new RuntimeException("Loan account not found"));

        // 2. Check Loan Account Status
        if (!"ACTIVE".equalsIgnoreCase(
                String.valueOf(account.getLoanStatus()))) {
            throw new RuntimeException(
                    "EMI schedule can only be generated for ACTIVE loan accounts");
        }

//        // 3. Prevent Duplicate EMI Schedule
//        if (emiScheduleRepository.existsForAccount(
//                account.getLoanAccountId())) {
//            throw new RuntimeException(
//                    "EMI schedule already exists for this loan account");
//        }

        // 4. Get Loan Details
        BigDecimal principal = account.getLoanAmount();
        BigDecimal annualRate = account.getInterestRate();
        int tenure = account.getTenureMonths();
        BigDecimal emi = account.getEmiAmount();

        // 5. Validate Loan Details
        if (principal == null
                || principal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Loan principal must be greater than zero");
        }

        if (annualRate == null
                || annualRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Interest rate cannot be negative");
        }
        if (tenure <= 0) {
            throw new RuntimeException("Loan tenure must be greater than zero");
        }
        if (emi == null
                || emi.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("EMI amount must be greater than zero");
        }

        // 6. Calculate Monthly Interest Rate
        BigDecimal monthlyRate =
                emiCalculatorService.calculateMonthlyRate(annualRate);

        // 7. Calculate First EMI Due Date
        LocalDate firstDueDate =
                calculateFirstDueDate(account.getDisbursementDate());

        // 8. Generate EMI Installments
        BigDecimal openingBalance = principal;

        List<EmiSchedule> schedules = new ArrayList<>();

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
    @Override
    @Transactional(readOnly = true)
    public List<EmiScheduleResponse> getEmiSchedule(Integer loanAccountId) {

        List<EmiSchedule> schedules =
                emiScheduleRepository.findSchedules(loanAccountId);

        boolean pendingFound = false;
        List<EmiScheduleResponse> responses = new ArrayList<>();

        for (EmiSchedule schedule : schedules) {

            EmiScheduleResponse response =
                    modelMapper.map(schedule, EmiScheduleResponse.class);

            response.setLoanAccountId(
                    schedule.getLoanAccount().getLoanAccountId());

            boolean payable = false;

            if ("Pending".equalsIgnoreCase(schedule.getPaymentStatus())
                    && !pendingFound) {

                payable = true;
                pendingFound = true;
            }

            response.setIsPayable(payable);
            responses.add(response);
        }

        return responses;
    }


    // 3. Get EMI Summary
    @Override
    @Transactional(readOnly = true)
    public EmiScheduleSummaryResponse getSummary(Integer loanAccountId) {

        List<EmiSchedule> schedules =
                emiScheduleRepository.findSchedules(loanAccountId);

        if (schedules.isEmpty()) {
            throw new RuntimeException(
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
            throw new RuntimeException("Disbursement date not found");
        }

        return disbursementDate.toLocalDate().plusMonths(1);
    }



    @Override
    @Transactional(readOnly = true)
    public PaymentBreakdownResponse getPaymentBreakdown(Integer emiScheduleId) {

        EmiSchedule schedule = emiScheduleRepository.findById(emiScheduleId)
                .orElseThrow(() -> new RuntimeException("EMI schedule not found"));

        BigDecimal baseEmi = schedule.getEmi();
        BigDecimal bounceCharges = penaltyChargeRepository
                .findByEmiSchedule_EmiScheduleIdAndReasonAndStatus(
                        emiScheduleId, "BOUNCE", "Pending")
                .stream()
                .map(PenaltyCharge::getPenaltyAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        LocalDate today = LocalDate.now();
        int overdueDays = 0;

        if ("Pending".equalsIgnoreCase(schedule.getPaymentStatus())
                && today.isAfter(schedule.getDueDate())) {
            overdueDays = (int) ChronoUnit.DAYS.between(
                    schedule.getDueDate(), today);
        }

        BigDecimal dailyPenalty =
                penaltyService.calculateDailyPenalty(overdueDays);

        BigDecimal totalPayable = baseEmi
                .add(bounceCharges)
                .add(dailyPenalty)
                .setScale(2, RoundingMode.HALF_UP);

        PaymentBreakdownResponse response = new PaymentBreakdownResponse();
        response.setEmiScheduleId(schedule.getEmiScheduleId());
        response.setLoanAccountId(schedule.getLoanAccount().getLoanAccountId());
        response.setInstallmentNo(schedule.getInstallmentNo());
        response.setBaseEmi(baseEmi);
        response.setDailyPenalty(dailyPenalty);
        response.setBounceCharges(bounceCharges);
        response.setLatePaymentInterest(BigDecimal.ZERO);
        response.setOverdueDays(overdueDays);
        response.setTotalPayable(totalPayable);

        return response;
    }



    // 6. Verify Payment
    @Override
    @Transactional
    public void verifyPayment(PaymentVerifyRequest request) {

        EmiSchedule schedule = emiScheduleRepository
                .findById(request.getEmiScheduleId())
                .orElseThrow(() ->
                        new RuntimeException("EMI schedule not found"));

        LoanAccount account = loanAccountRepository
                .findById(request.getLoanAccountId())
                .orElseThrow(() ->
                        new RuntimeException("Loan account not found"));

        // Check that this EMI belongs to this loan account
        if (!schedule.getLoanAccount().getLoanAccountId()
                .equals(account.getLoanAccountId())) {

            throw new RuntimeException(
                    "EMI does not belong to this loan account");
        }

        if ("Paid".equalsIgnoreCase(schedule.getPaymentStatus())) {
            throw new RuntimeException("EMI is already paid");
        }

        if (loanPaymentRepository.existsByTransactionRefNo(
                request.getTransactionRefNo())) {

            throw new RuntimeException("Transaction already processed");
        }

        // Only the first pending EMI can be paid
        List<EmiSchedule> schedules =
                emiScheduleRepository.findSchedules(
                        account.getLoanAccountId());

        EmiSchedule firstPending = schedules.stream()
                .filter(e -> "Pending".equalsIgnoreCase(
                        e.getPaymentStatus()))
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("No pending EMI available"));

        if (!firstPending.getEmiScheduleId()
                .equals(schedule.getEmiScheduleId())) {

            throw new RuntimeException(
                    "Future EMI cannot be paid before previous EMI");
        }

        PaymentBreakdownResponse breakdown =
                getPaymentBreakdown(schedule.getEmiScheduleId());

        if (request.getAmountPaid()
                .compareTo(breakdown.getTotalPayable()) != 0) {

            throw new RuntimeException(
                    "Incorrect payment amount. Expected: "
                            + breakdown.getTotalPayable());
        }

        // Mark EMI as paid
        schedule.setPaymentStatus("Paid");
        schedule.setPaidDate(LocalDateTime.now());
        emiScheduleRepository.save(schedule);

        // Save payment record
        LoanPayment payment = new LoanPayment();

        payment.setLoanAccount(account);
        payment.setPaymentAmount(request.getAmountPaid());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentStatus("SUCCESS");
        payment.setPaymentName(request.getPaymentMode());
        payment.setEmiScheduleId(schedule.getEmiScheduleId());
        payment.setTransactionRefNo(request.getTransactionRefNo());

        loanPaymentRepository.save(payment);

        // Update loan account totals
        BigDecimal oldTotalPaid = account.getTotalPaidAmount() == null
                ? BigDecimal.ZERO
                : account.getTotalPaidAmount();

        account.setTotalPaidAmount(
                oldTotalPaid.add(request.getAmountPaid()));

        BigDecimal outstandingPrincipal =
                account.getOutstandingPrincipal() == null
                        ? BigDecimal.ZERO
                        : account.getOutstandingPrincipal();

        BigDecimal newOutstanding = outstandingPrincipal
                .subtract(schedule.getPrincipalAmount());

        if (newOutstanding.compareTo(BigDecimal.ZERO) < 0) {
            newOutstanding = BigDecimal.ZERO;
        }

        account.setOutstandingPrincipal(newOutstanding);
        loanAccountRepository.save(account);

        // Mark pending penalties as paid
        List<PenaltyCharge> penalties =
                penaltyChargeRepository
                        .findByScheduleAndStatus(
                                schedule.getEmiScheduleId(), "Pending");

        penalties.forEach(p -> p.setStatus("PAID"));
        penaltyChargeRepository.saveAll(penalties);

        // Mark pending interest accruals as paid


    }
}