package com.example.Loan_Project.Service;

import com.example.Loan_Project.Entity.LoanEligibility;

public interface LoanEligibilityService {

    LoanEligibility checkEligibility(Long loanApplicationId);

    LoanEligibility getEligibility(Long loanApplicationId);
}