package com.example.Loan_Project.Service;

import com.example.Loan_Project.DTO.OfficerLoginRequest;
import com.example.Loan_Project.Entity.User;

public interface OfficerService {
    User login(OfficerLoginRequest request);
}
