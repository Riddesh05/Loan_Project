package com.example.Loan_Project.Service;

import com.example.Loan_Project.DTO.CreateSupportTicketRequest;
import com.example.Loan_Project.DTO.ResolveSupportTicketRequest;
import com.example.Loan_Project.DTO.SupportTicketResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupportService {

    SupportTicketResponse createTicket(
            Integer customerId,
            CreateSupportTicketRequest request
    );

    Page<SupportTicketResponse> getCustomerTickets(
            Integer customerId,
            Pageable pageable
    );

    Page<SupportTicketResponse> getOfficerTickets(
            Pageable pageable
    );

    SupportTicketResponse getTicket(
            String ticketNumber
    );

    SupportTicketResponse resolveTicket(
            String ticketNumber,
            ResolveSupportTicketRequest request
    );
}