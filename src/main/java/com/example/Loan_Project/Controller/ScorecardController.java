package com.example.Loan_Project.Controller;

import com.example.Loan_Project.Entity.Scorecard;
import com.example.Loan_Project.Service.ScorecardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/scorecards")
public class ScorecardController {

    private final ScorecardService scorecardService;

    public ScorecardController(ScorecardService scorecardService) {
        this.scorecardService = scorecardService;
    }

    @PostMapping("/{loanApplicationId}")
    public ResponseEntity<Scorecard> generateScorecard(
            @PathVariable Long loanApplicationId) {

        return ResponseEntity.ok(
                scorecardService.generateScorecard(loanApplicationId)
        );
    }

    @GetMapping("/{loanApplicationId}")
    public ResponseEntity<Scorecard> getScorecard(
            @PathVariable Long loanApplicationId) {

        return ResponseEntity.ok(
                scorecardService.getScorecard(loanApplicationId)
        );
    }
}