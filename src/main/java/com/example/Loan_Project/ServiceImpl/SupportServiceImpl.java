package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.DTO.CreateSupportTicketRequest;
import com.example.Loan_Project.DTO.ResolveSupportTicketRequest;
import com.example.Loan_Project.DTO.SupportTicketResponse;

import com.example.Loan_Project.Entity.Customer;
import com.example.Loan_Project.Entity.LoanAccount;
import com.example.Loan_Project.Entity.Notification;
import com.example.Loan_Project.Entity.SupportTicket;

import com.example.Loan_Project.Exception.ResourceNotFoundException;

import com.example.Loan_Project.Repository.CustomerRepository;
import com.example.Loan_Project.Repository.LoanAccountRepository;
import com.example.Loan_Project.Repository.NotificationRepository;
import com.example.Loan_Project.Repository.SupportTicketRepository;

import com.example.Loan_Project.Service.EmailService;
import com.example.Loan_Project.Service.SupportService;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class SupportServiceImpl implements SupportService {

    private final SupportTicketRepository supportTicketRepository;
    private final CustomerRepository customerRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public SupportServiceImpl(
            SupportTicketRepository supportTicketRepository,
            CustomerRepository customerRepository,
            LoanAccountRepository loanAccountRepository,
            NotificationRepository notificationRepository,
            EmailService emailService
    ) {
        this.supportTicketRepository = supportTicketRepository;
        this.customerRepository = customerRepository;
        this.loanAccountRepository = loanAccountRepository;
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    // ==========================================
    // CUSTOMER - CREATE TICKET
    // ==========================================

    @Override
    public SupportTicketResponse createTicket(
            Integer customerId,
            CreateSupportTicketRequest request
    ) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with ID: " + customerId
                        )
                );

        LoanAccount loanAccount =
                loanAccountRepository
                        .findByLoanAccountNoAndCustomer_CustomerId(
                                request.getLoanAccountNumber(),
                                customerId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan account not found for this customer"
                                )
                        );

        SupportTicket ticket = new SupportTicket();

        ticket.setTicketNumber(generateTicketNumber());

        ticket.setCustomer(customer);

        ticket.setLoanAccount(loanAccount);

        ticket.setDescription(
                request.getDescription()
        );

        ticket.setStatus("OPEN");

        ticket.setCreatedAt(
                LocalDateTime.now()
        );

        ticket.setUpdatedAt(
                LocalDateTime.now()
        );

        SupportTicket savedTicket =
                supportTicketRepository.save(ticket);

        return convertToResponse(savedTicket);
    }

    // ==========================================
    // CUSTOMER - GET TICKETS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> getCustomerTickets(
            Integer customerId,
            Pageable pageable
    ) {

        customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with ID: " + customerId
                        )
                );

        return supportTicketRepository
                .findByCustomer_CustomerIdOrderByCreatedAtDesc(
                        customerId,
                        pageable
                )
                .map(this::convertToResponse);
    }

    // ==========================================
    // CO - GET OPEN TICKETS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> getOfficerTickets(
            Pageable pageable
    ) {

        return supportTicketRepository
                .findByStatusOrderByCreatedAtAsc(
                        "OPEN",
                        pageable
                )
                .map(this::convertToResponse);
    }

    // ==========================================
    // CO - GET ONE TICKET
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = "supportTickets",
            key = "#ticketNumber"
    )
    public SupportTicketResponse getTicket(
            String ticketNumber
    ) {

        SupportTicket ticket =
                supportTicketRepository
                        .findByTicketNumber(ticketNumber)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Support ticket not found: "
                                                + ticketNumber
                                )
                        );

        return convertToResponse(ticket);
    }

    // ==========================================
    // CO - RESOLVE TICKET
    // ==========================================

    @Override
    @Transactional
    @CacheEvict(
            value = "supportTickets",
            key = "#ticketNumber"
    )
    public SupportTicketResponse resolveTicket(
            String ticketNumber,
            ResolveSupportTicketRequest request
    ) {

        SupportTicket ticket =
                supportTicketRepository
                        .findByTicketNumber(ticketNumber)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Support ticket not found: "
                                                + ticketNumber
                                )
                        );

        // Prevent duplicate resolution

        if ("RESOLVED".equalsIgnoreCase(ticket.getStatus())) {

            throw new IllegalArgumentException(
                    "Support ticket is already resolved"
            );
        }

        // Update ticket

        ticket.setOfficerRemarks(
                request.getOfficerRemarks()
        );

        ticket.setStatus("RESOLVED");

        ticket.setResolvedAt(
                LocalDateTime.now()
        );

        ticket.setUpdatedAt(
                LocalDateTime.now()
        );

        SupportTicket savedTicket =
                supportTicketRepository.save(ticket);

        // ==========================================
        // CREATE WEBSITE NOTIFICATION
        // ==========================================

        Notification notification =
                new Notification();

        notification.setCustomer(
                savedTicket.getCustomer()
        );

        notification.setSupportTicket(
                savedTicket
        );

        notification.setMessage(
                "Your support ticket "
                        + savedTicket.getTicketNumber()
                        + " has been resolved. "
                        + "Officer Remarks: "
                        + savedTicket.getOfficerRemarks()
        );

        notification.setIsRead(false);

        notification.setCreatedAt(
                LocalDateTime.now()
        );

        notificationRepository.save(notification);

        // ==========================================
        // SEND EMAIL ASYNCHRONOUSLY
        // ==========================================

        Customer customer =
                savedTicket.getCustomer();

        emailService.sendSupportTicketUpdateEmail(
                customer.getEmail(),
                customer.getFirstName(),
                savedTicket.getTicketNumber(),
                savedTicket.getStatus(),
                savedTicket.getOfficerRemarks()
        );

        return convertToResponse(savedTicket);
    }

    // ==========================================
    // GENERATE TICKET NUMBER
    // ==========================================

    private String generateTicketNumber() {

        String ticketNumber;

        do {

            ticketNumber =
                    "TICK-" +
                            UUID.randomUUID()
                                    .toString()
                                    .substring(0, 8)
                                    .toUpperCase();

        } while (
                supportTicketRepository
                        .findByTicketNumber(ticketNumber)
                        .isPresent()
        );

        return ticketNumber;
    }

    // ==========================================
    // ENTITY -> RESPONSE DTO
    // ==========================================

    private SupportTicketResponse convertToResponse(
            SupportTicket ticket
    ) {

        SupportTicketResponse response =
                new SupportTicketResponse();

        response.setTicketId(
                ticket.getTicketId()
        );

        response.setTicketNumber(
                ticket.getTicketNumber()
        );

        if (ticket.getCustomer() != null) {

            Customer customer =
                    ticket.getCustomer();

            response.setCustomerId(
                    customer.getCustomerId()
            );

            response.setCustomerName(
                    customer.getFirstName()
                            + " "
                            + (
                            customer.getLastName() == null
                                    ? ""
                                    : customer.getLastName()
                    )
            );

            response.setCustomerEmail(
                    customer.getEmail()
            );
        }

        if (ticket.getLoanAccount() != null) {

            response.setLoanAccountNumber(
                    ticket.getLoanAccount()
                            .getLoanAccountNo()
            );
        }

        response.setDescription(
                ticket.getDescription()
        );

        response.setOfficerRemarks(
                ticket.getOfficerRemarks()
        );

        response.setStatus(
                ticket.getStatus()
        );

        response.setCreatedAt(
                ticket.getCreatedAt()
        );

        response.setUpdatedAt(
                ticket.getUpdatedAt()
        );

        response.setResolvedAt(
                ticket.getResolvedAt()
        );

        return response;
    }
}