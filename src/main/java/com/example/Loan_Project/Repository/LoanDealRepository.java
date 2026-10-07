package com.example.Loan_Project.Repository;

import com.example.Loan_Project.Entity.LoanDeal;
import com.example.Loan_Project.Entity.LoanType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanDealRepository extends JpaRepository<LoanDeal, Long> {

    List<LoanDeal> findByActiveTrue();

    List<LoanDeal> findByLoanType(LoanType loanType);
}