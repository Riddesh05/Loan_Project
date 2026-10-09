package com.example.Loan_Project.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "CibilReports")
@Data
@ToString
public class CibilReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CibilReportId")
    private Long cibilReportId;

    @Column(name = "CustomerId", nullable = false)
    private Long customerId;

    @Column(name = "PanNo")
    private String panNo;

    @Column(name = "Income", nullable = false, precision = 18, scale = 2)
    private BigDecimal income;

    @Column(name = "EmploymentType", nullable = false)
    private String employmentType;


    @Column(name = "Age", nullable = false)
    private Integer age;

    @Column(name = "Foir", nullable = false, precision = 5, scale = 2)
    private BigDecimal foir;

    @Column(name = "CibilScore", nullable = false)
    private Integer cibilScore;

    @Column(name = "Status", nullable = false)
    private String status;

    @Column(name = "Decision", nullable = false)
    private String decision;

    @Column(name = "Eligibility", nullable = false)
    private String eligibility;

    @Column(name = "LoanAmount", nullable = false, precision = 18, scale = 2)
    private BigDecimal loanAmount;

    @Column(name = "CheckDate", nullable = false)
    private LocalDateTime checkDate;
}
