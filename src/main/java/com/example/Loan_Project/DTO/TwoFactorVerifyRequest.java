package com.example.Loan_Project.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwoFactorVerifyRequest {


    private String email;

    private String code;
}
