package com.example.Loan_Project.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PenaltyChargeResponse {

    private Integer penaltyChargeId;
    private Integer emiScheduleId;
    private Long loanAccountId;

    private BigDecimal penaltyAmount;
    private String reason;
    private String status;
    private LocalDate penaltyDate;
    private LocalDateTime createdAt;
}