package com.example.Loan_Project.Service;

import com.example.Loan_Project.DTO.LoanApplicationRequest;
import com.example.Loan_Project.Entity.LoanApplication;

import java.util.List;

public interface LoanApplicationService {

    LoanApplication applyForLoan(LoanApplicationRequest request);

    LoanApplication getApplicationById(Long applicationId);

    List<LoanApplication> getApplicationsByCustomer(Long customerId);

    List<LoanApplication> getAllApplications();
}