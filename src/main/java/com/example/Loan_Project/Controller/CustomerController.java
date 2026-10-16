package com.example.Loan_Project.Controller;


import com.example.Loan_Project.DTO.CustomerProfileResponse;
import com.example.Loan_Project.DTO.CustomerRegistrationRequest;
import com.example.Loan_Project.Entity.Customer;
import com.example.Loan_Project.Repository.CustomerRepository;
import com.example.Loan_Project.Response.ApiResponse;
import com.example.Loan_Project.Service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/v1/customers")
@CrossOrigin(origins = "http://localhost:4200")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerRepository customerRepository;
    public CustomerController(CustomerService customerService, CustomerRepository customerRepository) {
        this.customerService = customerService;
        this.customerRepository = customerRepository;
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

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>>
    getMyProfile(Authentication authentication) {

        String email = authentication.getName();

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found"));

        CustomerProfileResponse profile =
                new CustomerProfileResponse(
                        customer.getCustomerId(),
                        customer.getFirstName(),
                        customer.getLastName(),
                        customer.getAge(),
                        customer.getEmail(),
                        customer.getMobileNo(),
                        customer.getPanNo(),
                        customer.getAadhaarNo(),
                        customer.getEmploymentType() == null
                                ? null
                                : customer.getEmploymentType().name(),
                        customer.getMonthlyIncome(),
                        customer.getMonthlyInvestment()
                );

        ApiResponse<CustomerProfileResponse> response =
                new ApiResponse<>(
                        true,
                        "Customer profile fetched successfully",
                        profile
                );

        return ResponseEntity.ok(response);
    }


}
