package com.example.Loan_Project.Controller;

import com.example.Loan_Project.DTO.ApiResponse;
import com.example.Loan_Project.Entity.LoanDeal;
import com.example.Loan_Project.Enum.LoanType;
import com.example.Loan_Project.Service.LoanDealService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loan-deals")
public class LoanDealController {

    private final LoanDealService loanDealService;

    public LoanDealController(LoanDealService loanDealService) {
        this.loanDealService = loanDealService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LoanDeal>> createLoanDeal(@RequestBody LoanDeal loanDeal) {
        return ResponseEntity.ok(ApiResponse.success(
                "Loan deal created successfully", loanDealService.createLoanDeal(loanDeal)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LoanDeal>>> getAllLoanDeals() {
        return ResponseEntity.ok(ApiResponse.success(
                "Loan deals fetched successfully", loanDealService.getAllLoanDeals()));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<LoanDeal>>> getActiveLoanDeals() {
        return ResponseEntity.ok(ApiResponse.success(
                "Active loan deals fetched successfully", loanDealService.getActiveLoanDeals()));
    }

    @GetMapping("/type/{loanType}")
    public ResponseEntity<ApiResponse<List<LoanDeal>>> getByLoanType(@PathVariable LoanType loanType) {
        List<LoanDeal> deals = loanDealService.getAllLoanDeals().stream()
                .filter(deal -> deal.getLoanType() == loanType).toList();
        return ResponseEntity.ok(ApiResponse.success(
                "Loan deals fetched by type successfully", deals));
    }
}
