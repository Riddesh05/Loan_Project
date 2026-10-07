package com.example.Loan_Project.Repository;

import com.example.Loan_Project.Entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer,Long> {

    boolean existsByEmail(String email);

    Optional<Customer> findByEmail(String email);


}
