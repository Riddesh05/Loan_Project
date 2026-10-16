package com.example.Loan_Project.Controller;


import com.example.Loan_Project.DTO.*;
import com.example.Loan_Project.Entity.Customer;
import com.example.Loan_Project.Entity.KycDocument;
import com.example.Loan_Project.Entity.User;
import com.example.Loan_Project.Response.ApiResponse;
import com.example.Loan_Project.Service.OfficerCustomerService;
import com.example.Loan_Project.Service.OfficerService;
import com.example.Loan_Project.ServiceImpl.JwtService;
import com.example.Loan_Project.ServiceImpl.OfficerTwoFactorAuthService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/officer")
public class OfficerController {

    private final OfficerService officerService;
    private final OfficerCustomerService officerCustomerService;
    private final JwtService jwtService;
    private final OfficerTwoFactorAuthService officerTwoFactorAuthService;



    public OfficerController(OfficerService officerService, OfficerCustomerService officerCustomerService, JwtService jwtService, OfficerTwoFactorAuthService officerTwoFactorAuthService) {
        this.officerService = officerService;
        this.officerCustomerService = officerCustomerService;
        this.jwtService = jwtService;
        this.officerTwoFactorAuthService = officerTwoFactorAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<OfficerLoginResponse>> login(
            @Valid @RequestBody OfficerLoginRequest request) {

        User user = officerService.login(request);

        String pendingToken = jwtService.generatePendingToken(
                user.getEmail(),
                "OFFICER"
        );

        OfficerLoginResponse loginResponse =
                new OfficerLoginResponse(
                        user.getUserId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getRole().getRoleName(),
                        pendingToken,
                        "Bearer"
                );

        ApiResponse<OfficerLoginResponse> response =
                new ApiResponse<>(
                        true,
                        "Officer login successful",
                        loginResponse
                );

        return ResponseEntity.ok(response);
    }


    @PostMapping(
            value = "/2fa/setup",
            produces = MediaType.IMAGE_PNG_VALUE
    )
    public ResponseEntity<byte[]> setupOfficerTwoFactor(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);

        String email =
                jwtService.validateOfficerPendingToken(token);

        byte[] qrCode =
                officerTwoFactorAuthService
                        .setupOfficerTwoFactorImage(email);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrCode);
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<ApiResponse<OfficerLoginResponse>>
    verifyOfficerTwoFactor(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String code) {

        String token = authHeader.substring(7);

        String email =
                jwtService.validateOfficerPendingToken(token);

        officerTwoFactorAuthService
                .verifyOfficerTwoFactor(email, code);

        User officer = officerService.getOfficerByEmail(email);

        String accessToken = jwtService.generateAccessToken(
                officer.getEmail(),
                "OFFICER",
                officer.getRole().getRoleName()
        );

        OfficerLoginResponse loginResponse =
                new OfficerLoginResponse(
                        officer.getUserId(),
                        officer.getFirstName(),
                        officer.getLastName(),
                        officer.getEmail(),
                        officer.getRole().getRoleName(),
                        accessToken,
                        "Bearer"
                );

        ApiResponse<OfficerLoginResponse> response =
                new ApiResponse<>(
                        true,
                        "Officer 2FA verified successfully",
                        loginResponse
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<List<OfficerCustomerResponse>>> getAllCustomers() {

        List<OfficerCustomerResponse> customers =
                officerCustomerService.getAllCustomers();

        ApiResponse<List<OfficerCustomerResponse>> response =
                new ApiResponse<>(
                        true,
                        "Customers fetched successfully",
                        customers
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/customers/{customerId}/documents")
    public ResponseEntity<ApiResponse<List<OfficerKycDocumentResponse>>> getCustomerDocuments(
            @PathVariable Long customerId) {

        List<OfficerKycDocumentResponse> documents =
                officerCustomerService.getCustomerDocuments(customerId);

        ApiResponse<List<OfficerKycDocumentResponse>> response =
                new ApiResponse<>(
                        true,
                        "Customer documents fetched successfully",
                        documents
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/documents/{documentId}/file")
    public ResponseEntity<byte[]> getDocumentFile(
            @PathVariable Long documentId) {

        byte[] file = officerCustomerService.getDocumentFile(documentId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
    }


    @PutMapping("/documents/{documentId}/verify")
    public ResponseEntity<ApiResponse<String>> verifyDocument(
            @PathVariable Long documentId) {

        officerCustomerService.verifyDocument(documentId);

        ApiResponse<String> response =
                new ApiResponse<>(
                        true,
                        "Document verified successfully",
                        null
                );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/documents/{documentId}/reject")
    public ResponseEntity<ApiResponse<String>> rejectDocument(
            @PathVariable Long documentId,
            @Valid @RequestBody RejectDocumentRequest request) {

        officerCustomerService.rejectDocument(
                documentId,
                request.getRemark()
        );

        ApiResponse<String> response =
                new ApiResponse<>(
                        true,
                        "Document rejected successfully",
                        null
                );

        return ResponseEntity.ok(response);
    }


}