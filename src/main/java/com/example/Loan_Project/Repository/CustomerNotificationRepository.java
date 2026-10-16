package com.example.Loan_Project.Repository;

import com.example.Loan_Project.Entity.CustomerNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerNotificationRepository
        extends JpaRepository<CustomerNotification, Long> {

    List<CustomerNotification>
    findByCustomer_CustomerIdOrderByCreatedAtDesc(Long customerId);

    Optional<CustomerNotification>
    findByNotificationIdAndCustomer_CustomerId(
            Long notificationId,
            Long customerId
    );
}