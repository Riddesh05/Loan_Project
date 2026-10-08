package com.example.Loan_Project.Repository;

import com.example.Loan_Project.Entity.Notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository
        extends JpaRepository<Notification, Integer> {

    Page<Notification> findByCustomer_CustomerIdOrderByCreatedAtDesc(
            Integer customerId,
            Pageable pageable
    );

    Page<Notification> findByCustomer_CustomerIdAndIsReadFalseOrderByCreatedAtDesc(
            Integer customerId,
            Pageable pageable
    );
}