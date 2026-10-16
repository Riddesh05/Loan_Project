package com.example.Loan_Project.DTO;

import com.example.Loan_Project.Entity.Customer;
import com.example.Loan_Project.Entity.LoanApplication;
import com.example.Loan_Project.Entity.LoanDeal;
import com.example.Loan_Project.Enum.DocumentVerificationStatus;
import com.example.Loan_Project.Enum.LoanApplicationStatus;
import com.example.Loan_Project.Enum.LoanType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LoanApplicationResponse {

    private Long loanApplicationId;
    private Long customerId;
    private String customerName;

    private Long loanDealId;
    private LoanType loanType;
    private BigDecimal interestRate;

    private BigDecimal requestedAmount;
    private Integer tenureMonths;
    private Integer cibilScore;

    private String bankName;
    private String bankAccountNumber;
    private String ifscCode;
    private Integer emiDay;

    private LoanApplicationStatus status;
    private DocumentVerificationStatus documentVerificationStatus;
    private LocalDateTime appliedAt;
    private String rejectionReason;

    public static LoanApplicationResponse from(
            LoanApplication application) {

        LoanApplicationResponse response =
                new LoanApplicationResponse();

        response.setLoanApplicationId(
                application.getLoanApplicationId());
        response.setRequestedAmount(
                application.getRequestedAmount());
        response.setTenureMonths(
                application.getTenureMonths());
        response.setCibilScore(
                application.getCibilScore());

        response.setBankName(application.getBankName());
        response.setBankAccountNumber(
                application.getBankAccountNumber());
        response.setIfscCode(application.getIfscCode());
        response.setEmiDay(application.getEmiDay());

        response.setStatus(application.getStatus());
        response.setDocumentVerificationStatus(
                application.getDocumentVerificationStatus());
        response.setAppliedAt(application.getAppliedAt());

        // Include the rejection reason in the API response
        response.setRejectionReason(
                application.getRejectionReason());

        Customer customer = application.getCustomer();

        if (customer != null) {
            response.setCustomerId(customer.getCustomerId());

            String firstName = customer.getFirstName() == null
                    ? ""
                    : customer.getFirstName().trim();

            String lastName = customer.getLastName() == null
                    ? ""
                    : customer.getLastName().trim();

            response.setCustomerName(
                    (firstName + " " + lastName).trim());
        }

        LoanDeal loanDeal = application.getLoanDeal();

        if (loanDeal != null) {
            response.setLoanDealId(
                    loanDeal.getLoanDealId());
            response.setLoanType(loanDeal.getLoanType());
            response.setInterestRate(
                    loanDeal.getInterestRate());
        }

        return response;
    }
}