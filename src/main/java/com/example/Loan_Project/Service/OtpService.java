package com.example.Loan_Project.Service;

public interface OtpService {

    void generateAndStoreOtp(String email);

    boolean verifyOtp(String email, String otp);
}
