package com.example.Loan_Project.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResolveSupportTicketRequest {

    @NotBlank(message = "Officer remarks are required")
    @Size(
            max = 2000,
            message = "Officer remarks cannot exceed 2000 characters"
    )
    private String officerRemarks;
}