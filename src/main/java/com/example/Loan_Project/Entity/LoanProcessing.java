package com.example.Loan_Project.Entity;

import com.example.Loan_Project.Enum.DisbursementStatus;
import com.example.Loan_Project.Enum.LoanAccountStatus;
import com.example.Loan_Project.Enum.SanctionStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_processing")
@Data
public class LoanProcessing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long processingId;

    @OneToOne
    @JoinColumn(
            name = "loan_application_id",
            nullable = false,
            unique = true
    )
    @JsonIgnore
    private LoanApplication loanApplication;

    private BigDecimal sanctionedAmount;

    private BigDecimal interestRate;

    private Integer tenureMonths;

    private BigDecimal monthlyEmi;

    private String sanctionLetterNumber;

    @Enumerated(EnumType.STRING)
    private SanctionStatus sanctionStatus;

    private LocalDateTime sanctionedAt;

    private BigDecimal disbursedAmount;

    private String disbursementReference;

    @Enumerated(EnumType.STRING)
    private DisbursementStatus disbursementStatus;

    private LocalDateTime disbursedAt;

    private String loanAccountNumber;

    private BigDecimal outstandingAmount;

    @Enumerated(EnumType.STRING)
    private LoanAccountStatus loanAccountStatus;

    private LocalDateTime loanAccountCreatedAt;

    private LocalDateTime processedAt;

    @Column(length = 1000)
    private String sanctionLetterPath;

    @Column(nullable = false)
    private Boolean sanctionEmailSent = false;

    private LocalDateTime sanctionEmailSentAt;
}