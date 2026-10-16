package com.example.Loan_Project.Repository;

import com.example.Loan_Project.Entity.ForeClosureRequest;
import com.example.Loan_Project.Entity.ForeClosureStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForeClosureRepository
        extends JpaRepository<ForeClosureRequest, Integer> {

    List<ForeClosureRequest>
    findByLoanAccount_Customer_CustomerIdOrderByRequestedDateDesc(
            Integer customerId);

    List<ForeClosureRequest>
    findByStatusOrderByRequestedDateAsc(
            ForeClosureStatus status);

    List<ForeClosureRequest>
    findAllByOrderByRequestedDateDesc();

    boolean existsByLoanAccount_LoanAccountIdAndStatusIn(
            Integer loanAccountId,
            List<ForeClosureStatus> statuses);
}