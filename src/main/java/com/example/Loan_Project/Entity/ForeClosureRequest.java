package com.example.Loan_Project.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ForeClosureRequests")
@Data
public class ForeClosureRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RequestId")
    private Integer requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LoanAccountId", nullable = false)
    private LoanAccount loanAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "ForeClosureType")
    private ForeClosureType foreClosureType;

    @Column(name = "ForeClosureAmount", precision = 18, scale = 2)
    private BigDecimal foreClosureAmount;

    @Column(name = "PartialAmount", precision = 18, scale = 2)
    private BigDecimal partialAmount;

    // Hibernate maps these fields and can create/update columns with ddl-auto=update.
    @Column(name = "PrincipalAmount", precision = 18, scale = 2)
    private BigDecimal principalAmount = BigDecimal.ZERO;

    @Column(name = "AccruedInterest", precision = 18, scale = 2)
    private BigDecimal accruedInterest = BigDecimal.ZERO;

    @Column(name = "PenaltyAmount", precision = 18, scale = 2)
    private BigDecimal penaltyAmount = BigDecimal.ZERO;

    @Column(name = "BounceCharges", precision = 18, scale = 2)
    private BigDecimal bounceCharges = BigDecimal.ZERO;

    @Column(name = "OtherCharges", precision = 18, scale = 2)
    private BigDecimal otherCharges = BigDecimal.ZERO;

    @Column(name = "TotalPayable", precision = 18, scale = 2)
    private BigDecimal totalPayable = BigDecimal.ZERO;

    @Column(name = "RequestedDate")
    private LocalDateTime requestedDate;

    @Column(name = "ExpectedClosureDate")
    private LocalDateTime expectedClosureDate;

    @Column(name = "Reason", length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private ForeClosureStatus status;

    @Column(name = "ApprovedDate")
    private LocalDateTime approvedDate;

    @Column(name = "IsPaid", nullable = false)
    private Boolean isPaid = false;

    @Column(name = "PaidDate")
    private LocalDateTime paidDate;

    @Column(name = "ClosedBy")
    private Integer closedBy;

    @PrePersist
    public void beforeInsert() {
        if (requestedDate == null) {
            requestedDate = LocalDateTime.now();
        }
        if (isPaid == null) {
            isPaid = false;
        }
    }
}