package com.example.Loan_Project.Repository;

import com.example.Loan_Project.Entity.PenaltyCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PenaltyChargeRepository extends JpaRepository<PenaltyCharge, Integer> {
    List<PenaltyCharge> findByLoanAccount_LoanAccountId(Integer loanAccountId);
}
