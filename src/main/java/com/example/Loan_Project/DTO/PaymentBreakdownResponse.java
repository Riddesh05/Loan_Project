package com.example.Loan_Project.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentBreakdownResponse {

    private Integer emiScheduleId;
    private Integer loanAccountId;
    private Integer installmentNo;

    private BigDecimal baseEmi;
    private BigDecimal bounceCharges;
    private BigDecimal latePaymentInterest;

    private Integer overdueDays;

    private BigDecimal totalPayable;
    private BigDecimal dailyPenalty;
}