package com.example.Loan_Project.Service;

import com.example.Loan_Project.Entity.LoanApplication;
import com.example.Loan_Project.Entity.LoanProcessing;

import java.io.IOException;
import java.nio.file.Path;

public interface SanctionLetterPdfService {

    Path generate(
            LoanApplication application,
            LoanProcessing processing
    ) throws IOException;
}