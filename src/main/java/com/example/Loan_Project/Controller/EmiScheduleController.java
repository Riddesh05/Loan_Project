package com.example.Loan_Project.Controller;

import com.example.Loan_Project.DTO.*;
import com.example.Loan_Project.Service.EmiScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emi-schedule")
@RequiredArgsConstructor
public class EmiScheduleController {

    private final EmiScheduleService emiScheduleService;

    @PostMapping("/generate")
    public ResponseEntity<String> generateEmiSchedule(
            @RequestBody GenerateEmiScheduleRequest request) {

        emiScheduleService.generateEmiSchedule(request);

        return ResponseEntity.ok(
                "EMI schedule generated successfully"
        );
    }

    @GetMapping("/{loanAccountId}")
    public ResponseEntity<List<EmiScheduleResponse>> getEmiSchedule(
            @PathVariable Integer loanAccountId) {

        return ResponseEntity.ok(
                emiScheduleService.getEmiSchedule(loanAccountId)
        );
    }


    @GetMapping("/summary/{loanAccountId}")
    public ResponseEntity<EmiScheduleSummaryResponse> getSummary(
            @PathVariable Integer loanAccountId) {

        return ResponseEntity.ok(
                emiScheduleService.getSummary(loanAccountId)
        );
    }




}