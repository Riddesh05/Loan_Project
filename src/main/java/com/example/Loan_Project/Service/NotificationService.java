package com.example.Loan_Project.Service;

import com.example.Loan_Project.DTO.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    Page<NotificationResponse> getCustomerNotifications(
            Integer customerId,
            Pageable pageable
    );

    Page<NotificationResponse> getUnreadNotifications(
            Integer customerId,
            Pageable pageable
    );

    void markAsRead(Integer notificationId);

    void markAllAsRead(Integer customerId);
}