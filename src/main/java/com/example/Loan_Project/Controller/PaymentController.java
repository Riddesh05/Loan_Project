package com.example.Loan_Project.Controller;

import com.example.Loan_Project.DTO.PaymentBreakdownResponse;
import com.example.Loan_Project.DTO.PaymentVerifyRequest;
import com.example.Loan_Project.Service.EmiScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final EmiScheduleService emiScheduleService;

    @GetMapping("/breakdown/{emiScheduleId}")
    public ResponseEntity<PaymentBreakdownResponse> getPaymentBreakdown(
            @PathVariable Integer emiScheduleId) {

        return ResponseEntity.ok(
                emiScheduleService.getPaymentBreakdown(emiScheduleId)
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(
            @Valid @RequestBody PaymentVerifyRequest request) {

        emiScheduleService.verifyPayment(request);

        return ResponseEntity.ok(
                "Payment verified successfully"
        );
    }
}