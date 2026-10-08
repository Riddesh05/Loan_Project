package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.DTO.NotificationResponse;
import com.example.Loan_Project.Entity.Notification;
import com.example.Loan_Project.Exception.ResourceNotFoundException;
import com.example.Loan_Project.Repository.CustomerRepository;
import com.example.Loan_Project.Repository.NotificationRepository;
import com.example.Loan_Project.Service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final CustomerRepository customerRepository;

    // ==========================================
    // GET CUSTOMER NOTIFICATIONS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getCustomerNotifications(
            Integer customerId,
            Pageable pageable
    ) {

        customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with ID: " + customerId
                        )
                );

        return notificationRepository
                .findByCustomer_CustomerIdOrderByCreatedAtDesc(
                        customerId,
                        pageable
                )
                .map(this::mapToResponse);
    }

    // ==========================================
    // GET UNREAD NOTIFICATIONS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUnreadNotifications(
            Integer customerId,
            Pageable pageable
    ) {

        customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with ID: " + customerId
                        )
                );

        return notificationRepository
                .findByCustomer_CustomerIdAndIsReadFalseOrderByCreatedAtDesc(
                        customerId,
                        pageable
                )
                .map(this::mapToResponse);
    }

    // ==========================================
    // MARK ONE AS READ
    // ==========================================

    @Override
    @Transactional
    public void markAsRead(Integer notificationId) {

        Notification notification =
                notificationRepository
                        .findById(notificationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Notification not found with ID: "
                                                + notificationId
                                )
                        );

        notification.setIsRead(true);

        notificationRepository.save(notification);
    }

    // ==========================================
    // MARK ALL AS READ
    // ==========================================

    @Override
    @Transactional
    public void markAllAsRead(Integer customerId) {

        customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with ID: " + customerId
                        )
                );

        Page<Notification> page =
                notificationRepository
                        .findByCustomer_CustomerIdAndIsReadFalseOrderByCreatedAtDesc(
                                customerId,
                                Pageable.unpaged()
                        );

        page.forEach(notification ->
                notification.setIsRead(true)
        );

        notificationRepository.saveAll(
                page.getContent()
        );
    }

    // ==========================================
    // ENTITY -> DTO
    // ==========================================

    private NotificationResponse mapToResponse(
            Notification notification
    ) {

        NotificationResponse response =
                new NotificationResponse();

        response.setNotificationId(
                notification.getNotificationId()
        );

        response.setMessage(
                notification.getMessage()
        );

        response.setIsRead(
                notification.getIsRead()
        );

        response.setCreatedAt(
                notification.getCreatedAt()
        );

        // Support ticket information
        if (notification.getSupportTicket() != null) {

            response.setTicketId(
                    notification
                            .getSupportTicket()
                            .getTicketId()
            );

            response.setTicketNumber(
                    notification
                            .getSupportTicket()
                            .getTicketNumber()
            );
        }

        return response;
    }
}