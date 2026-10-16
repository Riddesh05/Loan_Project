package com.example.Loan_Project.Service;

import com.example.Loan_Project.DTO.SanctionLetterResponse;

import java.nio.file.Path;

public interface SanctionLetterService {

    SanctionLetterResponse generateSanctionLetter(Long loanApplicationId);

    Path getSanctionLetterPath(Long loanApplicationId);
}