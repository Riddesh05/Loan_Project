package com.example.Loan_Project.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponse {

    private Long notificationId;

    private Long loanAccountId;

    private Integer emiScheduleId;

    private String notificationType;

    private String title;

    private String message;

    private Boolean isRead;

    private LocalDateTime createdAt;
}
