package com.example.Loan_Project.Controller;

import com.example.Loan_Project.Response.ApiResponse;
import com.example.Loan_Project.DTO.CibilCalculationResponse;
import com.example.Loan_Project.Service.CibilScoreService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cibil")
public class CibilController {

    private static final Logger logger =
            LoggerFactory.getLogger(CibilController.class);

    private final CibilScoreService cibilScoreService;

    public CibilController(CibilScoreService cibilScoreService) {
        this.cibilScoreService = cibilScoreService;
    }

    // Calculate CIBIL score
    @PostMapping("/calculate/{customerId}")
    public ResponseEntity<ApiResponse<CibilCalculationResponse>>
    calculateCibilScore(@PathVariable Long customerId) {

        logger.info(
                "Received request to calculate CIBIL score for customer ID: {}",
                customerId
        );

        CibilCalculationResponse response =
                cibilScoreService.calculateCibilScore(customerId);

        logger.info(
                "CIBIL calculation completed for customer ID: {}",
                customerId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "CIBIL score generated successfully",
                        response
                )
        );
    }

    // Get latest CIBIL report for a customer
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<CibilCalculationResponse>>
    getLatestCibilReport(@PathVariable Long customerId) {

        logger.info(
                "Received request to fetch latest CIBIL report for customer ID: {}",
                customerId
        );

        CibilCalculationResponse response =
                cibilScoreService.getLatestCibilReport(customerId);

        logger.info(
                "Latest CIBIL report fetched for customer ID: {}",
                customerId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "CIBIL report fetched successfully",
                        response
                )
        );
    }

    // Get all CIBIL reports
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<CibilCalculationResponse>>>
    getAllCibilReports() {

        logger.info("Received request to fetch all CIBIL reports");

        List<CibilCalculationResponse> reports =
                cibilScoreService.getAllCibilReports();

        logger.info(
                "Successfully fetched {} CIBIL reports",
                reports.size()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "All CIBIL reports fetched successfully",
                        reports
                )
        );
    }
}