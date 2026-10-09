package com.example.Loan_Project.Service;

import com.example.Loan_Project.DTO.CustomerRegistrationRequest;
import com.example.Loan_Project.DTO.LoginRequest;
import com.example.Loan_Project.Entity.Customer;

public interface CustomerService {

    Customer register(CustomerRegistrationRequest customerRegistrationRequest);

    Customer login(LoginRequest loginRequest);

}
