package com.example.Loan_Project.Service;





import com.example.Loan_Project.Entity.EmiSchedule;

import java.math.BigDecimal;

public interface EmiCalculatorService {

    BigDecimal calculateMonthlyRate(BigDecimal annualRate);

    EmiSchedule calculateInstallment(
            BigDecimal openingBalance,
            BigDecimal monthlyRate,
            BigDecimal emi,
            int installmentNo,
            int tenure
    );
}
