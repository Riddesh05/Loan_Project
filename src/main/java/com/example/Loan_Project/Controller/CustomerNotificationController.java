package com.example.Loan_Project.Controller;

import com.example.Loan_Project.DTO.ApiResponse;
import com.example.Loan_Project.Entity.CustomerNotification;
import com.example.Loan_Project.Service.CustomerNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class CustomerNotificationController {

    private final CustomerNotificationService notificationService;

    public CustomerNotificationController(CustomerNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<CustomerNotification>>> getNotifications(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Customer notifications fetched successfully",
                notificationService.getCustomerNotifications(customerId)));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<CustomerNotification>> markAsRead(
            @PathVariable Long notificationId,
            @RequestParam Long customerId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Notification marked as read successfully",
                notificationService.markAsRead(customerId, notificationId)));
    }
}
