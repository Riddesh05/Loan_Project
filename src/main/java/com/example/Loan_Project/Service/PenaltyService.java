
package com.example.Loan_Project.Service;

import java.math.BigDecimal;

public interface PenaltyService {
    BigDecimal calculateDailyPenalty(long overdueDays);
    BigDecimal calculateBounceCharge();
}
