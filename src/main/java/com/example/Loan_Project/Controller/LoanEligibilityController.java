
package com.example.Loan_Project.Controller;

import com.example.Loan_Project.DTO.ApiResponse;
import com.example.Loan_Project.DTO.LoanEligibilityResponse;
import com.example.Loan_Project.Entity.LoanApplication;
import com.example.Loan_Project.Entity.LoanEligibility;
import com.example.Loan_Project.Service.LoanEligibilityService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loan-eligibility")
public class LoanEligibilityController {

    private final LoanEligibilityService loanEligibilityService;
    private final ModelMapper modelMapper;

    public LoanEligibilityController(
            LoanEligibilityService loanEligibilityService,
            ModelMapper modelMapper) {
        this.loanEligibilityService = loanEligibilityService;
        this.modelMapper = modelMapper;
    }

    @PostMapping("/{loanApplicationId}")
    public ResponseEntity<ApiResponse<LoanEligibilityResponse>> checkEligibility(
            @PathVariable Long loanApplicationId) {

        LoanEligibility eligibility =
                loanEligibilityService.checkEligibility(loanApplicationId);

        return ResponseEntity.ok(ApiResponse.success(
                "Loan eligibility evaluated successfully",
                mapToResponse(eligibility)
        ));
    }

    @GetMapping("/{loanApplicationId}")
    public ResponseEntity<ApiResponse<LoanEligibilityResponse>> getEligibility(
            @PathVariable Long loanApplicationId) {

        LoanEligibility eligibility =
                loanEligibilityService.getEligibility(loanApplicationId);

        return ResponseEntity.ok(ApiResponse.success(
                "Loan eligibility fetched successfully",
                mapToResponse(eligibility)
        ));
    }

    private LoanEligibilityResponse mapToResponse(
            LoanEligibility eligibility) {

        LoanEligibilityResponse response =
                modelMapper.map(eligibility, LoanEligibilityResponse.class);

        LoanApplication application = eligibility.getLoanApplication();

        response.setLoanApplicationId(application.getLoanApplicationId());
        response.setRequestedAmount(application.getRequestedAmount());
        response.setTenureMonths(application.getTenureMonths());
        response.setApplicationStatus(application.getStatus());
        response.setDocumentVerificationStatus(
                application.getDocumentVerificationStatus());

        response.setCustomerId(
                application.getCustomer().getCustomerId());

        response.setCustomerName(
                application.getCustomer().getFirstName() + " "
                        + application.getCustomer().getLastName());

        response.setCustomerEmail(
                application.getCustomer().getEmail());

        response.setLoanType(
                application.getLoanDeal().getLoanType().name());

        response.setInterestRate(
                application.getLoanDeal().getInterestRate());

        return response;
    }
}
