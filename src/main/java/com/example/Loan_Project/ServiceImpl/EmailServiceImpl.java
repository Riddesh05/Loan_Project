package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.Service.EmailService;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl
        implements EmailService {

    private final JavaMailSender mailSender;

    public EmailServiceImpl(
            JavaMailSender mailSender
    ) {
        this.mailSender = mailSender;
    }

    @Async
    @Override
    public void sendSupportTicketUpdateEmail(
            String customerEmail,
            String customerName,
            String ticketNumber,
            String status,
            String officerRemarks
    ) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(customerEmail);

        message.setSubject(
                "Support Ticket Update - "
                        + ticketNumber
        );

        String emailBody =
                "Dear "
                        + customerName
                        + ",\n\n"

                        + "Your support ticket has been updated."
                        + "\n\n"

                        + "Ticket Number: "
                        + ticketNumber
                        + "\n"

                        + "Status: "
                        + status
                        + "\n\n"

                        + "Officer Remarks:"
                        + "\n"
                        + officerRemarks
                        + "\n\n"

                        + "Please log in to your account "
                        + "to view the latest update."
                        + "\n\n"

                        + "Regards,"
                        + "\n"
                        + "Loan Management System";

        message.setText(emailBody);

        mailSender.send(message);
    }
    @Async
    @Override
    public void sendForeclosureUpdateEmail(
            String customerEmail,
            String customerName,
            String subject,
            String details
    ) {
        if (customerEmail == null || customerEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "Customer email is required"
            );
        }

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(customerEmail);
        message.setSubject(subject);

        message.setText(
                "Dear " + customerName + ",\n\n"
                        + details + "\n\n"
                        + "Please log in to your Loan Management System "
                        + "to view your foreclosure details.\n\n"
                        + "Regards,\n"
                        + "Loan Management System"
        );

        mailSender.send(message);
    }
}