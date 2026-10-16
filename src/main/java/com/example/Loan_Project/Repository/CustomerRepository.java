package com.example.Loan_Project.Repository;

import com.example.Loan_Project.Entity.Customer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository
        extends JpaRepository<Customer, Integer> {
}