package com.example.Loan_Project.Controller;

import com.example.Loan_Project.DTO.ApiResponse;
import com.example.Loan_Project.Entity.Scorecard;
import com.example.Loan_Project.Service.ScorecardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/scorecards")
public class ScorecardController {

    private final ScorecardService scorecardService;

    public ScorecardController(ScorecardService scorecardService) {
        this.scorecardService = scorecardService;
    }

    @PostMapping("/{loanApplicationId}")
    public ResponseEntity<ApiResponse<Scorecard>> generateScorecard(
            @PathVariable Long loanApplicationId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Scorecard generated successfully",
                scorecardService.generateScorecard(loanApplicationId)));
    }

    @GetMapping("/{loanApplicationId}")
    public ResponseEntity<ApiResponse<Scorecard>> getScorecard(
            @PathVariable Long loanApplicationId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Scorecard fetched successfully",
                scorecardService.getScorecard(loanApplicationId)));
    }
}
