package com.example.Loan_Project.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class NotificationReadRequest {

    @NotNull(message = "Notification ID is required")
    @Positive(message = "Notification ID must be positive")
    private Long notificationId;
}
