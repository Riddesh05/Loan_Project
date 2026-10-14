package com.example.Loan_Project.Entity;


import com.example.Loan_Project.Enum.LoanType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "loan_deals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDeal {

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "DealId")
//    private Integer dealId;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loan_deal_id")
    private Long loanDealId;


//    @Column(name = "CustomerId", nullable = false)
//    private Integer customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerId", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "LoanType")
    private LoanType loanType;

//    @Column(name = "LoanAmount", precision = 18, scale = 2)
//    private BigDecimal loanAmount;




    @Column(name = "LoanAmount", precision = 18, scale = 2)
    private BigDecimal maxLoanAmount;

    @Column(name = "TenureMonths")
    private Integer maxTenureMonths;


    @Column(name = "InterestRate", precision = 8, scale = 4)
    private BigDecimal interestRate;

//    @Column(name = "TenureMonths")
//    private Integer tenureMonths;

    @Column(name = "EmiAmount", precision = 18, scale = 2)
    private BigDecimal emiAmount;

    @Column(name = "BankName")
    private String bankName;

    @Column(name = "BankAccountNumber")
    private String bankAccountNumber;

    @Column(name = "IFSCCode")
    private String ifscCode;

    @Column(name = "EmiDay")
    private Integer emiDay;

    @Column(name = "ApprovedAmount", precision = 18, scale = 2)
    private BigDecimal approvedAmount;
}