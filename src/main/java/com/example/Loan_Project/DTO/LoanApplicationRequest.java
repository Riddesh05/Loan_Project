package com.example.Loan_Project.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanApplicationRequest {

    private Long customerId;

    private Long loanDealId;

    private BigDecimal requestedAmount;

    private Integer tenureMonths;

    private String bankName;

    private String bankAccountNumber;

    private String ifscCode;

    private Integer emiDay;
}