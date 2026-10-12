package com.example.Loan_Project.Repository;

import com.example.Loan_Project.Entity.PenaltyCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}