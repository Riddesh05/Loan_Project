package com.example.Loan_Project.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewForeClosureRequest {

    @NotNull(message = "Approval decision is required")
    private Boolean approved;
}