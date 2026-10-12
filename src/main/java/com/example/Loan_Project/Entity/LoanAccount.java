package com.example.Loan_Project.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "LoanAccounts")
@Data
public class LoanAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LoanAccountId")
    private Integer loanAccountId;

    @Column(name = "CustomerId", nullable = false)
    private Integer customerId;

//    @Column(name = "DealId", nullable = false)
//    private Integer dealId;
// Relationship with LoanDeal
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DealId", nullable = false)
     private LoanDeal loanDeal;

    @Column(name = "LoanAccountNo", nullable = false, unique = true, length = 100)
    private String loanAccountNo;

    @Column(name = "LoanAmount", nullable = false, precision = 18, scale = 2)
    private BigDecimal loanAmount;

    @Column(name = "OutstandingPrincipal", precision = 18, scale = 2)
    private BigDecimal outstandingPrincipal;

    @Column(name = "LoanStatus", length = 50)
    private String loanStatus;

    @Column(name = "InterestRate", precision = 8, scale = 4)
    private BigDecimal interestRate;

    @Column(name = "TenureMonths")
    private Integer tenureMonths;

    @Column(name = "EmiAmount", precision = 18, scale = 2)
    private BigDecimal emiAmount;

    @Column(name = "DisbursementDate")
    private LocalDateTime disbursementDate;

    @Column(name = "TotalPaidAmount", precision = 18, scale = 2)
    private BigDecimal totalPaidAmount;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;
}