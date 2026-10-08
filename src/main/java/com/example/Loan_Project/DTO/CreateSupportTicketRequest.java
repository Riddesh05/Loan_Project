package com.example.Loan_Project.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateSupportTicketRequest {

    @NotBlank(message = "Loan account number is required")
    private String loanAccountNumber;

    @NotBlank(message = "Description is required")
    @Size(
            max = 2000,
            message = "Description cannot exceed 2000 characters"
    )
    private String description;
}