package com.example.Loan_Project.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmiScheduleSummaryResponse {

    private Long loanAccountId;

    private Integer totalInstallments;
    private Integer paidInstallments;
    private Integer pendingInstallments;

    private BigDecimal totalPaidAmount;
    private BigDecimal totalPendingAmount;
    private BigDecimal remainingPrincipal;

    private Integer nextInstallmentNo;
    private LocalDate nextDueDate;
    private BigDecimal nextEmiAmount;
}