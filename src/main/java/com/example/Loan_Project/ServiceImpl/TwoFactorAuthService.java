
package com.example.Loan_Project.ServiceImpl;

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

    // Generate QR image for the first-time customer
    @Transactional
    public byte[] setupCustomerTwoFactorImage(String email) {

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found"));

        // Do not allow QR setup again after successful setup
        if (Boolean.FALSE.equals(
                customer.getIsFirstTimeLoggedIn())) {

            throw new IllegalStateException(
                    "Two-factor authentication is already set up");
        }

        // Reuse the existing secret if setup was started earlier
        String secret = customer.getTwoFactorSecret();

        if (secret == null || secret.isBlank()) {
            secret = twoFactorService.generateSecret();

            customer.setTwoFactorSecret(secret);
            customerRepository.save(customer);
        }

        // Return the actual PNG image bytes
        return twoFactorService.generateQrCode(
                customer.getEmail(),
                secret
        );
    }

    // Verify Google Authenticator OTP
    @Transactional
    public void verifyCustomerTwoFactor(
            String email,
            String code) {

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found"));

        String secret = customer.getTwoFactorSecret();

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "Two-factor authentication setup not started");
        }

        if (!twoFactorService.verifyCode(secret, code)) {
            throw new IllegalArgumentException(
                    "Invalid authenticator code");
        }

        // Mark first-time setup complete only after valid OTP
        if (Boolean.TRUE.equals(
                customer.getIsFirstTimeLoggedIn())) {

            customer.setIsFirstTimeLoggedIn(false);
            customerRepository.save(customer);
        }
    }
}
