package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.DTO.LoanApplicationRequest;
import com.example.Loan_Project.Entity.Customer;
import com.example.Loan_Project.Entity.LoanApplication;
import com.example.Loan_Project.Entity.LoanApplicationStatus;
import com.example.Loan_Project.Entity.LoanDeal;
import com.example.Loan_Project.Repository.CustomerRepository;
import com.example.Loan_Project.Repository.LoanApplicationRepository;
import com.example.Loan_Project.Repository.LoanDealRepository;
import com.example.Loan_Project.Service.LoanApplicationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final CustomerRepository customerRepository;
    private final LoanDealRepository loanDealRepository;

    public LoanApplicationServiceImpl(
            LoanApplicationRepository loanApplicationRepository,
            CustomerRepository customerRepository,
            LoanDealRepository loanDealRepository) {

        this.loanApplicationRepository = loanApplicationRepository;
        this.customerRepository = customerRepository;
        this.loanDealRepository = loanDealRepository;
    }

    @Override
    public LoanApplication applyForLoan(LoanApplicationRequest request) {

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        LoanDeal loanDeal = loanDealRepository.findById(request.getLoanDealId())
                .orElseThrow(() -> new RuntimeException("Loan deal not found"));

        if (!loanDeal.getActive()) {
            throw new RuntimeException("Loan deal is not active");
        }

        LoanApplication application = new LoanApplication();

        application.setCustomer(customer);
        application.setLoanDeal(loanDeal);
        application.setRequestedAmount(request.getRequestedAmount());
        application.setTenureMonths(request.getTenureMonths());
        application.setCibilScore(request.getCibilScore());
        application.setStatus(LoanApplicationStatus.APPLIED);
        application.setAppliedAt(LocalDateTime.now());

        return loanApplicationRepository.save(application);
    }

    @Override
    public LoanApplication getApplicationById(Long applicationId) {

        return loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Loan application not found"));
    }

    @Override
    public List<LoanApplication> getApplicationsByCustomer(Long customerId) {

        return loanApplicationRepository.findByCustomerCustomerId(customerId);
    }

    @Override
    public List<LoanApplication> getAllApplications() {

        return loanApplicationRepository.findAll();
    }
}