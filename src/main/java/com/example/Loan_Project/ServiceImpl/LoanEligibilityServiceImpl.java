
package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.Entity.CibilEligibilityRule;
import com.example.Loan_Project.Entity.LoanApplication;
import com.example.Loan_Project.Entity.LoanDeal;
import com.example.Loan_Project.Entity.LoanEligibility;
import com.example.Loan_Project.Enum.DocumentVerificationStatus;
import com.example.Loan_Project.Enum.LoanApplicationStatus;
import com.example.Loan_Project.Repository.CibilEligibilityRuleRepository;
import com.example.Loan_Project.Repository.LoanApplicationRepository;
import com.example.Loan_Project.Repository.LoanEligibilityRepository;
import com.example.Loan_Project.Service.LoanEligibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoanEligibilityServiceImpl implements LoanEligibilityService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanEligibilityRepository loanEligibilityRepository;
    private final CibilEligibilityRuleRepository cibilEligibilityRuleRepository;

    @Override
    @Transactional
    public LoanEligibility checkEligibility(Long loanApplicationId) {

        // 1. Find the loan application
        LoanApplication application = loanApplicationRepository
                .findById(loanApplicationId)
                .orElseThrow(() ->
                        new RuntimeException("Loan application not found"));

        // 2. Only an approved application can proceed to eligibility
        if (application.getStatus() != LoanApplicationStatus.APPROVED) {
            throw new RuntimeException(
                    "Credit Officer approval is required before eligibility check");
        }

        // 3. Ensure eligibility has not already been checked
        if (loanEligibilityRepository
                .findByLoanApplicationLoanApplicationId(loanApplicationId)
                .isPresent()) {
            throw new RuntimeException(
                    "Eligibility already checked for this application");
        }

        // 4. Ensure documents have been verified
        DocumentVerificationStatus documentStatus =
                application.getDocumentVerificationStatus();

        if (documentStatus == null
                || documentStatus == DocumentVerificationStatus.PENDING) {
            throw new RuntimeException(
                    "Document verification is pending");
        }

        if (documentStatus == DocumentVerificationStatus.REJECTED) {
            throw new RuntimeException(
                    "Document verification rejected");
        }

        if (documentStatus != DocumentVerificationStatus.VERIFIED) {
            throw new RuntimeException(
                    "Documents must be verified before eligibility check");
        }

        // 5. Validate the CIBIL score
        Integer cibilScore = application.getCibilScore();

        if (cibilScore == null || cibilScore < 0) {
            throw new RuntimeException(
                    "Valid CIBIL score is required");
        }

        // 6. Validate the requested amount
        BigDecimal requestedAmount = application.getRequestedAmount();

        if (requestedAmount == null
                || requestedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException(
                    "Requested loan amount must be greater than zero");
        }

        // 7. Find the matching active CIBIL eligibility rule
        CibilEligibilityRule rule = cibilEligibilityRuleRepository
                .findMatchingRule(cibilScore)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No active CIBIL rule found for score: "
                                        + cibilScore));

        BigDecimal eligibleAmount = rule.getEligibleAmount();

        if (eligibleAmount == null
                || eligibleAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException(
                    "Eligible amount is not configured correctly in CIBIL rules");
        }

        // 8. Validate the selected loan deal and product limit
        LoanDeal loanDeal = application.getLoanDeal();

        if (loanDeal == null) {
            throw new RuntimeException(
                    "Loan deal is not assigned to the application");
        }

        BigDecimal productMaxAmount = loanDeal.getMaxLoanAmount();

        if (productMaxAmount == null
                || productMaxAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException(
                    "Loan product maximum amount is not configured");
        }

        // 9. Create the eligibility result
        LoanEligibility eligibility = new LoanEligibility();

        eligibility.setLoanApplication(application);
        eligibility.setCibilScore(cibilScore);
        eligibility.setCibilStatus(rule.getStatus());
        eligibility.setEligibleAmount(eligibleAmount);
        eligibility.setEvaluatedAt(LocalDateTime.now());

        boolean rejectedRule =
                "REJECT".equalsIgnoreCase(rule.getStatus());

        boolean exceedsCibilLimit =
                requestedAmount.compareTo(eligibleAmount) > 0;

        boolean exceedsProductLimit =
                requestedAmount.compareTo(productMaxAmount) > 0;

        // 10. Determine eligibility and record the reason
        if (rejectedRule) {

            eligibility.setEligible(false);
            eligibility.setRemarks(
                    "Rejected because the CIBIL status is REJECT");

        } else if (exceedsCibilLimit) {

            eligibility.setEligible(false);
            eligibility.setRemarks(
                    "Requested amount exceeds the CIBIL-based eligible amount");

        } else if (exceedsProductLimit) {

            eligibility.setEligible(false);
            eligibility.setRemarks(
                    "Requested amount exceeds the selected loan product maximum");

        } else {

            eligibility.setEligible(true);
            eligibility.setRemarks(
                    "Customer satisfies the CIBIL and loan product amount limits");
        }

        // 11. Update the application status
        if (Boolean.TRUE.equals(eligibility.getEligible())) {
            application.setStatus(LoanApplicationStatus.ELIGIBLE);
        } else {
            application.setStatus(LoanApplicationStatus.NOT_ELIGIBLE);
        }

        loanApplicationRepository.save(application);

        // 12. Save and return the eligibility result
        return loanEligibilityRepository.save(eligibility);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanEligibility getEligibility(Long loanApplicationId) {

        return loanEligibilityRepository
                .findByLoanApplicationLoanApplicationId(loanApplicationId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Eligibility not found for this application"));
    }
}
