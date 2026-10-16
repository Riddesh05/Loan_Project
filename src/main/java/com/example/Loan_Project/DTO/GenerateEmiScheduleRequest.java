package com.example.Loan_Project.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerateEmiScheduleRequest {

    @NotNull(message = "Loan Account ID is required")
    private Long loanAccountId;


}