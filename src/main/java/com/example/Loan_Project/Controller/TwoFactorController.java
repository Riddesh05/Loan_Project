package com.example.Loan_Project.Controller;

import com.example.Loan_Project.DTO.TwoFactorResponse;
import com.example.Loan_Project.DTO.TwoFactorVerifyRequest;
import com.example.Loan_Project.ServiceImpl.JwtService;
import com.example.Loan_Project.ServiceImpl.TwoFactorAuthService;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/customers/2fa")
public class TwoFactorController {

    private final TwoFactorAuthService twoFactorAuthService;
    private final JwtService jwtService;

    public TwoFactorController(
            TwoFactorAuthService twoFactorAuthService,
            JwtService jwtService) {
        this.twoFactorAuthService = twoFactorAuthService;
        this.jwtService = jwtService;
    }


    @PostMapping(
            value = "/setup",
            produces = MediaType.IMAGE_PNG_VALUE
    )
    public ResponseEntity<?> setupTwoFactor(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization) {

        try {
            String email = extractCustomerEmail(authorization);

            byte[] qrImage =
                    twoFactorAuthService.setupCustomerTwoFactorImage(email);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(qrImage);

        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired 2FA pending token");
        }
    }


    @PostMapping("/verify")
    public ResponseEntity<?> verifyTwoFactor(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization,
            @RequestBody TwoFactorVerifyRequest request) {

        String email;

        try {
            email = extractCustomerEmail(authorization);
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired 2FA pending token");
        }

        try {
            twoFactorAuthService.verifyCustomerTwoFactor(
                    email,
                    request.getCode()
            );

            String accessToken = jwtService.generateAccessToken(
                    email,
                    "CUSTOMER",
                    "CUSTOMER"
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Google Authenticator verification successful",
                    "tokenType", "Bearer",
                    "token", accessToken
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid Google Authenticator code");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    private String extractCustomerEmail(String authorization) {

        if (authorization == null
                || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException(
                    "Missing 2FA pending token");
        }

        String token = authorization.substring(7);

        return jwtService.validatePendingToken(token);
    }
}