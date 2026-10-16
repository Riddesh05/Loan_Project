package com.example.Loan_Project.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class RazorpayCreateOrderRequest {

    @NotNull
    @Positive
    private Integer emiScheduleId;
}