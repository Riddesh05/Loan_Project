
package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.Entity.EmiSchedule;
import com.example.Loan_Project.Service.EmiCalculatorService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class EmiCalculatorServiceImpl
        implements EmiCalculatorService {

    @Override
    public BigDecimal calculateMonthlyRate(BigDecimal annualRate) {

        return annualRate.divide(
                BigDecimal.valueOf(1200),
                10,
                RoundingMode.HALF_UP
        );
    }

    @Override
    public EmiSchedule calculateInstallment(
            BigDecimal openingBalance,
            BigDecimal monthlyRate,
            BigDecimal emi,
            int installmentNo,
            int tenure) {

        // Calculate interest
        BigDecimal interest = openingBalance
                .multiply(monthlyRate)
                .setScale(2, RoundingMode.HALF_UP);

        // Calculate principal
        BigDecimal principal = emi
                .subtract(interest)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal currentEmi = emi;

        // Last EMI adjustment
        if (installmentNo == tenure) {
            principal = openingBalance;

            currentEmi = principal
                    .add(interest)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // Calculate closing balance
        BigDecimal closingBalance = openingBalance
                .subtract(principal)
                .setScale(2, RoundingMode.HALF_UP);

        if (closingBalance.compareTo(BigDecimal.ZERO) < 0) {
            closingBalance = BigDecimal.ZERO;
        }

        // Store calculation result in an object
        EmiSchedule schedule = new EmiSchedule();

        schedule.setOpeningBalance(openingBalance);
        schedule.setInterestAmount(interest);
        schedule.setPrincipalAmount(principal);
        schedule.setEmi(currentEmi);
        schedule.setClosingBalance(closingBalance);

        return schedule;
    }
}
