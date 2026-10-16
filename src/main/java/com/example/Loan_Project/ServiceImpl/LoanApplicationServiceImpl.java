
package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.DTO.CibilCalculationResponse;
import com.example.Loan_Project.DTO.EmiCalculationRequest;
import com.example.Loan_Project.DTO.EmiCalculationResponse;
import com.example.Loan_Project.DTO.LoanApplicationRequest;
import com.example.Loan_Project.Entity.Customer;
import com.example.Loan_Project.Entity.LoanApplication;
import com.example.Loan_Project.Entity.LoanDeal;
import com.example.Loan_Project.Enum.DocumentVerificationStatus;
import com.example.Loan_Project.Enum.LoanApplicationStatus;
import com.example.Loan_Project.Repository.CustomerRepository;
import com.example.Loan_Project.Repository.LoanApplicationRepository;
import com.example.Loan_Project.Repository.LoanDealRepository;
import com.example.Loan_Project.Service.CibilScoreService;
import com.example.Loan_Project.Service.LoanApplicationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private static final MathContext MC = MathContext.DECIMAL128;

    private final LoanApplicationRepository loanApplicationRepository;
    private final CustomerRepository customerRepository;
    private final LoanDealRepository loanDealRepository;
    private final CibilScoreService cibilScoreService;

    public LoanApplicationServiceImpl(
            LoanApplicationRepository loanApplicationRepository,
            CustomerRepository customerRepository,
            LoanDealRepository loanDealRepository,
            CibilScoreService cibilScoreService) {

        this.loanApplicationRepository = loanApplicationRepository;
        this.customerRepository = customerRepository;
        this.loanDealRepository = loanDealRepository;
        this.cibilScoreService = cibilScoreService;
    }

    @Override
    public EmiCalculationResponse calculateEmi(
            EmiCalculationRequest request) {

        if (request == null
                || request.getLoanDealId() == null) {
            throw new RuntimeException("Loan deal ID is required");
        }

        if (request.getRequestedAmount() == null
                || request.getRequestedAmount().signum() <= 0) {
            throw new RuntimeException(
                    "Requested loan amount must be greater than zero");
        }

        if (request.getTenureMonths() == null
                || request.getTenureMonths() <= 0) {
            throw new RuntimeException(
                    "Tenure must be greater than zero");
        }

        LoanDeal loanDeal = loanDealRepository
                .findById(request.getLoanDealId())
                .orElseThrow(() ->
                        new RuntimeException("Loan deal not found"));

        if (!Boolean.TRUE.equals(loanDeal.getActive())) {
            throw new RuntimeException("Loan deal is not active");
        }

        BigDecimal principal = request.getRequestedAmount();
        BigDecimal annualRate = loanDeal.getInterestRate();

        if (annualRate == null || annualRate.signum() < 0) {
            throw new RuntimeException(
                    "Loan deal has an invalid interest rate");
        }

        int tenure = request.getTenureMonths();

        // Convert annual percentage rate into a monthly decimal rate.
        BigDecimal monthlyRate = annualRate.divide(
                new BigDecimal("1200"), MC);

        BigDecimal monthlyEmi;

        if (monthlyRate.signum() == 0) {
            // Zero-interest loan: principal divided by number of months.
            monthlyEmi = principal.divide(
                    BigDecimal.valueOf(tenure),
                    2,
                    RoundingMode.HALF_UP);
        } else {
            // Reducing-balance EMI formula:
            // EMI = P * r * (1+r)^n / ((1+r)^n - 1)
            BigDecimal factor = BigDecimal.ONE
                    .add(monthlyRate, MC)
                    .pow(tenure, MC);

            BigDecimal numerator = principal
                    .multiply(monthlyRate, MC)
                    .multiply(factor, MC);

            BigDecimal denominator = factor.subtract(
                    BigDecimal.ONE, MC);

            monthlyEmi = numerator.divide(
                    denominator,
                    2,
                    RoundingMode.HALF_UP);
        }

        BigDecimal totalPayment = monthlyEmi
                .multiply(BigDecimal.valueOf(tenure))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalInterest = totalPayment
                .subtract(principal)
                .setScale(2, RoundingMode.HALF_UP);

        EmiCalculationResponse response =
                new EmiCalculationResponse();

        response.setLoanDealId(loanDeal.getLoanDealId());
        response.setPrincipalAmount(principal);
        response.setAnnualInterestRate(annualRate);
        response.setTenureMonths(tenure);
        response.setMonthlyEmi(monthlyEmi);
        response.setTotalPayment(totalPayment);
        response.setTotalInterest(totalInterest);

        return response;
    }

    @Override
    public LoanApplication applyForLoan(
            LoanApplicationRequest request) {

        if (request.getCustomerId() == null
                || request.getLoanDealId() == null) {
            throw new RuntimeException(
                    "Customer ID and Loan Deal ID are required");
        }

        if (request.getRequestedAmount() == null
                || request.getRequestedAmount().signum() <= 0) {
            throw new RuntimeException(
                    "Requested loan amount must be greater than zero");
        }

        if (request.getTenureMonths() == null
                || request.getTenureMonths() <= 0) {
            throw new RuntimeException(
                    "Loan tenure must be greater than zero");
        }

        if (request.getBankName() == null
                || request.getBankName().isBlank()) {
            throw new RuntimeException("Bank name is required");
        }

        if (request.getBankAccountNumber() == null
                || !request.getBankAccountNumber()
                .matches("[0-9]{9,18}")) {
            throw new RuntimeException(
                    "Bank account number must contain 9 to 18 digits");
        }

        if (request.getIfscCode() == null
                || !request.getIfscCode().matches(
                "(?i)^[A-Z]{4}0[A-Z0-9]{6}$")) {
            throw new RuntimeException("Invalid IFSC code");
        }

        if (request.getEmiDay() == null
                || request.getEmiDay() < 1
                || request.getEmiDay() > 31) {
            throw new RuntimeException(
                    "EMI day must be between 1 and 31");
        }

        Customer customer = customerRepository
                .findById(request.getCustomerId())
                .orElseThrow(() ->
                        new RuntimeException("Customer not found"));

        LoanDeal loanDeal = loanDealRepository
                .findById(request.getLoanDealId())
                .orElseThrow(() ->
                        new RuntimeException("Loan deal not found"));

        if (!Boolean.TRUE.equals(loanDeal.getActive())) {
            throw new RuntimeException("Loan deal is not active");
        }

        // Calculate the customer's CIBIL score on the backend.
        CibilCalculationResponse cibilResponse =
                cibilScoreService.calculateCibilScore(
                        customer.getCustomerId());

        if (cibilResponse == null
                || cibilResponse.getCibilScore() == null) {
            throw new RuntimeException(
                    "Unable to retrieve the customer's CIBIL score");
        }

        LoanApplication application = new LoanApplication();

        application.setLoanApplicationId(null);
        application.setCustomer(customer);
        application.setLoanDeal(loanDeal);
        application.setRequestedAmount(request.getRequestedAmount());
        application.setTenureMonths(request.getTenureMonths());
        application.setCibilScore(cibilResponse.getCibilScore());

        application.setBankName(request.getBankName());
        application.setBankAccountNumber(
                request.getBankAccountNumber());
        application.setIfscCode(request.getIfscCode());
        application.setEmiDay(request.getEmiDay());

        application.setStatus(LoanApplicationStatus.APPLIED);
        application.setDocumentVerificationStatus(
                DocumentVerificationStatus.PENDING);
        application.setAppliedAt(LocalDateTime.now());

        return loanApplicationRepository.save(application);
    }

    @Override
    public LoanApplication getApplicationById(Long applicationId) {
        return loanApplicationRepository.findById(applicationId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Loan application not found"));
    }

    @Override
    public List<LoanApplication> getApplicationsByCustomer(
            Long customerId) {
        return loanApplicationRepository
                .findByCustomerCustomerId(customerId);
    }

    @Override
    public List<LoanApplication> getAllApplications() {
        return loanApplicationRepository.findAll();
    }

    @Override
    public LoanApplication updateDocumentVerificationStatus(
            Long applicationId,
            DocumentVerificationStatus status) {

        LoanApplication application =
                loanApplicationRepository.findById(applicationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Loan application not found"));

        application.setDocumentVerificationStatus(status);

        return loanApplicationRepository.save(application);
    }

    @Override
    public List<LoanApplication> getPendingApplications() {
        return loanApplicationRepository.findByStatus(
                LoanApplicationStatus.APPLIED
        );
    }

    @Override
    public LoanApplication approveApplication(Long applicationId) {

        LoanApplication application = loanApplicationRepository
                .findById(applicationId)
                .orElseThrow(() ->
                        new RuntimeException("Loan application not found"));

        if (application.getStatus() != LoanApplicationStatus.APPLIED) {
            throw new RuntimeException(
                    "Only pending applications can be approved");
        }

        application.setStatus(LoanApplicationStatus.APPROVED);
        application.setRejectionReason(null);

        return loanApplicationRepository.save(application);
    }

    @Override
    public LoanApplication rejectApplication(
            Long applicationId,
            String rejectionReason) {

        LoanApplication application = loanApplicationRepository
                .findById(applicationId)
                .orElseThrow(() ->
                        new RuntimeException("Loan application not found"));

        if (application.getStatus() != LoanApplicationStatus.APPLIED) {
            throw new RuntimeException(
                    "Only pending applications can be rejected");
        }

        if (rejectionReason == null || rejectionReason.isBlank()) {
            throw new RuntimeException(
                    "Rejection reason is required");
        }

        if (rejectionReason.trim().length() > 1000) {
            throw new RuntimeException(
                    "Rejection reason cannot exceed 1000 characters");
        }

        application.setStatus(LoanApplicationStatus.REJECTED);
        application.setRejectionReason(rejectionReason.trim());

        return loanApplicationRepository.save(application);
    }
}
