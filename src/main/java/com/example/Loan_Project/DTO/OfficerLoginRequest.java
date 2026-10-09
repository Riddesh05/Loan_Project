package com.example.Loan_Project.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OfficerLoginRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}
