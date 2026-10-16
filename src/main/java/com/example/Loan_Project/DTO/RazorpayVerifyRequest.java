package com.example.Loan_Project.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class RazorpayVerifyRequest {

    @NotNull
    @Positive
    private Long loanAccountId;

    @NotNull
    @Positive
    private Integer emiScheduleId;

    @NotBlank
    private String razorpayOrderId;

    @NotBlank
    private String razorpayPaymentId;

    @NotBlank
    private String razorpaySignature;
}