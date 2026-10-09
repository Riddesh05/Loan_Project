package com.example.Loan_Project.Controller;


import com.example.Loan_Project.DTO.LoginRequest;
import com.example.Loan_Project.DTO.LoginResponse;
import com.example.Loan_Project.Entity.Customer;
import com.example.Loan_Project.Response.ApiResponse;
import com.example.Loan_Project.Service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final CustomerService customerService;

    public AuthController(CustomerService customerService) {
        this.customerService = customerService;
    }


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>>
    login(@Valid @RequestBody LoginRequest loginRequest)
    {
        Customer customer = customerService.login(loginRequest);

        LoginResponse loginResponse = new LoginResponse(
                customer.getCustomerId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail()
        );

        ApiResponse<LoginResponse> response =
                new ApiResponse<>(
                        true,
                        "Login Successfull",
                        loginResponse
                );

        return ResponseEntity.ok(response);
    }

}
