package com.example.Loan_Project.Entity;

import com.example.Loan_Project.Enum.LoanAccountStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_accounts")
@Data
public class LoanAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loan_account_id")
    private Long loanAccountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DealId", nullable = false)
    private LoanDeal loanDeal;

    @Column(name = "loan_account_no", nullable = false, unique = true, length = 40)
    private String loanAccountNo;

    @Column(name = "LoanAmount", nullable = false, precision = 18, scale = 2)
    private BigDecimal loanAmount;

    @Column(name = "outstanding_principal", nullable = false, precision = 18, scale = 2)
    private BigDecimal outstandingPrincipal;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_status", nullable = false, length = 30)
    private LoanAccountStatus loanStatus = LoanAccountStatus.ACTIVE;

    @Column(name = "interest_rate", nullable = false, precision = 8, scale = 4)
    private BigDecimal interestRate;

    @Column(name = "tenure_months", nullable = false)
    private Integer tenureMonths;

    @Column(name = "emi_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal emiAmount;

    @Column(name = "disbursement_date", nullable = false)
    private LocalDateTime disbursementDate;

    @Column(name = "total_paid_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalPaidAmount = BigDecimal.ZERO;

    // Tracks whether the two-consecutive-miss warning was already logged.
    @Column(name = "missed_emi_warning_logged", nullable = false)
    private Boolean missedEmiWarningLogged = false;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;
}
