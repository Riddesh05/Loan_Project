package com.example.Loan_Project.Repository;

import com.example.Loan_Project.Entity.LoanProcessing;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LoanProcessingRepository extends JpaRepository<LoanProcessing, Long> {

    Optional<LoanProcessing> findByLoanApplicationLoanApplicationId(Long loanApplicationId);
}