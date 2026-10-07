package com.example.Loan_Project.Controller;

import com.example.Loan_Project.DTO.LoanApplicationRequest;
import com.example.Loan_Project.Entity.LoanApplication;
import com.example.Loan_Project.Service.LoanApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loan-applications")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    public LoanApplicationController(LoanApplicationService loanApplicationService) {
        this.loanApplicationService = loanApplicationService;
    }

    @PostMapping
    public ResponseEntity<LoanApplication> applyForLoan(
            @RequestBody LoanApplicationRequest request) {

        return ResponseEntity.ok(
                loanApplicationService.applyForLoan(request)
        );
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<LoanApplication> getApplication(
            @PathVariable Long applicationId) {

        return ResponseEntity.ok(
                loanApplicationService.getApplicationById(applicationId)
        );
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<LoanApplication>> getByCustomer(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                loanApplicationService.getApplicationsByCustomer(customerId)
        );
    }

    @GetMapping
    public ResponseEntity<List<LoanApplication>> getAllApplications() {

        return ResponseEntity.ok(
                loanApplicationService.getAllApplications()
        );
    }
}