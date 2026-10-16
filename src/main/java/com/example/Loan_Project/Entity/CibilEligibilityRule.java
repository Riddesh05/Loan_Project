
package com.example.Loan_Project.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "cibil_eligibility_rules")
@Data
public class CibilEligibilityRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "min_cibil_score", nullable = false)
    private Integer minCibilScore;

    @Column(name = "max_cibil_score")
    private Integer maxCibilScore;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "eligible_amount", nullable = false,
            precision = 18, scale = 2)
    private BigDecimal eligibleAmount;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
