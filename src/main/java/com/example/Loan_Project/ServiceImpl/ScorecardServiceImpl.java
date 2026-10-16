
package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.Entity.LoanApplication;
import com.example.Loan_Project.Entity.LoanEligibility;
import com.example.Loan_Project.Entity.Scorecard;
import com.example.Loan_Project.Enum.LoanApplicationStatus;
import com.example.Loan_Project.Enum.ScorecardStatus;
import com.example.Loan_Project.Repository.LoanApplicationRepository;
import com.example.Loan_Project.Repository.LoanEligibilityRepository;
import com.example.Loan_Project.Repository.ScorecardRepository;
import com.example.Loan_Project.Service.ScorecardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ScorecardServiceImpl implements ScorecardService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanEligibilityRepository loanEligibilityRepository;
    private final ScorecardRepository scorecardRepository;

    @Override
    @Transactional
    public Scorecard generateScorecard(Long loanApplicationId) {

        LoanApplication application = loanApplicationRepository
                .findById(loanApplicationId)
                .orElseThrow(() ->
                        new RuntimeException("Loan application not found"));

        LoanEligibility eligibility = loanEligibilityRepository
                .findByLoanApplicationLoanApplicationId(loanApplicationId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Loan eligibility not found. Check eligibility first."));

        if (scorecardRepository
                .findByLoanApplicationLoanApplicationId(loanApplicationId)
                .isPresent()) {
            throw new RuntimeException(
                    "Scorecard already generated for this application");
        }

        Scorecard scorecard = new Scorecard();

        scorecard.setLoanApplication(application);
        scorecard.setCibilScore(eligibility.getCibilScore());

        scorecard.setEmploymentType(
                application.getCustomer()
                        .getEmploymentType()
                        .name()
        );

        scorecard.setLoanType(
                application.getLoanDeal()
                        .getLoanType()
                        .name()
        );

        scorecard.setRequestedAmount(
                application.getRequestedAmount()
        );

        scorecard.setEligibleAmount(
                eligibility.getEligibleAmount()
        );

        scorecard.setGeneratedAt(LocalDateTime.now());

        if (Boolean.TRUE.equals(eligibility.getEligible())) {

            scorecard.setStatus(ScorecardStatus.APPROVED);
            scorecard.setRemarks(
                    "Scorecard approved based on loan eligibility");

            application.setStatus(LoanApplicationStatus.ELIGIBLE);

        } else {

            scorecard.setStatus(ScorecardStatus.REJECTED);
            scorecard.setRemarks(
                    "Scorecard rejected because loan eligibility was not approved");

            application.setStatus(LoanApplicationStatus.NOT_ELIGIBLE);
        }

        loanApplicationRepository.save(application);

        return scorecardRepository.save(scorecard);
    }

    @Override
    @Transactional(readOnly = true)
    public Scorecard getScorecard(Long loanApplicationId) {

        return scorecardRepository
                .findByLoanApplicationLoanApplicationId(loanApplicationId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Scorecard not found for this application"));
    }
}
