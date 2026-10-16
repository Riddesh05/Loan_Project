package com.example.Loan_Project.Repository;

import com.example.Loan_Project.Entity.CibilEligibilityRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface CibilEligibilityRuleRepository extends JpaRepository<CibilEligibilityRule, Long> {

    @Query("""
            SELECT r FROM CibilEligibilityRule r
            WHERE r.active = true
            AND r.minCibilScore <= :cibilScore
            AND (r.maxCibilScore IS NULL OR r.maxCibilScore >= :cibilScore)
            """)

    Optional<CibilEligibilityRule> findMatchingRule(@Param("cibilScore") Integer cibilScore);
}