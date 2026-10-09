package com.example.Loan_Project.Controller;

import com.example.Loan_Project.DTO.KycDocumentUploadRequest;
import com.example.Loan_Project.Entity.DocumentType;
import com.example.Loan_Project.Entity.KycDocument;
import com.example.Loan_Project.Response.ApiResponse;
import com.example.Loan_Project.Service.KycDocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/kyc")
public class KycDocumentController {

    private final KycDocumentService kycDocumentService;

    public KycDocumentController(KycDocumentService kycDocumentService) {
        this.kycDocumentService = kycDocumentService;
    }


    @PostMapping("/documents")
    public ResponseEntity<ApiResponse<KycDocument>> uploadDocument(
            @RequestParam Long customerId,
            @RequestParam String documentType,
            @RequestParam MultipartFile file) {

        KycDocumentUploadRequest request =
                new KycDocumentUploadRequest();

        request.setCustomerId(customerId);
        request.setDocumentType(
                DocumentType.valueOf(documentType)
        );
        request.setFile(file);

        KycDocument document =
                kycDocumentService.uploadDocument(request);

        ApiResponse<KycDocument> response =
                new ApiResponse<>(
                        true,
                        "Document uploaded successfully",
                        document
                );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/documents/{customerId}")
    public ResponseEntity<ApiResponse<List<KycDocument>>> getDocuments(
            @PathVariable Long customerId) {

        List<KycDocument> documents =
                kycDocumentService.getDocumentsByCustomerId(customerId);

        ApiResponse<List<KycDocument>> response =
                new ApiResponse<>(
                        true,
                        "Documents fetched successfully",
                        documents
                );

        return ResponseEntity.ok(response);
    }

}
