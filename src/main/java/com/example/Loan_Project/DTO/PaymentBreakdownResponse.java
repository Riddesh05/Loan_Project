package com.example.Loan_Project.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentBreakdownResponse {

    private Integer emiScheduleId;
    private Long loanAccountId;
    private Integer installmentNo;

    private BigDecimal baseEmi;
    private BigDecimal carryForwardEmiAmount;
    private BigDecimal currentMonthEmiAmount;
    private Integer installmentsIncluded;
    private BigDecimal bounceCharges;
    private BigDecimal latePaymentInterest;

    private Integer overdueDays;

    private BigDecimal totalPayable;
    private BigDecimal dailyPenalty;
}