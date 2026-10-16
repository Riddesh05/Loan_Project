
package com.example.Loan_Project.Service;

import com.example.Loan_Project.DTO.EmiCalculationRequest;
import com.example.Loan_Project.DTO.EmiCalculationResponse;
import com.example.Loan_Project.DTO.LoanApplicationRequest;
import com.example.Loan_Project.Entity.LoanApplication;
import com.example.Loan_Project.Enum.DocumentVerificationStatus;

import java.util.List;

public interface LoanApplicationService {

    LoanApplication applyForLoan(LoanApplicationRequest request);

    EmiCalculationResponse calculateEmi(EmiCalculationRequest request);

    LoanApplication getApplicationById(Long applicationId);

    List<LoanApplication> getApplicationsByCustomer(Long customerId);

    List<LoanApplication> getAllApplications();

    List<LoanApplication> getPendingApplications();

    LoanApplication approveApplication(Long applicationId);

    LoanApplication rejectApplication(
            Long applicationId,
            String rejectionReason);

    LoanApplication updateDocumentVerificationStatus(
            Long applicationId,
            DocumentVerificationStatus status);
}
