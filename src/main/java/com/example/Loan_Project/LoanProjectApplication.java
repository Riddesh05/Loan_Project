package com.example.Loan_Project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class LoanProjectApplication {

	public static void main(String[] args) {

		SpringApplication.run(
				LoanProjectApplication.class,
				args
		);
	}
}