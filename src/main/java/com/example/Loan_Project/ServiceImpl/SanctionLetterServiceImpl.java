
package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.DTO.SanctionLetterResponse;
import com.example.Loan_Project.Entity.LoanApplication;
import com.example.Loan_Project.Entity.LoanProcessing;
import com.example.Loan_Project.Enum.SanctionStatus;
import com.example.Loan_Project.Repository.LoanProcessingRepository;
import com.example.Loan_Project.Service.CustomerNotificationService;
import com.example.Loan_Project.Service.EmailService;
import com.example.Loan_Project.Service.SanctionLetterPdfService;
import com.example.Loan_Project.Service.SanctionLetterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Service
public class SanctionLetterServiceImpl implements SanctionLetterService {

    private static final Logger log =
            LoggerFactory.getLogger(SanctionLetterServiceImpl.class);

    private final LoanProcessingRepository loanProcessingRepository;
    private final SanctionLetterPdfService pdfService;
    private final CustomerNotificationService notificationService;
    private final EmailService emailService;

    public SanctionLetterServiceImpl(
            LoanProcessingRepository loanProcessingRepository,
            SanctionLetterPdfService pdfService,
            CustomerNotificationService notificationService,
            EmailService emailService) {

        this.loanProcessingRepository = loanProcessingRepository;
        this.pdfService = pdfService;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public SanctionLetterResponse generateSanctionLetter(
            Long loanApplicationId) {

        LoanProcessing processing =
                loanProcessingRepository
                        .findByLoanApplicationLoanApplicationId(
                                loanApplicationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Loan processing record not found"));

        if (processing.getSanctionStatus() != SanctionStatus.SANCTIONED) {
            throw new IllegalStateException(
                    "Loan must be sanctioned before generating the sanction letter");
        }

        if (processing.getSanctionLetterNumber() == null
                || processing.getSanctionLetterNumber().isBlank()) {
            throw new IllegalStateException(
                    "Sanction letter number is missing");
        }

        LoanApplication application = processing.getLoanApplication();

        String savedPath = processing.getSanctionLetterPath();

        boolean notificationCreated = false;
        Path pdfPath;

        boolean existingFileAvailable =
                savedPath != null
                        && !savedPath.isBlank()
                        && Files.isRegularFile(Paths.get(savedPath));

        if (existingFileAvailable) {

            pdfPath = Paths.get(savedPath).toAbsolutePath().normalize();

        } else {

            try {
                pdfPath = pdfService.generate(application, processing);
            } catch (IOException ex) {
                throw new UncheckedIOException(
                        "Failed to generate sanction letter PDF", ex);
            }

            processing.setSanctionLetterPath(pdfPath.toString());

            loanProcessingRepository.save(processing);

            if (savedPath == null || savedPath.isBlank()) {

                notificationService.createLoanSanctionedNotification(
                        application.getCustomer().getCustomerId(),
                        processing.getSanctionLetterNumber(),
                        processing.getSanctionedAmount());

                notificationCreated = true;
            }
        }

        boolean emailSent =
                Boolean.TRUE.equals(processing.getSanctionEmailSent());

        if (!emailSent) {

            try {
                emailService.sendSanctionLetterEmail(
                        application.getCustomer().getEmail(),
                        application.getCustomer().getFirstName(),
                        processing.getSanctionLetterNumber(),
                        processing.getSanctionedAmount(),
                        pdfPath);

                processing.setSanctionEmailSent(true);
                processing.setSanctionEmailSentAt(LocalDateTime.now());

                loanProcessingRepository.save(processing);

                emailSent = true;

                log.info(
                        "Sanction letter email sent for application {}",
                        loanApplicationId);

            } catch (MailException ex) {

                log.error(
                        "Sanction letter exists, but email delivery failed for application {}",
                        loanApplicationId,
                        ex);
            }
        }

        String message;

        if (emailSent && existingFileAvailable) {
            message = "Existing sanction letter found. Email delivery status is successful.";
        } else if (emailSent) {
            message = "Sanction letter generated, email sent, and notification created.";
        } else {
            message = "Sanction letter is available, but email delivery failed. Check the application logs and retry.";
        }

        return new SanctionLetterResponse(
                loanApplicationId,
                processing.getSanctionLetterNumber(),
                "/api/v1/loan-processing/sanction-letter/"
                        + loanApplicationId + "/download",
                notificationCreated,
                emailSent,
                message);
    }

    @Override
    @Transactional(readOnly = true)
    public Path getSanctionLetterPath(Long loanApplicationId) {

        LoanProcessing processing =
                loanProcessingRepository
                        .findByLoanApplicationLoanApplicationId(
                                loanApplicationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Loan processing record not found"));

        String savedPath = processing.getSanctionLetterPath();

        if (savedPath == null || savedPath.isBlank()) {
            throw new RuntimeException(
                    "Sanction letter has not been generated");
        }

        Path path = Paths.get(savedPath).toAbsolutePath().normalize();

        if (!Files.isRegularFile(path)) {
            throw new RuntimeException(
                    "Sanction letter PDF file was not found");
        }

        return path;
    }
}
