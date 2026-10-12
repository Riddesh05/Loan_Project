package com.example.Loan_Project.Repository;

import com.example.Loan_Project.Entity.LoanAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanAccountRepository extends JpaRepository<LoanAccount, Integer> {

    Optional<LoanAccount> findByLoanAccountNo(String loanAccountNo);

//    Optional<LoanAccount> findByDealId(Integer dealId);


    @Query("""
           SELECT a
           FROM LoanAccount a
           WHERE a.loanDeal.dealId = :dealId
           """)
    List<LoanAccount> findAccountsByDeal(
            @Param("dealId") Integer dealId
    );
}
