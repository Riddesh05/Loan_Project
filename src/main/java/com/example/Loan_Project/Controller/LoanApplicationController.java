package com.example.Loan_Project.Controller;

import com.example.Loan_Project.DTO.ApiResponse;
import com.example.Loan_Project.DTO.LoanApplicationRequest;
import com.example.Loan_Project.DTO.LoanApplicationResponse;
import com.example.Loan_Project.Entity.LoanApplication;
import com.example.Loan_Project.Enum.DocumentVerificationStatus;
import com.example.Loan_Project.Service.LoanApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.Loan_Project.DTO.EmiCalculationRequest;
import com.example.Loan_Project.DTO.EmiCalculationResponse;
import java.util.List;

@RestController
@RequestMapping("/api/v1/loan-applications")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    public LoanApplicationController(LoanApplicationService loanApplicationService) {
        this.loanApplicationService = loanApplicationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> applyForLoan(
            @RequestBody LoanApplicationRequest request) {
        LoanApplication application = loanApplicationService.applyForLoan(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Loan application created successfully",
                LoanApplicationResponse.from(application)));
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> getApplication(
            @PathVariable Long applicationId) {
        LoanApplication application = loanApplicationService.getApplicationById(applicationId);
        return ResponseEntity.ok(ApiResponse.success(
                "Loan application fetched successfully",
                LoanApplicationResponse.from(application)));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<LoanApplicationResponse>>> getByCustomer(
            @PathVariable Long customerId) {
        List<LoanApplicationResponse> responses = loanApplicationService
                .getApplicationsByCustomer(customerId)
                .stream().map(LoanApplicationResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.success(
                "Customer loan applications fetched successfully", responses));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LoanApplicationResponse>>> getAllApplications() {
        List<LoanApplicationResponse> responses = loanApplicationService.getAllApplications()
                .stream().map(LoanApplicationResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.success(
                "Loan applications fetched successfully", responses));
    }

    @PatchMapping("/{applicationId}/document-verification")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> updateDocumentVerification(
            @PathVariable Long applicationId,
            @RequestParam DocumentVerificationStatus status) {
        LoanApplication application = loanApplicationService
                .updateDocumentVerificationStatus(applicationId, status);
        return ResponseEntity.ok(ApiResponse.success(
                "Document verification status updated successfully",
                LoanApplicationResponse.from(application)));
    }

    @PostMapping("/emi-calculation")
    public ResponseEntity<ApiResponse<EmiCalculationResponse>> calculateEmi(
            @RequestBody EmiCalculationRequest request) {

        EmiCalculationResponse response =
                loanApplicationService.calculateEmi(request);

        return ResponseEntity.ok(ApiResponse.success(
                "EMI calculated successfully",
                response));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<LoanApplicationResponse>>>
    getPendingApplications() {

        List<LoanApplicationResponse> responses =
                loanApplicationService.getPendingApplications()
                        .stream()
                        .map(LoanApplicationResponse::from)
                        .toList();

        return ResponseEntity.ok(ApiResponse.success(
                "Pending applications fetched successfully",
                responses));
    }

    @PatchMapping("/{applicationId}/approve")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>>
    approveApplication(@PathVariable Long applicationId) {

        LoanApplication application =
                loanApplicationService.approveApplication(applicationId);

        return ResponseEntity.ok(ApiResponse.success(
                "Loan application approved successfully",
                LoanApplicationResponse.from(application)));
    }

    @PatchMapping("/{applicationId}/reject")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>>
    rejectApplication(
            @PathVariable Long applicationId,
            @RequestBody java.util.Map<String, String> request) {

        LoanApplication application =
                loanApplicationService.rejectApplication(
                        applicationId,
                        request.get("rejectionReason"));

        return ResponseEntity.ok(ApiResponse.success(
                "Loan application rejected successfully",
                LoanApplicationResponse.from(application)));
    }
}
