package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.Entity.EmiSchedule;
import com.example.Loan_Project.Entity.PenaltyCharge;
import com.example.Loan_Project.Exception.BusinessException;
import com.example.Loan_Project.Exception.ResourceNotFoundException;
import com.example.Loan_Project.Repository.EmiScheduleRepository;
import com.example.Loan_Project.Repository.PenaltyChargeRepository;
import com.example.Loan_Project.Service.PenaltyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class PenaltyServiceImpl implements PenaltyService {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Kolkata");
    private static final BigDecimal DAILY_PENALTY = new BigDecimal("1.00");
    private static final BigDecimal BOUNCE_CHARGE = new BigDecimal("500.00");

    private final PenaltyChargeRepository penaltyChargeRepository;
    private final EmiScheduleRepository emiScheduleRepository;

    @Override
    public BigDecimal calculateDailyPenalty(long overdueDays) {
        return DAILY_PENALTY.multiply(BigDecimal.valueOf(Math.max(0, overdueDays)));
    }

    @Override
    public BigDecimal calculateBounceCharge() {
        return BOUNCE_CHARGE;
    }

    /**
     * Business rule: after the due date, a still-pending EMI receives one
     * MISSED_EMI charge of Rs. 500 and Rs. 1 for every overdue calendar day.
     * MISSED_EMI is deliberately distinct from a verified bank/payment bounce.
     */
    @Override
    @Transactional
    public void accruePenaltiesForSchedule(EmiSchedule schedule) {
        if (schedule == null
                || !"Pending".equalsIgnoreCase(schedule.getPaymentStatus())
                || schedule.getDueDate() == null) {
            return;
        }

        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        LocalDate dueDate = schedule.getDueDate();

        // The EMI is not missed until the due date has passed.
        if (!today.isAfter(dueDate)) {
            return;
        }

        boolean bounceChargeExists = penaltyChargeRepository
                .existsByEmiSchedule_EmiScheduleIdAndReason(
                        schedule.getEmiScheduleId(), "BOUNCE");

        // Only one ₹500 event charge per EMI. A genuine bounce takes precedence
        // over a generic missed-EMI reason, preventing accidental double charging.
        if (!bounceChargeExists) {
            PenaltyCharge bounceCharge = new PenaltyCharge();
            bounceCharge.setEmiSchedule(schedule);
            bounceCharge.setLoanAccount(schedule.getLoanAccount());
            bounceCharge.setPenaltyAmount(BOUNCE_CHARGE);
            bounceCharge.setReason("BOUNCE");
            bounceCharge.setStatus("Pending");
            bounceCharge.setPenaltyDate(dueDate.plusDays(1));
            bounceCharge.setCreatedAt(LocalDateTime.now(BUSINESS_ZONE));

            penaltyChargeRepository.save(bounceCharge);
        }

        // One persisted record per overdue date, beginning the day after due date.
        LocalDate penaltyDate = dueDate.plusDays(1);
        while (!penaltyDate.isAfter(today)) {
            boolean lateFeeExists = penaltyChargeRepository
                    .existsByEmiSchedule_EmiScheduleIdAndReasonAndPenaltyDate(
                            schedule.getEmiScheduleId(), "LATE_FEE", penaltyDate);

            if (!lateFeeExists) {
                PenaltyCharge lateFee = new PenaltyCharge();
                lateFee.setEmiSchedule(schedule);
                lateFee.setLoanAccount(schedule.getLoanAccount());
                lateFee.setPenaltyAmount(DAILY_PENALTY);
                lateFee.setReason("LATE_FEE");
                lateFee.setStatus("Pending");
                lateFee.setPenaltyDate(penaltyDate);
                lateFee.setCreatedAt(LocalDateTime.now(BUSINESS_ZONE));
                penaltyChargeRepository.save(lateFee);
            }
            penaltyDate = penaltyDate.plusDays(1);
        }
    }

    /**
     * Internal hook for a trusted gateway/bank callback only. Do not expose
     * this as a public endpoint that a borrower can call directly.
     */
    @Override
    @Transactional
    public void recordBouncePenalty(Integer emiScheduleId) {
        EmiSchedule schedule = emiScheduleRepository.findById(emiScheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("EMI schedule not found"));

        if (!"Pending".equalsIgnoreCase(schedule.getPaymentStatus())) {
            throw new BusinessException("Bounce penalty can only be recorded for a pending EMI");
        }

        boolean alreadyExists = penaltyChargeRepository
                .existsByEmiSchedule_EmiScheduleIdAndReason(emiScheduleId, "BOUNCE");
        if (alreadyExists) {
            return;
        }

        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        LocalDateTime now = LocalDateTime.now(BUSINESS_ZONE);

        // If a generic missed-EMI fee was already accrued, convert that same
        // pending ₹500 record into the verified BOUNCE reason instead of adding
        // a second ₹500 fee for the same EMI.
        var pendingMissedFees = penaltyChargeRepository
                .findByEmiSchedule_EmiScheduleIdAndReasonAndStatus(
                        emiScheduleId, "MISSED_EMI", "Pending");
        if (!pendingMissedFees.isEmpty()) {
            PenaltyCharge existingFee = pendingMissedFees.get(0);
            existingFee.setReason("BOUNCE");
            existingFee.setPenaltyAmount(BOUNCE_CHARGE);
            existingFee.setPenaltyDate(today);
            existingFee.setCreatedAt(now);
            penaltyChargeRepository.save(existingFee);
            return;
        }

        PenaltyCharge penalty = new PenaltyCharge();
        penalty.setEmiSchedule(schedule);
        penalty.setLoanAccount(schedule.getLoanAccount());
        penalty.setPenaltyAmount(BOUNCE_CHARGE);
        penalty.setReason("BOUNCE");
        penalty.setStatus("Pending");
        penalty.setPenaltyDate(today);
        penalty.setCreatedAt(now);
        penaltyChargeRepository.save(penalty);
    }
}
