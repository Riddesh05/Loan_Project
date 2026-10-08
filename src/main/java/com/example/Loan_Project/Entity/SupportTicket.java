package com.example.Loan_Project.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "SupportTickets")
@Data
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TicketId")
    private Integer ticketId;

    @Column(name = "TicketNumber", unique = true, nullable = false)
    private String ticketNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerId", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LoanAccountId")
    private LoanAccount loanAccount;

    @Column(name = "Subject")
    private String subject;

    @Column(name = "Description", length = 2000)
    private String description;

    @Column(name = "OfficerRemarks", length = 2000)
    private String officerRemarks;

    @Column(name = "Status", length = 50)
    private String status;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "ResolvedAt")
    private LocalDateTime resolvedAt;
}