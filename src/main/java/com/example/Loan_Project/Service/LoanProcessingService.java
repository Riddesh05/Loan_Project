package com.example.Loan_Project.Service;

import com.example.Loan_Project.Entity.LoanProcessing;

public interface LoanProcessingService {

    LoanProcessing createSanction(Long loanApplicationId);

    LoanProcessing disburseLoan(Long loanApplicationId);

    LoanProcessing getProcessingDetails(Long loanApplicationId);
}