package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.DTO.TwoFactorResponse;
import com.example.Loan_Project.Entity.Customer;
import com.example.Loan_Project.Repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TwoFactorAuthService {

    private final CustomerRepository customerRepository;
    private final TwoFactorServiceImpl twoFactorService;

    public TwoFactorAuthService(
            CustomerRepository customerRepository,
            TwoFactorServiceImpl twoFactorService) {
        this.customerRepository = customerRepository;
        this.twoFactorService = twoFactorService;
    }

    @Transactional
    public TwoFactorResponse setupCustomerTwoFactor(String email) {

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found"));

        if (Boolean.FALSE.equals(customer.getIsFirstTimeLoggedIn())) {
            throw new IllegalStateException(
                    "Two-factor authentication is already set up");
        }

        String secret = customer.getTwoFactorSecret();

        if (secret == null || secret.isBlank()) {
            secret = twoFactorService.generateSecret();
            customer.setTwoFactorSecret(secret);
            customerRepository.save(customer);
        }

        String qrCode = twoFactorService.generateQrCode(
                customer.getEmail(), secret);

        return new TwoFactorResponse(
                "Scan the QR code using Google Authenticator, then verify the six-digit code.",
                qrCode
        );
    }
}
