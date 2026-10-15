package com.example.Loan_Project.DTO;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor

public class EmiScheduleGenerationRequest {

    @NotNull
    private Long loanAccountId;
}
