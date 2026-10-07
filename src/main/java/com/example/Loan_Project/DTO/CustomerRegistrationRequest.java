package com.example.Loan_Project.DTO;

import com.example.Loan_Project.Entity.EmploymentType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerRegistrationRequest {

    @NotBlank
    private String firstName;

    private String lastName;

    @NotNull
    @Min(18)
    private Integer age;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    @Pattern(regexp = "^[6-9][0-9]{9}$")
    private String mobileNo;

    @NotBlank
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]$")
    private String panNo;

    @NotBlank
    @Pattern(regexp = "^[0-9]{12}$")
    private String aadhaarNo;

    @NotNull
    private EmploymentType employmentType;

    @NotNull
    @Positive
    private BigDecimal monthlyIncome;

    @NotNull
    @Positive
    private BigDecimal monthlyInvestment;


}
