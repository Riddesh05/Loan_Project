package com.example.Loan_Project.Entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Customers")
@Data
@ToString
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CustomerId")
    private Long customerId;

    @Column(name = "FirstName", nullable = false)
    private String firstName;

    @Column(name = "LastName")
    private String lastName;

    @Column(name = "Age")
    private Integer age;

    @Column(name = "Email", nullable = false, unique = true)
    private String email;

    @Column(name = "Password")
    private String password;

    @Column(name = "MobileNo")
    private String mobileNo;

    @Column(name = "PanNo", nullable = false, unique = true)
    private String panNo;

    @Column(name = "AadhaarNo" , nullable = false, unique = true)
    private String aadhaarNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "EmploymentType")
    private EmploymentType employmentType;

    @Column(name = "MonthlyIncome")
    private BigDecimal monthlyIncome;

    @Column(name = "MonthlyInvestment")
    private BigDecimal monthlyInvestment;

    @Column(name = "IsEmailVerified", nullable = false)
    private Boolean isEmailVerified = false;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;


}
