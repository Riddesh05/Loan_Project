
package com.example.Loan_Project.DTO;

import com.example.Loan_Project.Enum.DocumentVerificationStatus;
import com.example.Loan_Project.Enum.LoanApplicationStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LoanEligibilityResponse {

    private Long eligibilityId;

    private Integer cibilScore;
    private String cibilStatus;
    private BigDecimal eligibleAmount;

    private Boolean eligible;
    private String remarks;
    private LocalDateTime evaluatedAt;

    private Long loanApplicationId;
    private BigDecimal requestedAmount;
    private Integer tenureMonths;

    private LoanApplicationStatus applicationStatus;
    private DocumentVerificationStatus documentVerificationStatus;

    private Long customerId;
    private String customerName;
    private String customerEmail;

    private String loanType;
    private BigDecimal interestRate;
}
