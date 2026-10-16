
package com.example.Loan_Project.DTO;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class EmiCalculationRequest {

    private Long loanDealId;
    private BigDecimal requestedAmount;
    private Integer tenureMonths;
}
