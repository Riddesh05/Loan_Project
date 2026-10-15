package com.example.Loan_Project.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentBreakdownResponse {

    private Integer emiScheduleId;
    private Long loanAccountId;
    private Integer installmentNo;

    // EMI amount for the first pending installment requested.
    private BigDecimal baseEmi;
    // Other unpaid EMIs from previous months.
    private BigDecimal carryForwardEmiAmount;
    // Current-month EMI(s) included along with previous arrears.
    private BigDecimal currentMonthEmiAmount;
    private Integer installmentsIncluded;
 //   private BigDecimal missedEmiCharge;
    private BigDecimal bounceCharges;
    private BigDecimal latePaymentInterest;

    private Integer overdueDays;

    private BigDecimal totalPayable;
    private BigDecimal dailyPenalty;
}