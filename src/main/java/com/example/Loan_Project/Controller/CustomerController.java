package com.example.Loan_Project.Controller;


import com.example.Loan_Project.DTO.CustomerRegistrationRequest;
import com.example.Loan_Project.Entity.Customer;
import com.example.Loan_Project.Service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }


    @PostMapping("/register")
    public ResponseEntity<Customer> register(@Valid @RequestBody CustomerRegistrationRequest
                                                     customerRegistrationRequest){
        Customer customer = customerService.register(customerRegistrationRequest);

        return ResponseEntity.ok(customer);
    }

}
