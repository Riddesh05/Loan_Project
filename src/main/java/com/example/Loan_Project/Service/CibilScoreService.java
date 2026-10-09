package com.example.Loan_Project.Service;

import com.example.Loan_Project.DTO.CibilCalculationResponse;

import java.util.List;

public interface CibilScoreService {

    CibilCalculationResponse calculateCibilScore(
            Long customerId
    );

    CibilCalculationResponse getLatestCibilReport(
            Long customerId
    );

    List<CibilCalculationResponse> getAllCibilReports();

}