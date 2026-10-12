package com.example.Loan_Project.Repository;

import com.example.Loan_Project.Entity.EmiSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmiScheduleRepository
        extends JpaRepository<EmiSchedule, Integer > {

//    boolean existsByLoanAccount_LoanAccountId(Integer loanAccountId);
//
//    List<EmiSchedule> findByLoanAccount_LoanAccountIdOrderByInstallmentNoAsc(
//            Integer loanAccountId);

    @Query("""
           SELECT COUNT(e) > 0
           FROM EmiSchedule e
           WHERE e.loanAccount.loanAccountId = :accountId
           """)
    boolean existsForAccount(@Param("accountId") Integer accountId);

    @Query("""
           SELECT e
           FROM EmiSchedule e
           WHERE e.loanAccount.loanAccountId = :accountId
           ORDER BY e.installmentNo ASC
           """)
    List<EmiSchedule> findSchedules(
            @Param("accountId") Integer accountId);

}