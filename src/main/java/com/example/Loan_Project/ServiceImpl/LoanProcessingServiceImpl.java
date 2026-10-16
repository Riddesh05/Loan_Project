
package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.Entity.LoanAccount;
import com.example.Loan_Project.Entity.LoanApplication;
import com.example.Loan_Project.Entity.LoanProcessing;
import com.example.Loan_Project.Entity.Scorecard;
import com.example.Loan_Project.Enum.DisbursementStatus;
import com.example.Loan_Project.Enum.LoanAccountStatus;
import com.example.Loan_Project.Enum.LoanApplicationStatus;
import com.example.Loan_Project.Enum.SanctionStatus;
import com.example.Loan_Project.Enum.ScorecardStatus;
import com.example.Loan_Project.Repository.LoanAccountRepository;
import com.example.Loan_Project.Repository.LoanApplicationRepository;
import com.example.Loan_Project.Repository.LoanProcessingRepository;
import com.example.Loan_Project.Repository.ScorecardRepository;
import com.example.Loan_Project.Service.CustomerNotificationService;
import com.example.Loan_Project.Service.LoanProcessingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LoanProcessingServiceImpl implements LoanProcessingService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanProcessingRepository loanProcessingRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final ScorecardRepository scorecardRepository;
    private final CustomerNotificationService notificationService;

    public LoanProcessingServiceImpl(
            LoanApplicationRepository loanApplicationRepository,
            LoanProcessingRepository loanProcessingRepository,
            LoanAccountRepository loanAccountRepository,
            ScorecardRepository scorecardRepository,
            CustomerNotificationService notificationService) {

        this.loanApplicationRepository = loanApplicationRepository;
        this.loanProcessingRepository = loanProcessingRepository;
        this.loanAccountRepository = loanAccountRepository;
        this.scorecardRepository = scorecardRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public LoanProcessing createSanction(Long loanApplicationId) {

        LoanApplication application = getApplication(loanApplicationId);

        Scorecard scorecard = scorecardRepository
                .findByLoanApplicationLoanApplicationId(loanApplicationId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Scorecard not found. Generate the scorecard first."
                        ));

        if (scorecard.getStatus() != ScorecardStatus.APPROVED) {
            throw new RuntimeException(
                    "Loan cannot be sanctioned because the scorecard is not approved"
            );
        }

        if (!Boolean.TRUE.equals(application.getLoanDeal().getActive())) {
            throw new RuntimeException("Loan deal is not active");
        }

        BigDecimal requestedAmount = application.getRequestedAmount();
        BigDecimal maximumLoanAmount =
                application.getLoanDeal().getMaxLoanAmount();

        if (requestedAmount == null
                || requestedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException(
                    "Requested loan amount must be greater than zero");
        }

        if (maximumLoanAmount == null
                || requestedAmount.compareTo(maximumLoanAmount) > 0) {
            throw new RuntimeException(
                    "Requested amount exceeds the maximum amount of the loan deal");
        }

        Integer tenureMonths = application.getTenureMonths();

        if (tenureMonths == null || tenureMonths <= 0) {
            throw new RuntimeException(
                    "Loan tenure must be greater than zero");
        }

        if (application.getLoanDeal().getMaxTenureMonths() == null
                || tenureMonths
                > application.getLoanDeal().getMaxTenureMonths()) {
            throw new RuntimeException(
                    "Requested tenure exceeds the maximum tenure of the loan deal");
        }

        LoanProcessing processing = loanProcessingRepository
                .findByLoanApplicationLoanApplicationId(loanApplicationId)
                .orElseGet(() -> {
                    LoanProcessing newProcessing = new LoanProcessing();
                    newProcessing.setLoanApplication(application);
                    return newProcessing;
                });

        if (processing.getSanctionStatus() == SanctionStatus.SANCTIONED) {
            throw new RuntimeException("Loan is already sanctioned");
        }

        BigDecimal interestRate = application.getLoanDeal().getInterestRate();

        if (interestRate == null
                || interestRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Loan interest rate is invalid");
        }

        BigDecimal emi = calculateEmi(
                requestedAmount,
                interestRate,
                tenureMonths
        );

        LocalDateTime now = LocalDateTime.now();

        processing.setLoanApplication(application);
        processing.setSanctionedAmount(requestedAmount);
        processing.setInterestRate(interestRate);
        processing.setTenureMonths(tenureMonths);
        processing.setMonthlyEmi(emi);

        processing.setSanctionLetterNumber(
                "SAN-" + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase()
        );

        processing.setSanctionStatus(SanctionStatus.SANCTIONED);
        processing.setSanctionedAt(now);
        processing.setProcessedAt(now);

        LoanProcessing saved = loanProcessingRepository.save(processing);

        application.setStatus(LoanApplicationStatus.SANCTIONED);
        loanApplicationRepository.save(application);

        return saved;
    }

    @Override
    @Transactional
    public LoanProcessing disburseLoan(Long loanApplicationId) {

        LoanApplication application = getApplication(loanApplicationId);
        LoanProcessing processing = getProcessing(loanApplicationId);

        if (processing.getSanctionStatus() != SanctionStatus.SANCTIONED) {
            throw new RuntimeException(
                    "Loan must be sanctioned before disbursement");
        }

        if (processing.getDisbursementStatus() == DisbursementStatus.DISBURSED) {
            throw new RuntimeException("Loan is already disbursed");
        }

        // Prevent a duplicate LoanAccount for this application.
        if (loanAccountRepository
                .findByLoanApplication_LoanApplicationId(loanApplicationId)
                .isPresent()) {
            throw new RuntimeException(
                    "Loan account already exists for this application");
        }

        String sanctionLetterPath = processing.getSanctionLetterPath();

        if (sanctionLetterPath == null || sanctionLetterPath.isBlank()
                || !Files.isRegularFile(Paths.get(sanctionLetterPath))) {
            throw new RuntimeException(
                    "Generate the sanction letter PDF before disbursement");
        }

        if (!Boolean.TRUE.equals(processing.getSanctionEmailSent())) {
            throw new RuntimeException(
                    "Send the sanction letter email before disbursement");
        }

        BigDecimal sanctionedAmount = processing.getSanctionedAmount();

        if (sanctionedAmount == null
                || sanctionedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Sanctioned amount is invalid");
        }

        LocalDateTime now = LocalDateTime.now();

        String loanAccountNumber =
                "LN-" + UUID.randomUUID()
                        .toString()
                        .substring(0, 10)
                        .toUpperCase();

        // Update the processing record.
        processing.setDisbursedAmount(sanctionedAmount);
        processing.setDisbursementReference(
                "DIS-" + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase()
        );
        processing.setDisbursementStatus(DisbursementStatus.DISBURSED);
        processing.setDisbursedAt(now);
        processing.setLoanAccountNumber(loanAccountNumber);
        processing.setOutstandingAmount(sanctionedAmount);
        processing.setLoanAccountStatus(LoanAccountStatus.ACTIVE);
        processing.setLoanAccountCreatedAt(now);
        processing.setProcessedAt(now);

        // Create the separate LoanAccount record.
        LoanAccount loanAccount = new LoanAccount();

        loanAccount.setLoanAccountNo(loanAccountNumber);
        loanAccount.setCustomer(application.getCustomer());
        loanAccount.setLoanDeal(application.getLoanDeal());
        loanAccount.setLoanApplication(application);

        loanAccount.setFundedAmount(sanctionedAmount);
        loanAccount.setOutstandingPrincipal(sanctionedAmount);
        loanAccount.setInterestRate(processing.getInterestRate());
        loanAccount.setTenureMonths(processing.getTenureMonths());
        loanAccount.setEmiAmount(processing.getMonthlyEmi());

        loanAccount.setDisbursementDate(now);
        loanAccount.setActivatedAt(now);
        loanAccount.setTotalPaidAmount(BigDecimal.ZERO);
        loanAccount.setLoanStatus(LoanAccountStatus.ACTIVE);

        loanAccountRepository.save(loanAccount);

        application.setStatus(LoanApplicationStatus.DISBURSED);
        loanApplicationRepository.save(application);

        LoanProcessing saved = loanProcessingRepository.save(processing);

        notificationService.createLoanDisbursedNotification(
                application.getCustomer().getCustomerId(),
                saved.getLoanAccountNumber(),
                saved.getDisbursedAmount(),
                saved.getDisbursementReference()
        );

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public LoanProcessing getProcessingDetails(Long loanApplicationId) {
        return getProcessing(loanApplicationId);
    }

    private LoanApplication getApplication(Long loanApplicationId) {
        return loanApplicationRepository.findById(loanApplicationId)
                .orElseThrow(() ->
                        new RuntimeException("Loan application not found"));
    }

    private LoanProcessing getProcessing(Long loanApplicationId) {
        return loanProcessingRepository
                .findByLoanApplicationLoanApplicationId(loanApplicationId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Loan processing record not found"));
    }

    private BigDecimal calculateEmi(
            BigDecimal principal,
            BigDecimal annualInterestRate,
            int tenureMonths) {

        if (tenureMonths <= 0) {
            throw new IllegalArgumentException(
                    "Tenure must be greater than zero");
        }

        BigDecimal monthlyRate = annualInterestRate.divide(
                BigDecimal.valueOf(1200),
                10,
                RoundingMode.HALF_UP
        );

        double amount = principal.doubleValue();
        double rate = monthlyRate.doubleValue();

        double emi;

        if (rate == 0) {
            emi = amount / tenureMonths;
        } else {
            double factor = Math.pow(1 + rate, tenureMonths);
            emi = amount * rate * factor / (factor - 1);
        }

        return BigDecimal.valueOf(emi)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
