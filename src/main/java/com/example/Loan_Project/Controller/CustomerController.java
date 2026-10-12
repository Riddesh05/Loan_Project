package com.example.Loan_Project.Controller;


import com.example.Loan_Project.DTO.CustomerRegistrationRequest;
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
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }



    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(
            @Valid @RequestBody CustomerRegistrationRequest customerRegistrationRequest) {

        customerService.register(customerRegistrationRequest);

        ApiResponse<String> response = new ApiResponse<>(
                true,
                "Registration successful. Please verify your email using the OTP sent to you.",
                null
        );

        return ResponseEntity.ok(response);
    }


}
