package com.example.Loan_Project.Service;

import com.example.Loan_Project.Entity.EmiSchedule;

import java.math.BigDecimal;

public interface PenaltyService {

    BigDecimal calculateDailyPenalty(long overdueDays);

    BigDecimal calculateBounceCharge();

    void accruePenaltiesForSchedule(EmiSchedule schedule);

    void recordBouncePenalty(Integer emiScheduleId);
}
