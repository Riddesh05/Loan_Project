package com.example.Loan_Project.Controller;

import com.example.Loan_Project.DTO.ApiResponse;
import com.example.Loan_Project.DTO.SanctionLetterResponse;
import com.example.Loan_Project.Entity.LoanProcessing;
import com.example.Loan_Project.Service.LoanProcessingService;
import com.example.Loan_Project.Service.SanctionLetterService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/v1/loan-processing")
public class LoanProcessingController {

    private final LoanProcessingService loanProcessingService;
    private final SanctionLetterService sanctionLetterService;

    public LoanProcessingController(LoanProcessingService loanProcessingService,
                                    SanctionLetterService sanctionLetterService) {
        this.loanProcessingService = loanProcessingService;
        this.sanctionLetterService = sanctionLetterService;
    }

    @PostMapping("/sanction/{loanApplicationId}")
    public ResponseEntity<ApiResponse<LoanProcessing>> createSanction(
            @PathVariable Long loanApplicationId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Loan sanctioned successfully",
                loanProcessingService.createSanction(loanApplicationId)));
    }

    @PostMapping("/disbursement/{loanApplicationId}")
    public ResponseEntity<ApiResponse<LoanProcessing>> disburseLoan(
            @PathVariable Long loanApplicationId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Loan disbursed successfully",
                loanProcessingService.disburseLoan(loanApplicationId)));
    }

    @GetMapping("/details/{loanApplicationId}")
    public ResponseEntity<ApiResponse<LoanProcessing>> getProcessingDetails(
            @PathVariable Long loanApplicationId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Loan processing details fetched successfully",
                loanProcessingService.getProcessingDetails(loanApplicationId)));
    }

    @PostMapping("/sanction-letter/{loanApplicationId}")
    public ResponseEntity<ApiResponse<SanctionLetterResponse>> generateSanctionLetter(
            @PathVariable Long loanApplicationId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Sanction letter processing completed",
                sanctionLetterService.generateSanctionLetter(loanApplicationId)));
    }

    // A PDF download is intentionally returned as binary data, not JSON.
    @GetMapping("/sanction-letter/{loanApplicationId}/download")
    public ResponseEntity<Resource> downloadSanctionLetter(
            @PathVariable Long loanApplicationId) throws IOException {
        Path path = sanctionLetterService.getSanctionLetterPath(loanApplicationId);
        Resource resource = new FileSystemResource(path);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(path.getFileName().toString()).build().toString())
                .body(resource);
    }
}
