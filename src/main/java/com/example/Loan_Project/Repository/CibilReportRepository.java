package com.example.Loan_Project.Repository;

import com.example.Loan_Project.DTO.CibilCalculationResponse;
import com.example.Loan_Project.Entity.CibilReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CibilReportRepository extends JpaRepository<CibilReport, Long> {

    Optional<CibilReport> findTopByCustomerIdOrderByCheckDateDesc(Long customerId);
}
