package com.example.Loan_Project.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentVerifyRequest {

    @NotNull
    private Integer loanAccountId;

    @NotNull
    private Integer emiScheduleId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amountPaid;

    @NotBlank
    private String transactionRefNo;

    @NotBlank
    private String paymentMode;
}