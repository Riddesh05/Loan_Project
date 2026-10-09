package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.DTO.CibilCalculationResponse;
import com.example.Loan_Project.Entity.CibilReport;
import com.example.Loan_Project.Entity.Customer;
import com.example.Loan_Project.Exception.CibilReportNotFoundException;
import com.example.Loan_Project.Repository.CibilReportRepository;
import com.example.Loan_Project.Repository.CustomerRepository;
import com.example.Loan_Project.Service.CibilScoreService;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CibilScoreServiceImpl implements CibilScoreService {

    private static final Logger logger =
            LoggerFactory.getLogger(CibilScoreServiceImpl.class);

    private final CustomerRepository customerRepository;
    private final CibilReportRepository cibilReportRepository;
    private final ModelMapper modelMapper;

    public CibilScoreServiceImpl(
            CustomerRepository customerRepository,
            CibilReportRepository cibilReportRepository,
            ModelMapper modelMapper) {

        this.customerRepository = customerRepository;
        this.cibilReportRepository = cibilReportRepository;
        this.modelMapper = modelMapper;
    }

    // =========================================================
    // 1. CALCULATE CIBIL SCORE
    // =========================================================

    @Override
    @CacheEvict(value = {"allCibilReports", "cibilReports"}, allEntries = true)
    public CibilCalculationResponse calculateCibilScore(Long customerId) {

        logger.info("Starting CIBIL calculation for customer ID: {}", customerId);

        // Fetch customer from Customers table
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    logger.warn("Customer not found for ID: {}", customerId);
                    return new RuntimeException(
                            "Customer not found with ID: " + customerId);
                });

        BigDecimal income = customer.getMonthlyIncome();
        Integer age = customer.getAge();

        String employmentType = customer.getEmploymentType() != null
                ? customer.getEmploymentType().name()
                : null;

        // Current project requirement: use MonthlyInvestment as FOIR numerator
        BigDecimal monthlyInvestment = customer.getMonthlyInvestment();

        // Validate income
        if (income == null || income.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Invalid monthly income for customer ID: {}", customerId);
            throw new RuntimeException(
                    "Customer income must be greater than zero");
        }

        // Validate employment type
        if (employmentType == null) {
            logger.warn("Employment type is missing for customer ID: {}", customerId);
            throw new RuntimeException(
                    "Customer employment type cannot be null");
        }

        // Validate monthly investment
        if (monthlyInvestment == null
                || monthlyInvestment.compareTo(BigDecimal.ZERO) < 0) {
            logger.warn("Invalid monthly investment for customer ID: {}", customerId);
            throw new RuntimeException(
                    "Customer monthly investment must be present and cannot be negative");
        }

        // FOIR = MonthlyInvestment / MonthlyIncome * 100
        BigDecimal foir = monthlyInvestment
                .divide(income, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        logger.info("FOIR calculated for customer ID {}: {}%",
                customerId, foir);

        // =====================================================
        // 2. AUTOMATIC REJECTION IF FOIR > 50%
        // =====================================================

        if (foir.compareTo(BigDecimal.valueOf(50)) > 0) {

            CibilReport rejectedReport = new CibilReport();

            rejectedReport.setCustomerId(customerId);
            rejectedReport.setPanNo(customer.getPanNo());
            rejectedReport.setIncome(income);
            rejectedReport.setEmploymentType(employmentType);
            rejectedReport.setAge(age);
            rejectedReport.setFoir(foir);
            rejectedReport.setCibilScore(0);
            rejectedReport.setStatus("REJECT");
            rejectedReport.setDecision("REJECT");
            rejectedReport.setEligibility("NOT_ELIGIBLE");
            rejectedReport.setLoanAmount(BigDecimal.ZERO);
            rejectedReport.setCheckDate(LocalDateTime.now());

            CibilReport savedReport =
                    cibilReportRepository.save(rejectedReport);

            logger.warn(
                    "CIBIL report rejected due to FOIR above 50% for customer ID: {}",
                    customerId);

            return convertToResponse(savedReport);
        }

        // =====================================================
        // 3. CALCULATE INDIVIDUAL FACTOR SCORES
        // =====================================================

        int incomeScore = calculateIncomeScore(income);
        int employmentScore = calculateEmploymentScore(employmentType);
        int ageScore = calculateAgeScore(age);
        int foirScore = calculateFoirScore(foir);

        logger.debug(
                "Customer ID {} factor scores: income={}, employment={}, age={}, FOIR={}",
                customerId, incomeScore, employmentScore, ageScore, foirScore);

        // =====================================================
        // 4. CALCULATE FINAL SCORE
        // =====================================================

        int cibilScore =
                incomeScore + employmentScore + ageScore + foirScore;

        // =====================================================
        // 5. DETERMINE STATUS, DECISION, ELIGIBILITY AND LOAN AMOUNT
        // =====================================================

        String status = calculateStatus(cibilScore);
        String decision = calculateDecision(cibilScore);
        String eligibility = calculateEligibility(cibilScore);
        BigDecimal loanAmount = calculateLoanAmount(cibilScore);

        // =====================================================
        // 6. CREATE AND SAVE CIBIL REPORT
        // =====================================================

        CibilReport report = new CibilReport();

        report.setCustomerId(customerId);
        report.setPanNo(customer.getPanNo());
        report.setIncome(income);
        report.setEmploymentType(employmentType);
        report.setAge(age);
        report.setFoir(foir);
        report.setCibilScore(cibilScore);
        report.setStatus(status);
        report.setDecision(decision);
        report.setEligibility(eligibility);
        report.setLoanAmount(loanAmount);
        report.setCheckDate(LocalDateTime.now());

        CibilReport savedReport = cibilReportRepository.save(report);

        logger.info(
                "CIBIL report saved for customer ID: {}, score: {}, status: {}",
                customerId, cibilScore, status);

        return convertToResponse(savedReport);
    }

    // =========================================================
    // 7. INCOME SCORE
    // =========================================================

    private int calculateIncomeScore(BigDecimal income) {

        if (income.compareTo(BigDecimal.valueOf(25_000)) < 0) {
            return 100;
        }

        if (income.compareTo(BigDecimal.valueOf(50_000)) < 0) {
            return 200;
        }

        if (income.compareTo(BigDecimal.valueOf(100_000)) <= 0) {
            return 300;
        }

        return 400;
    }

    // =========================================================
    // 8. EMPLOYMENT SCORE
    // =========================================================

    private int calculateEmploymentScore(String employmentType) {

        return switch (employmentType) {
            case "GOVERNMENT" -> 200;
            case "PRIVATE_SECTOR" -> 150;
            case "SELF_EMPLOYED" -> 100;
            default -> 0;
        };
    }

    // =========================================================
    // 9. AGE SCORE
    // =========================================================

    private int calculateAgeScore(Integer age) {

        if (age == null) {
            return 0;
        }

        if (age >= 21 && age <= 24) {
            return 50;
        }

        if (age >= 25 && age <= 45) {
            return 150;
        }

        if (age >= 46 && age <= 60) {
            return 100;
        }

        return 0;
    }

    // =========================================================
    // 10. FOIR SCORE
    // =========================================================

    private int calculateFoirScore(BigDecimal foir) {

        if (foir.compareTo(BigDecimal.valueOf(30)) < 0) {
            return 250;
        }

        if (foir.compareTo(BigDecimal.valueOf(50)) <= 0) {
            return 150;
        }

        return 0;
    }

    // =========================================================
    // 11. STATUS
    // =========================================================

    private String calculateStatus(int cibilScore) {

        if (cibilScore >= 900) {
            return "EXCELLENT";
        }

        if (cibilScore >= 800) {
            return "VERY_GOOD";
        }

        if (cibilScore >= 750) {
            return "GOOD";
        }

        if (cibilScore >= 700) {
            return "AVERAGE";
        }

        if (cibilScore >= 650) {
            return "RISKY";
        }

        return "REJECT";
    }

    // =========================================================
    // 12. DECISION
    // =========================================================

    private String calculateDecision(int cibilScore) {

        if (cibilScore >= 800) {
            return "AUTO_APPROVE";
        }

        if (cibilScore >= 650) {
            return "MANUAL_REVIEW";
        }

        return "REJECT";
    }

    // =========================================================
    // 13. ELIGIBILITY
    // =========================================================

    private String calculateEligibility(int cibilScore) {

        return cibilScore >= 650
                ? "ELIGIBLE"
                : "NOT_ELIGIBLE";
    }

    // =========================================================
    // 14. LOAN AMOUNT
    // =========================================================

    private BigDecimal calculateLoanAmount(int cibilScore) {

        if (cibilScore >= 900) {
            return BigDecimal.valueOf(10_000_001);
        }

        if (cibilScore >= 800) {
            return BigDecimal.valueOf(7_500_000);
        }

        if (cibilScore >= 750) {
            return BigDecimal.valueOf(5_000_000);
        }

        if (cibilScore >= 700) {
            return BigDecimal.valueOf(2_500_000);
        }

        if (cibilScore >= 650) {
            return BigDecimal.valueOf(1_000_000);
        }

        return BigDecimal.ZERO;
    }

    // =========================================================
    // 15. ENTITY TO RESPONSE DTO
    // =========================================================

    private CibilCalculationResponse convertToResponse(CibilReport report) {

        return modelMapper.map(report, CibilCalculationResponse.class);
    }

    // =========================================================
    // 16. GET LATEST CIBIL REPORT FOR A CUSTOMER
    // =========================================================

    @Override
    @Cacheable(value = "cibilReports", key = "#customerId")
    public CibilCalculationResponse getLatestCibilReport(Long customerId) {

        logger.info("Fetching latest CIBIL report for customer ID: {}", customerId);

        CibilReport report = cibilReportRepository
                .findTopByCustomerIdOrderByCheckDateDesc(customerId)
                .orElseThrow(() -> {
                    logger.warn("CIBIL report not found for customer ID: {}", customerId);
                    return new CibilReportNotFoundException(
                            "CIBIL report not found for customer ID: " + customerId);
                });

        return convertToResponse(report);
    }

    // =========================================================
    // 17. GET ALL CIBIL REPORTS
    // =========================================================

    @Override
    @Cacheable(value = "allCibilReports")
    public List<CibilCalculationResponse> getAllCibilReports() {

        logger.info("Fetching all CIBIL reports");

        List<CibilReport> reports = cibilReportRepository.findAll();
        List<CibilCalculationResponse> responseList = new ArrayList<>();

        for (CibilReport report : reports) {
            responseList.add(convertToResponse(report));
        }

        logger.info("Successfully fetched {} CIBIL reports", responseList.size());

        return responseList;
    }
}