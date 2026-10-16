package com.example.Loan_Project.Repository;

import com.example.Loan_Project.Entity.EmiSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmiScheduleRepository
        extends JpaRepository<EmiSchedule, Integer> {

    List<EmiSchedule>
    findByLoanAccount_LoanAccountIdOrderByInstallmentNoAsc(
            Integer loanAccountId);
}