package com.example.Loan_Project.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_eligibility")
@Data
public class LoanEligibility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eligibilityId;

    @OneToOne
    @JoinColumn(name = "loan_application_id", nullable = false, unique = true)
    @JsonIgnore
    private LoanApplication loanApplication;

    @Column(nullable = false)
    private Integer cibilScore;

    @Column(nullable = false)
    private BigDecimal eligibleAmountFrom;

    private BigDecimal eligibleAmountTo;

    @Column(nullable = false)
    private Boolean eligible;

    private String remarks;

    @Column(nullable = false)
    private LocalDateTime evaluatedAt;
}