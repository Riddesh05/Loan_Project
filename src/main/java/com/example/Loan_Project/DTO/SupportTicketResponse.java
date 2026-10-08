package com.example.Loan_Project.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupportTicketResponse {

    private Integer ticketId;

    private String ticketNumber;

    private Integer customerId;

    private String customerName;

    private String customerEmail;

    private String loanAccountNumber;

    private String description;

    private String officerRemarks;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime resolvedAt;
}