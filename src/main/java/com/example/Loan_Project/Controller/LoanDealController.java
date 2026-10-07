package com.example.Loan_Project.Controller;

import com.example.Loan_Project.Entity.LoanDeal;
import com.example.Loan_Project.Entity.LoanType;
import com.example.Loan_Project.Service.LoanDealService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loan-deals")
public class LoanDealController {

    private final LoanDealService loanDealService;

    public LoanDealController(LoanDealService loanDealService) {
        this.loanDealService = loanDealService;
    }

    @PostMapping
    public ResponseEntity<LoanDeal> createLoanDeal(@RequestBody LoanDeal loanDeal) {
        return ResponseEntity.ok(loanDealService.createLoanDeal(loanDeal));
    }

    @GetMapping
    public ResponseEntity<List<LoanDeal>> getAllLoanDeals() {
        return ResponseEntity.ok(loanDealService.getAllLoanDeals());
    }

    @GetMapping("/active")
    public ResponseEntity<List<LoanDeal>> getActiveLoanDeals() {
        return ResponseEntity.ok(loanDealService.getActiveLoanDeals());
    }

    @GetMapping("/type/{loanType}")
    public ResponseEntity<List<LoanDeal>> getByLoanType(@PathVariable LoanType loanType) {
        return ResponseEntity.ok(loanDealService.getAllLoanDeals()
                .stream()
                .filter(deal -> deal.getLoanType() == loanType)
                .toList());
    }
}