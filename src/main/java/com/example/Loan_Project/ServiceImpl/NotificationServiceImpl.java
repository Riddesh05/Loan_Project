
package com.example.Loan_Project.ServiceImpl;


import com.example.Loan_Project.DTO.NotificationReadRequest;
import com.example.Loan_Project.DTO.NotificationResponse;
import com.example.Loan_Project.Entity.EmiSchedule;
import com.example.Loan_Project.Entity.Notification;
import com.example.Loan_Project.Repository.NotificationRepository;
import com.example.Loan_Project.Service.NotificationService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void createEmiNotification(
            EmiSchedule schedule,
            String notificationType,
            String title,
            String message) {

        boolean exists =
                notificationRepository
                        .existsByEmiSchedule_EmiScheduleIdAndNotificationType(
                                schedule.getEmiScheduleId(),
                                notificationType
                        );

        if (exists) {
            return;
        }

        Notification notification = new Notification();

        notification.setLoanAccount(schedule.getLoanAccount());
        notification.setEmiSchedule(schedule);
        notification.setNotificationType(notificationType);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long loanAccountId) {

        return notificationRepository
                .findByLoanAccount_LoanAccountIdOrderByCreatedAtDesc(
                        loanAccountId
                )
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(
            NotificationReadRequest request) {

        Notification notification = notificationRepository
                .findById(request.getNotificationId())
                .orElseThrow(() ->
                        new RuntimeException("Notification not found"));

        notification.setIsRead(true);

        Notification saved =
                notificationRepository.save(notification);

        return convertToResponse(saved);
    }

    private NotificationResponse convertToResponse(
            Notification notification) {

        NotificationResponse response = new NotificationResponse();

        response.setNotificationId(
                notification.getNotificationId());

        response.setLoanAccountId(
                notification.getLoanAccount().getLoanAccountId());

        response.setEmiScheduleId(
                notification.getEmiSchedule().getEmiScheduleId());

        response.setNotificationType(
                notification.getNotificationType());

        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());

        response.setIsRead(notification.getIsRead());

        response.setCreatedAt(notification.getCreatedAt());

        return response;
    }
}
