package com.example.Loan_Project.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "scorecards")
@Data
public class Scorecard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scorecardId;

    @OneToOne
    @JoinColumn(name = "loan_application_id", nullable = false, unique = true)
    @JsonIgnore
    private LoanApplication loanApplication;

    @Column(nullable = false)
    private Integer cibilScore;

    @Column(nullable = false)
    private String employmentType;

    @Column(nullable = false)
    private String loanType;

    @Column(nullable = false)
    private BigDecimal requestedAmount;

    @Column(nullable = false)
    private BigDecimal eligibleAmountFrom;

    private BigDecimal eligibleAmountTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScorecardStatus status;

    private String remarks;

    @Column(nullable = false)
    private LocalDateTime generatedAt;
}