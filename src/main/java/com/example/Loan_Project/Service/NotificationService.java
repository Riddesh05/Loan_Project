
package com.example.Loan_Project.Service;

import com.example.Loan_Project.DTO.NotificationReadRequest;
import com.example.Loan_Project.DTO.NotificationResponse;
import com.example.Loan_Project.Entity.EmiSchedule;

import java.util.List;

public interface NotificationService {

    void createEmiNotification(
            EmiSchedule schedule,
            String notificationType,
            String title,
            String message
    );

    List<NotificationResponse> getNotifications(Long loanAccountId);

    NotificationResponse markAsRead(NotificationReadRequest request);
}
