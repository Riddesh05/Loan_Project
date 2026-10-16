package com.example.Loan_Project.Entity;

import com.example.Loan_Project.Enum.LoanType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.*;

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
    @JoinColumn(name = "CustomerId", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
  //  @Column(name = "LoanType")
    @Column(nullable = false)
    private LoanType loanType;

    @Column(name = "LoanAmount", precision = 18, scale = 2)
    private BigDecimal maxLoanAmount;

   //  @Column(name = "TenureMonths")
    @Column(nullable = false)
    private Integer maxTenureMonths;

  //  @Column(name = "InterestRate", precision = 8, scale = 4)
     @Column(nullable = false)
    private BigDecimal interestRate;

    @Column(name = "EmiAmount", precision = 18, scale = 2)
    private BigDecimal emiAmount;

    @Column(name = "BankName")
    private String bankName;

    @Column(name = "BankAccountNumber")
    private String bankAccountNumber;

    @Column(name = "IFSCCode")
    private String ifscCode;
    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "EmiDay")
    private Integer emiDay;

    @Column(name = "ApprovedAmount", precision = 18, scale = 2)
    private BigDecimal approvedAmount;
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
