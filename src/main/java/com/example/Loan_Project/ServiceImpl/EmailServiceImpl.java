package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.Service.EmailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    public EmailServiceImpl(
            JavaMailSender javaMailSender) {

        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendOtp(String email, String otp) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(email);
        message.setSubject(
                "Loan App - Email Verification OTP"
        );

        message.setText(
                "Your OTP is: " + otp +
                        "\n\nThis OTP is valid for 5 minutes."
        );

        javaMailSender.send(message);
    }

    @Override
    public void sendLoanApprovalEmail(
            String email,
            String customerName,
            BigDecimal sanctionedAmount) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(email);

        message.setSubject(
                "Loan Approved - Loan Project"
        );

        message.setText(
                "Dear " + customerName + ",\n\n" +
                        "Your loan application has been approved successfully.\n\n" +
                        "Sanctioned Amount: ₹" + sanctionedAmount + "\n\n" +
                        "Regards,\n" +
                        "Loan Project Team"
        );

        javaMailSender.send(message);
    }

    @Override
    public void sendSanctionLetterEmail(
            String email,
            String customerName,
            String letterNumber,
            BigDecimal sanctionedAmount,
            Path pdfPath) {

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    true,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(email);
            helper.setSubject(
                    "Your Loan Sanction Letter - " + letterNumber
            );

            helper.setText(
                    "Dear " + customerName + ",\n\n"
                            + "Your loan has been sanctioned successfully.\n\n"
                            + "Sanction Letter Number: " + letterNumber + "\n"
                            + "Sanctioned Amount: INR "
                            + sanctionedAmount.toPlainString() + "\n\n"
                            + "Please find your sanction letter attached.\n\n"
                            + "Regards,\n"
                            + "Loan Project Team"
            );

            helper.addAttachment(
                    pdfPath.getFileName().toString(),
                    new FileSystemResource(pdfPath)
            );

            javaMailSender.send(mimeMessage);

        } catch (MessagingException ex) {
            throw new MailPreparationException(
                    "Unable to prepare sanction letter email",
                    ex
            );
        }
    }
}