
package com.example.Loan_Project.DTO;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class EmiCalculationResponse {

    private Long loanDealId;
    private BigDecimal principalAmount;
    private BigDecimal annualInterestRate;
    private Integer tenureMonths;
    private BigDecimal monthlyEmi;
    private BigDecimal totalPayment;
    private BigDecimal totalInterest;
}
