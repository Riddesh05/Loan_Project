package com.example.Loan_Project.Repository;

import com.example.Loan_Project.Entity.LoanAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanAccountRepository
        extends JpaRepository<LoanAccount, Integer> {

    Optional<LoanAccount> findByLoanAccountNoAndCustomer_CustomerId(
            String loanAccountNo,
            Integer customerId
    );
}