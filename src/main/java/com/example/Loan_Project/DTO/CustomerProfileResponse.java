
package com.example.Loan_Project.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CustomerProfileResponse {

    private Long customerId;
    private String firstName;
    private String lastName;
    private Integer age;
    private String email;
    private String mobileNo;
    private String panNo;
    private String aadhaarNo;
    private String employmentType;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyInvestment;
}
