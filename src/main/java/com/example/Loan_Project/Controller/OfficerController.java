package com.example.Loan_Project.Controller;


import com.example.Loan_Project.DTO.*;
import com.example.Loan_Project.Entity.Customer;
import com.example.Loan_Project.Entity.KycDocument;
import com.example.Loan_Project.Entity.User;
import com.example.Loan_Project.Response.ApiResponse;
import com.example.Loan_Project.Service.OfficerCustomerService;
import com.example.Loan_Project.Service.OfficerService;
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

    public OfficerController(OfficerService officerService, OfficerCustomerService officerCustomerService) {
        this.officerService = officerService;
        this.officerCustomerService = officerCustomerService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<OfficerLoginResponse>> login(
            @Valid @RequestBody OfficerLoginRequest request) {

        User user = officerService.login(request);

        OfficerLoginResponse loginResponse =
                new OfficerLoginResponse(
                        user.getUserId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getRole().getRoleName()
                );

        ApiResponse<OfficerLoginResponse> response =
                new ApiResponse<>(
                        true,
                        "Officer login successful",
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