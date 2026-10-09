package com.example.Loan_Project.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OfficerLoginResponse {

    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
}
