package com.example.Loan_Project.ServiceImpl;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.springframework.stereotype.Service;
import java.util.Base64;


@Service
public class TwoFactorServiceImpl {

    private final SecretGenerator secretGenerator =
            new DefaultSecretGenerator();

    private final QrGenerator qrGenerator =
            new ZxingPngQrGenerator();

    private final CodeVerifier codeVerifier =
            new DefaultCodeVerifier(
                    new DefaultCodeGenerator(),
                    new SystemTimeProvider()
            );

    public String generateSecret() {
        return secretGenerator.generate();
    }

    public String generateQrCode(String email, String secret) {
        QrData qrData = new QrData.Builder()
                .label(email)
                .secret(secret)
                .issuer("Loan Origination System")
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        try {
            byte[] image = qrGenerator.generate(qrData);
            return Base64.getEncoder().encodeToString(image);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not generate authenticator QR code", e);
        }
    }

    public boolean verifyCode(String secret, String code) {
        if (secret == null || code == null
                || !code.matches("\\d{6}")) {
            return false;
        }

        return codeVerifier.isValidCode(secret, code);
    }
}
