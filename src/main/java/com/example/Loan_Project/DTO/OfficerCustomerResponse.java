package com.example.Loan_Project.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OfficerCustomerResponse {

    private Long customerId;
    private String customerName;
    private String email;
    private String mobile;
}