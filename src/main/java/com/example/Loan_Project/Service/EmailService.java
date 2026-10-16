package com.example.Loan_Project.Service;

import java.math.BigDecimal;

public interface EmailService {

    void sendOtp(String email, String otp);

    void sendLoanApprovalEmail(
            String email,
            String customerName,
            BigDecimal sanctionedAmount
    );

    void sendSanctionLetterEmail(
            String email,
            String customerName,
            String letterNumber,
            BigDecimal sanctionedAmount,
            java.nio.file.Path pdfPath
    );
}