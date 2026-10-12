package com.example.Loan_Project.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "LoanPayments")
@Data
public class LoanPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PaymentId")
    private Integer paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LoanAccountId", nullable = false)
    private LoanAccount loanAccount;

    @Column(name = "PaymentAmount", precision = 18, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "PaymentDate")
    private LocalDateTime paymentDate;

    @Column(name = "PaymentStatus", length = 50)
    private String paymentStatus;

    @Column(name = "PaymentName", length = 200)
    private String paymentName;

    @Column(name = "EmiScheduleId")
    private Integer emiScheduleId;

    @Column(name = "TransactionRefNo", length = 100, unique = true)
    private String transactionRefNo;
}