package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.Entity.LoanApplication;
import com.example.Loan_Project.Entity.LoanApplicationStatus;
import com.example.Loan_Project.Entity.LoanEligibility;
import com.example.Loan_Project.Repository.LoanApplicationRepository;
import com.example.Loan_Project.Repository.LoanEligibilityRepository;
import com.example.Loan_Project.Service.LoanEligibilityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class LoanEligibilityServiceImpl implements LoanEligibilityService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanEligibilityRepository loanEligibilityRepository;

    public LoanEligibilityServiceImpl(
            LoanApplicationRepository loanApplicationRepository,
            LoanEligibilityRepository loanEligibilityRepository) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanEligibilityRepository = loanEligibilityRepository;
    }

    @Override
    @Transactional
    public LoanEligibility checkEligibility(Long loanApplicationId) {

        LoanApplication application = loanApplicationRepository.findById(loanApplicationId)
                .orElseThrow(() -> new RuntimeException("Loan application not found"));

        if (loanEligibilityRepository
                .findByLoanApplicationLoanApplicationId(loanApplicationId)
                .isPresent()) {
            throw new RuntimeException("Eligibility already generated for this application");
        }

        Integer cibilScore = application.getCibilScore();

        LoanEligibility eligibility = new LoanEligibility();

        eligibility.setLoanApplication(application);
        eligibility.setCibilScore(cibilScore);
        eligibility.setEvaluatedAt(LocalDateTime.now());

        setEligibilityRange(eligibility, cibilScore);

        BigDecimal requestedAmount = application.getRequestedAmount();
        BigDecimal fromAmount = eligibility.getEligibleAmountFrom();
        BigDecimal toAmount = eligibility.getEligibleAmountTo();

        if (cibilScore < 650) {

            eligibility.setEligible(false);
            eligibility.setRemarks("Reject - CIBIL score is below 650");
            application.setStatus(LoanApplicationStatus.NOT_ELIGIBLE);

        } else if (toAmount == null && requestedAmount.compareTo(fromAmount) >= 0) {

            eligibility.setEligible(true);
            eligibility.setRemarks(
                    "Excellent - Customer is eligible for loan amount above 1 crore"
            );
            application.setStatus(LoanApplicationStatus.ELIGIBLE);

        } else if (requestedAmount.compareTo(fromAmount) >= 0
                && requestedAmount.compareTo(toAmount) <= 0) {

            eligibility.setEligible(true);
            eligibility.setRemarks(
                    "Customer is eligible for the requested loan amount"
            );
            application.setStatus(LoanApplicationStatus.ELIGIBLE);

        } else {

            eligibility.setEligible(false);
            eligibility.setRemarks(
                    "Requested amount is outside the eligible loan amount range"
            );
            application.setStatus(LoanApplicationStatus.NOT_ELIGIBLE);
        }

        loanApplicationRepository.save(application);

        return loanEligibilityRepository.save(eligibility);
    }

    private void setEligibilityRange(LoanEligibility eligibility, Integer cibilScore) {

        if (cibilScore >= 900) {

            eligibility.setEligibleAmountFrom(
                    BigDecimal.valueOf(10000000)
            );

            eligibility.setEligibleAmountTo(null);

        } else if (cibilScore >= 800) {

            eligibility.setEligibleAmountFrom(
                    BigDecimal.valueOf(7500000)
            );

            eligibility.setEligibleAmountTo(
                    BigDecimal.valueOf(10000000)
            );

        } else if (cibilScore >= 750) {

            eligibility.setEligibleAmountFrom(
                    BigDecimal.valueOf(5000000)
            );

            eligibility.setEligibleAmountTo(
                    BigDecimal.valueOf(7500000)
            );

        } else if (cibilScore >= 700) {

            eligibility.setEligibleAmountFrom(
                    BigDecimal.valueOf(2500000)
            );

            eligibility.setEligibleAmountTo(
                    BigDecimal.valueOf(5000000)
            );

        } else if (cibilScore >= 650) {

            eligibility.setEligibleAmountFrom(
                    BigDecimal.valueOf(1000000)
            );

            eligibility.setEligibleAmountTo(
                    BigDecimal.valueOf(2500000)
            );

        } else {

            eligibility.setEligibleAmountFrom(
                    BigDecimal.ZERO
            );

            eligibility.setEligibleAmountTo(
                    BigDecimal.ZERO
            );
        }
    }

    @Override
    public LoanEligibility getEligibility(Long loanApplicationId) {

        return loanEligibilityRepository
                .findByLoanApplicationLoanApplicationId(loanApplicationId)
                .orElseThrow(() -> new RuntimeException("Eligibility not found for this application"));
    }
}