package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.DTO.CustomerRegistrationRequest;
import com.example.Loan_Project.Entity.Customer;
import com.example.Loan_Project.Exception.EmailAlreadyExistsException;
import com.example.Loan_Project.Repository.CustomerRepository;
import com.example.Loan_Project.Service.CustomerService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }


    @Override
    public Customer register(CustomerRegistrationRequest customerRegistrationRequest) {

        if (customerRepository.existsByEmail(customerRegistrationRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Email Already Registered!");
        }

        Customer customer = new Customer();
        customer.setFirstName(customerRegistrationRequest.getFirstName());
        customer.setLastName(customerRegistrationRequest.getLastName());
        customer.setAge(customerRegistrationRequest.getAge());
        customer.setEmail(customerRegistrationRequest.getEmail());
        customer.setPassword(customerRegistrationRequest.getPassword());
        customer.setMobileNo(customerRegistrationRequest.getMobileNo());
        customer.setPanNo(customerRegistrationRequest.getPanNo());
        customer.setAadhaarNo(customerRegistrationRequest.getAadhaarNo());
        customer.setEmploymentType(customerRegistrationRequest.getEmploymentType());
        customer.setMonthlyIncome(customerRegistrationRequest.getMonthlyIncome());
        customer.setMonthlyInvestment(customerRegistrationRequest.getMonthlyInvestment());
        customer.setCreatedAt(LocalDateTime.now());

        customerRepository.save(customer);

        return customer;

    }
}
