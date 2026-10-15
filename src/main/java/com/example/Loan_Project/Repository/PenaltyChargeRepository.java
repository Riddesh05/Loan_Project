package com.example.Loan_Project.Repository;

import com.example.Loan_Project.Entity.PenaltyCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PenaltyChargeRepository
        extends JpaRepository<PenaltyCharge, Integer> {
    @Query("""
       SELECT p
       FROM PenaltyCharge p
       WHERE p.emiSchedule.emiScheduleId = :emiScheduleId
       AND p.status = :status
       """)
    List<PenaltyCharge> findByScheduleAndStatus(
            @Param("emiScheduleId") Integer emiScheduleId,
            @Param("status") String status
    );


    @Query("""
       SELECT p
       FROM PenaltyCharge p
       WHERE p.emiSchedule.emiScheduleId = :emiScheduleId
       AND p.reason = :reason
       """)
    List<PenaltyCharge> findByScheduleAndReason(
            @Param("emiScheduleId") Integer emiScheduleId,
            @Param("reason") String reason
    );


    List<PenaltyCharge> findByEmiSchedule_EmiScheduleIdAndReasonAndStatus(
            Integer emiScheduleId,
            String reason,
            String status
    );
    boolean existsByEmiSchedule_EmiScheduleIdAndReason(
            Integer emiScheduleId,
            String reason
    );
// NEW METHOD: Loan account ki saari penalties fetch karne ke liye

    List<PenaltyCharge>
    findByLoanAccount_LoanAccountIdOrderByCreatedAtDesc(
            Long loanAccountId
    );


    // NEW: Check whether daily late fee already exists for this date
    boolean existsByEmiSchedule_EmiScheduleIdAndReasonAndPenaltyDate(
            Integer emiScheduleId,
            String reason,
            LocalDate penaltyDate
    );

    // NEW: Fetch all penalties for a particular EMI
    List<PenaltyCharge>
    findByEmiSchedule_EmiScheduleId(Integer emiScheduleId);
}