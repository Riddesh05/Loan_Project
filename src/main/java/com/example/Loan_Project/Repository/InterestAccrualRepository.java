package com.example.Loan_Project.Repository;

import com.example.Loan_Project.Entity.InterestAccrual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface InterestAccrualRepository
        extends JpaRepository<InterestAccrual, Integer> {

    @Query("""
       SELECT i
       FROM InterestAccrual i
       WHERE i.loanAccount.loanAccountId = :accountId
       AND i.installmentNo = :installmentNo
       AND i.status = :status
       """)
    List<InterestAccrual> findByAccountAndInstallmentAndStatus(
            @Param("accountId") Integer accountId,
            @Param("installmentNo") Integer installmentNo,
            @Param("status") String status
    );
}