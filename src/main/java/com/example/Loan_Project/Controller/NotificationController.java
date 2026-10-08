package com.example.Loan_Project.Controller;

import com.example.Loan_Project.DTO.NotificationResponse;
import com.example.Loan_Project.Respsonse.ApiResponse;
import com.example.Loan_Project.Service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // ==========================================
    // GET ALL CUSTOMER NOTIFICATIONS
    // ==========================================

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>>
    getCustomerNotifications(

            @PathVariable Integer customerId,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        Page<NotificationResponse> response =
                notificationService.getCustomerNotifications(
                        customerId,
                        pageable
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Notifications fetched successfully",
                        response
                )
        );
    }

    // ==========================================
    // GET UNREAD NOTIFICATIONS
    // ==========================================

    @GetMapping("/customer/{customerId}/unread")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>>
    getUnreadNotifications(

            @PathVariable Integer customerId,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        Page<NotificationResponse> response =
                notificationService.getUnreadNotifications(
                        customerId,
                        pageable
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Unread notifications fetched successfully",
                        response
                )
        );
    }

    // ==========================================
    // MARK ONE AS READ
    // ==========================================

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>>
    markAsRead(
            @PathVariable Integer notificationId
    ) {

        notificationService.markAsRead(
                notificationId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Notification marked as read",
                        null
                )
        );
    }

    // ==========================================
    // MARK ALL AS READ
    // ==========================================

    @PutMapping("/customer/{customerId}/read-all")
    public ResponseEntity<ApiResponse<Void>>
    markAllAsRead(
            @PathVariable Integer customerId
    ) {

        notificationService.markAllAsRead(
                customerId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "All notifications marked as read",
                        null
                )
        );
    }
}