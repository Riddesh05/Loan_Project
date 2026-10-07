package com.example.Loan_Project.Controller;


import com.example.Loan_Project.DTO.VerifyOtpRequest;
import com.example.Loan_Project.Service.OtpService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
public class OtpController {

    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }


    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(
            @RequestParam String email) {

        otpService.generateAndStoreOtp(email);

        return ResponseEntity.ok("OTP generated successfully");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        boolean verified = otpService.verifyOtp(
                request.getEmail(),
                request.getOtp()
        );

        if (verified) {
            return ResponseEntity.ok("Email verified successfully");
        }

        return ResponseEntity
                .badRequest()
                .body("Invalid or expired OTP");
    }

}
