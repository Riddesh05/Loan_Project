package com.example.Loan_Project.Entity;

import com.example.Loan_Project.Enum.LoanType;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_deals")
@Data
public class LoanDeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loan_deal_id")
    private Long loanDealId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "customer_id",
            referencedColumnName = "customer_id",
            nullable = false
    )
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_type", nullable = false)
    private LoanType loanType;

    @Column(name = "max_loan_amount", precision = 38, scale = 2)
    private BigDecimal maxLoanAmount;

    @Column(name = "max_tenure_months", nullable = false)
    private Integer maxTenureMonths;

    @Column(name = "interest_rate", precision = 38, scale = 2,
            nullable = false)
    private BigDecimal interestRate;

    @Column(name = "EmiAmount", precision = 18, scale = 2)
    private BigDecimal emiAmount;

    @Column(name = "BankName")
    private String bankName;

    @Column(name = "BankAccountNumber")
    private String bankAccountNumber;

    @Column(name = "IFSCCode")
    private String ifscCode;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "EmiDay")
    private Integer emiDay;

    @Column(name = "ApprovedAmount", precision = 18, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}