package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.Entity.LoanDeal;
import com.example.Loan_Project.Repository.LoanDealRepository;
import com.example.Loan_Project.Service.LoanDealService;
import org.springframework.stereotype.Service;

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