package com.example.Loan_Project.Controller;

import com.example.Loan_Project.Entity.LoanEligibility;
import com.example.Loan_Project.Service.LoanEligibilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/loan-eligibility")
public class LoanEligibilityController {

    private final LoanEligibilityService loanEligibilityService;

    public LoanEligibilityController(LoanEligibilityService loanEligibilityService) {
        this.loanEligibilityService = loanEligibilityService;
    }

    @PostMapping("/{loanApplicationId}")
    public ResponseEntity<LoanEligibility> checkEligibility(
            @PathVariable Long loanApplicationId) {

        return ResponseEntity.ok(
                loanEligibilityService.checkEligibility(loanApplicationId)
        );
    }

    @GetMapping("/{loanApplicationId}")
    public ResponseEntity<LoanEligibility> getEligibility(
            @PathVariable Long loanApplicationId) {

        return ResponseEntity.ok(
                loanEligibilityService.getEligibility(loanApplicationId)
        );
    }
}