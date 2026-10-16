package com.example.Loan_Project.Repository;

import com.example.Loan_Project.Entity.EmiSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmiScheduleRepository
        extends JpaRepository<EmiSchedule, Integer> {

//    boolean existsByLoanAccount_LoanAccountId(Long loanAccountId);
//
//    List<EmiSchedule> findByLoanAccount_LoanAccountIdOrderByInstallmentNoAsc(
//            Long loanAccountId);

    @Query("""
           SELECT COUNT(e) > 0
           FROM EmiSchedule e
           WHERE e.loanAccount.loanAccountId = :accountId
           """)
    boolean existsForAccount(@Param("accountId") Long accountId);

    @Query("""
           SELECT e
           FROM EmiSchedule e
           WHERE e.loanAccount.loanAccountId = :accountId
           ORDER BY e.installmentNo ASC
           """)
    List<EmiSchedule> findSchedules(
            @Param("accountId") Long accountId);

}