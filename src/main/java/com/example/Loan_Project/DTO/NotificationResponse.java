package com.example.Loan_Project.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponse {

    private Integer notificationId;

    private Integer ticketId;

    private String ticketNumber;

    private String message;

    private Boolean isRead;

    private LocalDateTime createdAt;
}