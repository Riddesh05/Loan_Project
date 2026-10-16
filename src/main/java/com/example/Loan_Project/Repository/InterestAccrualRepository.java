package com.example.Loan_Project.Repository;

import com.example.Loan_Project.Entity.InterestAccrual;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InterestAccrualRepository extends JpaRepository<InterestAccrual, Integer> {
    List<InterestAccrual> findByLoanAccount_LoanAccountId(Integer loanAccountId);
}
