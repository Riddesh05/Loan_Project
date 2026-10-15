package com.example.Loan_Project.Controller;

import com.example.Loan_Project.DTO.RazorpayCreateOrderRequest;
import com.example.Loan_Project.DTO.RazorpayCreateOrderResponse;
import com.example.Loan_Project.DTO.RazorpayVerifyRequest;
import com.example.Loan_Project.Service.RazorpayPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment/razorpay")
@RequiredArgsConstructor
public class RazorpayPaymentController {

    private final RazorpayPaymentService razorpayPaymentService;

    @PostMapping("/create-order")
    public ResponseEntity<RazorpayCreateOrderResponse> createOrder(
            @Valid @RequestBody RazorpayCreateOrderRequest request) {

        return ResponseEntity.ok(
                razorpayPaymentService.createOrder(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyPayment(
            @Valid @RequestBody RazorpayVerifyRequest request) {

        razorpayPaymentService.verifyPayment(request);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Razorpay payment verified and EMI settled"
        ));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getOrderDetails(
            @PathVariable String orderId) {
        return ResponseEntity.ok(
                razorpayPaymentService.getOrderDetails(orderId)
        );
    }
}