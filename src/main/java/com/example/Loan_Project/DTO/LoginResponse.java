package com.example.Loan_Project.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

    private Long customerId;
    private String firstName;
    private String lastName;
    private String email;
}
