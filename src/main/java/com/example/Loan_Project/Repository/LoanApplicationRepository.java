package com.example.Loan_Project.Repository;

import com.example.Loan_Project.Entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    List<LoanApplication> findByCustomerCustomerId(Long customerId);
}