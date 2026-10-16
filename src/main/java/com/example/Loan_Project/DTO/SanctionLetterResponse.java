package com.example.Loan_Project.DTO;

public record SanctionLetterResponse(
        Long loanApplicationId,
        String sanctionLetterNumber,
        String downloadUrl,
        boolean notificationCreated,
        boolean emailSent,
        String message
) {
}