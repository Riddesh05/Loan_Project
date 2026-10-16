package com.example.Loan_Project.Repository;

import com.example.Loan_Project.Entity.Scorecard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScorecardRepository extends JpaRepository<Scorecard, Long> {

    Optional<Scorecard> findByLoanApplicationLoanApplicationId(Long loanApplicationId);
}