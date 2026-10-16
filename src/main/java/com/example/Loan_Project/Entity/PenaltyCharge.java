package com.example.Loan_Project.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PenaltyCharges")
@Data
public class PenaltyCharge {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PenaltyChargeId")
    private Integer penaltyChargeId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LoanAccountId", nullable = false)
    private LoanAccount loanAccount;
    @Column(name = "EmiScheduleId", nullable = false)
    private Integer emiScheduleId;
    @Column(name = "PenaltyAmount", precision = 18, scale = 2)
    private BigDecimal penaltyAmount;
    @Column(name = "ChargeType", length = 30)
    private String chargeType = "PENALTY";
    @Column(name = "Reason", length = 1000)
    private String reason;
    @Column(name = "Status", length = 50)
    private String status;
    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;
}