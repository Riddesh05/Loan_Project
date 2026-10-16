package com.example.Loan_Project.Service;

public interface EmailService {

    void sendSupportTicketUpdateEmail(
            String customerEmail,
            String customerName,
            String ticketNumber,
            String status,
            String officerRemarks
    );

    void sendForeclosureUpdateEmail(
            String customerEmail,
            String customerName,
            String subject,
            String details
    );
}