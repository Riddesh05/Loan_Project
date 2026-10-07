package com.example.Loan_Project.Repository;

import com.example.Loan_Project.Entity.LoanEligibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanEligibilityRepository extends JpaRepository<LoanEligibility, Long> {

    Optional<LoanEligibility> findByLoanApplicationLoanApplicationId(Long loanApplicationId);
}