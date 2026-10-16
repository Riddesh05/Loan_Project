package com.example.Loan_Project.Service;

import com.example.Loan_Project.Entity.Scorecard;

public interface ScorecardService {

    Scorecard generateScorecard(Long loanApplicationId);

    Scorecard getScorecard(Long loanApplicationId);
}