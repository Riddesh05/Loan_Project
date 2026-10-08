package com.example.Loan_Project.Repository;

import com.example.Loan_Project.Entity.SupportTicket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupportTicketRepository
        extends JpaRepository<SupportTicket, Integer> {

    Optional<SupportTicket> findByTicketNumber(
            String ticketNumber
    );

    Page<SupportTicket> findByCustomer_CustomerIdOrderByCreatedAtDesc(
            Integer customerId,
            Pageable pageable
    );

    Page<SupportTicket> findByStatusOrderByCreatedAtAsc(
            String status,
            Pageable pageable
    );

    Page<SupportTicket> findByLoanAccount_LoanAccountNoOrderByCreatedAtDesc(
            String loanAccountNumber,
            Pageable pageable
    );
}