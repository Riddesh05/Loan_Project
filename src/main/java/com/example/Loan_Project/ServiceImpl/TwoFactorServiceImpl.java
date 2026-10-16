
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

    // Generate a secret for Google Authenticator
    public String generateSecret() {
        return secretGenerator.generate();
    }

    // Generate the QR code as actual PNG image bytes
    public byte[] generateQrCode(String email, String secret) {

        QrData qrData = new QrData.Builder()
                .label(email)
                .secret(secret)
                .issuer("Loan Origination System")
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        try {
            return qrGenerator.generate(qrData);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not generate authenticator QR code", e
            );
        }
    }

    // Verify the six-digit Google Authenticator code
    public boolean verifyCode(String secret, String code) {

        if (secret == null || code == null
                || !code.matches("\\d{6}")) {
            return false;
        }

        return codeVerifier.isValidCode(secret, code);
    }
}
