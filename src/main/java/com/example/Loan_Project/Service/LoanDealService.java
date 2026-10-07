package com.example.Loan_Project.Service;

import com.example.Loan_Project.Entity.LoanDeal;

import java.util.List;

public interface LoanDealService {

    LoanDeal createLoanDeal(LoanDeal loanDeal);

    List<LoanDeal> getAllLoanDeals();

    List<LoanDeal> getActiveLoanDeals();
}