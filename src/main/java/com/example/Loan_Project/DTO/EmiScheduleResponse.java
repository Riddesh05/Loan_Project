package com.example.Loan_Project.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EmiScheduleResponse {

    private Integer emiScheduleId;
    private Long loanAccountId;
    private Integer installmentNo;
    private LocalDate dueDate;

    private BigDecimal openingBalance;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal emi;
    private BigDecimal closingBalance;

    private String paymentStatus;
    private LocalDateTime paidDate;

    private Boolean isPayable;
}