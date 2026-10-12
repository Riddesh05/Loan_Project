package com.example.Loan_Project.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "InterestAccruals")
@Data
public class InterestAccrual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AccrualId")
    private Integer accrualId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LoanAccountId", nullable = false)
    private LoanAccount loanAccount;


    @Column(name = "InterestAmount", nullable = false, precision = 18, scale = 2)
    private BigDecimal interestAmount;

    @Column(name = "AccrualDate")
    private LocalDateTime accrualDate;

    @Column(name = "InstallmentNo")
    private Integer installmentNo;

    @Column(name = "Status", length = 50)
    private String status;
}