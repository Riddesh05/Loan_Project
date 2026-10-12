package com.example.Loan_Project.Repository;

import com.example.Loan_Project.Entity.LoanDeal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanDealRepository
        extends JpaRepository<LoanDeal, Integer> {
}
