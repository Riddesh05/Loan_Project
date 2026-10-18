package com.example.Loan_Project.Service;





import com.example.Loan_Project.Entity.EmiSchedule;
import com.example.Loan_Project.Entity.LoanAccount;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
