package com.example.Loan_Project.Controller;


import com.example.Loan_Project.DTO.VerifyOtpRequest;
import com.example.Loan_Project.Response.ApiResponse;
import com.example.Loan_Project.Service.OtpService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
public class OtpController {

    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }


    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<String>> sendOtp(
            @RequestParam String email) {

        otpService.generateAndStoreOtp(email);

        ApiResponse<String> response =
                new ApiResponse<>(
                        true,
                        "OTP sent successfully",
                        null
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        boolean verified = otpService.verifyOtp(
                request.getEmail(),
                request.getOtp()
        );

        if (verified) {
            ApiResponse<String> response =
                    new ApiResponse<>(
                            true,
                            "Email verified successfully",
                            null
                    );
            return ResponseEntity.ok(response);
        }

        ApiResponse<String> response =
                new ApiResponse<>(
                        false,
                        "Invalid or expired OTP",
                        null
                );

        return ResponseEntity.badRequest().body(response);
    }

}
