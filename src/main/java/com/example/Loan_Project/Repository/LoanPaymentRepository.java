package com.example.Loan_Project.Repository;

import com.example.Loan_Project.Entity.LoanPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanPaymentRepository
        extends JpaRepository<LoanPayment, Integer> {
    boolean existsByTransactionRefNo(String transactionRefNo);
    List<LoanPayment>
    findByLoanAccount_LoanAccountIdOrderByPaymentDateDesc(
            Long loanAccountId
    );

}