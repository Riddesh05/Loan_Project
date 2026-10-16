
package com.example.Loan_Project.Entity;

import com.example.Loan_Project.Enum.ScorecardStatus;
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
    @Column(name = "scorecard_id")
    private Long scorecardId;

    @OneToOne
    @JoinColumn(name = "loan_application_id",
            nullable = false, unique = true)
    @JsonIgnore
    private LoanApplication loanApplication;



    @Column(name = "cibil_score", nullable = false)
    private Integer cibilScore;

    @Column(name = "employment_type", nullable = false)
    private String employmentType;

    @Column(name = "loan_type", nullable = false)
    private String loanType;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "eligible_amount",
            nullable = false, precision = 18, scale = 2)
    private BigDecimal eligibleAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ScorecardStatus status;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;
}
