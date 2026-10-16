package com.example.Loan_Project.Service;

import com.example.Loan_Project.Entity.EmiSchedule;

import java.math.BigDecimal;

public interface PenaltyService {

    BigDecimal calculateDailyPenalty(long overdueDays);

    BigDecimal calculateBounceCharge();

    /** Creates any missing overdue charges for a pending EMI. */
    void accruePenaltiesForSchedule(EmiSchedule schedule);

    /** Call only after a trusted payment provider confirms a genuine bounce. */
    void recordBouncePenalty(Integer emiScheduleId);
}
