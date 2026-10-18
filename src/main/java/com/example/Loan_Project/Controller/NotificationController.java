
package com.example.Loan_Project.Controller;

import com.example.Loan_Project.DTO.NotificationReadRequest;
import com.example.Loan_Project.DTO.NotificationResponse;

import com.example.Loan_Project.Service.NotificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/loan-account/{loanAccountId}")
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @PathVariable Long loanAccountId) {

        List<NotificationResponse> notifications =
                notificationService.getNotifications(loanAccountId);

        return ResponseEntity.ok(notifications);
    }

    @PatchMapping("/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @Valid @RequestBody NotificationReadRequest request) {

        NotificationResponse response =
                notificationService.markAsRead(request);

        return ResponseEntity.ok(response);
    }
}
