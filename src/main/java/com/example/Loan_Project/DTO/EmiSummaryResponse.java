package com.example.Loan_Project.DTO;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmiSummaryResponse {

    private BigDecimal totalPaidAmount;

    private BigDecimal totalPendingAmount;

    private BigDecimal remainingPrincipalBalance;

    private LocalDate nextDueDate;

    private BigDecimal nextDueAmount;
}
