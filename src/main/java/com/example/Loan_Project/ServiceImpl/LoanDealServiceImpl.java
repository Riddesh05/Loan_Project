
package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.Entity.LoanDeal;
import com.example.Loan_Project.Enum.LoanType;
import com.example.Loan_Project.Repository.LoanDealRepository;
import com.example.Loan_Project.Service.LoanDealService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanDealServiceImpl implements LoanDealService {

    private final LoanDealRepository loanDealRepository;

    public LoanDealServiceImpl(LoanDealRepository loanDealRepository) {
        this.loanDealRepository = loanDealRepository;
    }

    @Override
    public LoanDeal createLoanDeal(LoanDeal loanDeal) {

        if (loanDeal.getLoanType() == null) {
            throw new IllegalArgumentException("Loan type is required");
        }

        // Always assign the interest rate on the backend.
        // Never trust the rate supplied by the client.
        switch (loanDeal.getLoanType()) {
            case HOME_LOAN ->
                    loanDeal.setInterestRate(new BigDecimal("15.00"));

            case VEHICLE_LOAN ->
                    loanDeal.setInterestRate(new BigDecimal("10.00"));

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported loan type: " + loanDeal.getLoanType()
                    );
        }

        loanDeal.setCreatedAt(LocalDateTime.now());

        return loanDealRepository.save(loanDeal);
    }

    @Override
    public List<LoanDeal> getAllLoanDeals() {
        return loanDealRepository.findAll();
    }

    @Override
    public List<LoanDeal> getActiveLoanDeals() {
        return loanDealRepository.findByActiveTrue();
    }
}
