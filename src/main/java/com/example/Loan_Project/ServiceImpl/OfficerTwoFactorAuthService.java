
package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.Entity.User;
import com.example.Loan_Project.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OfficerTwoFactorAuthService {

    private final UserRepository userRepository;
    private final TwoFactorServiceImpl twoFactorService;

    public OfficerTwoFactorAuthService(
            UserRepository userRepository,
            TwoFactorServiceImpl twoFactorService) {

        this.userRepository = userRepository;
        this.twoFactorService = twoFactorService;
    }

    @Transactional
    public byte[] setupOfficerTwoFactorImage(String email) {

        User officer = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Officer not found"));

        if (officer.getRole() == null
                || !"Loan Officer".equalsIgnoreCase(
                officer.getRole().getRoleName())) {
            throw new IllegalArgumentException(
                    "User is not a loan officer");
        }

        if (Boolean.FALSE.equals(
                officer.getIsFirstTimeLoggedIn())) {
            throw new IllegalStateException(
                    "Two-factor authentication is already set up");
        }

        String secret = officer.getTwoFactorSecret();

        if (secret == null || secret.isBlank()) {
            secret = twoFactorService.generateSecret();
            officer.setTwoFactorSecret(secret);
            userRepository.save(officer);
        }

        return twoFactorService.generateQrCode(
                officer.getEmail(),
                secret
        );
    }

    @Transactional
    public void verifyOfficerTwoFactor(
            String email, String code) {

        User officer = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Officer not found"));

        if (officer.getRole() == null
                || !"Loan Officer".equalsIgnoreCase(
                officer.getRole().getRoleName())) {
            throw new IllegalArgumentException(
                    "User is not a loan officer");
        }

        String secret = officer.getTwoFactorSecret();

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "Two-factor authentication setup not started");
        }

        if (!twoFactorService.verifyCode(secret, code)) {
            throw new IllegalArgumentException(
                    "Invalid authenticator code");
        }

        if (Boolean.TRUE.equals(
                officer.getIsFirstTimeLoggedIn())) {
            officer.setIsFirstTimeLoggedIn(false);
            userRepository.save(officer);
        }
    }
}
