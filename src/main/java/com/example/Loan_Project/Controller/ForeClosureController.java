package com.example.Loan_Project.Controller;

import com.example.Loan_Project.DTO.CreateForeClosureRequest;
import com.example.Loan_Project.DTO.ForeClosureResponse;
import com.example.Loan_Project.DTO.ReviewForeClosureRequest;
import com.example.Loan_Project.Entity.ForeClosureRequest;
import com.example.Loan_Project.Respsonse.ApiResponse;
import com.example.Loan_Project.Service.ForeClosureService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/foreclosures")
@RequiredArgsConstructor
public class ForeClosureController {

    private final ForeClosureService foreClosureService;

    @PostMapping("/customers/{customerId}/requests")
    public ResponseEntity<ApiResponse<ForeClosureResponse>> createRequest(
            @PathVariable Integer customerId,
            @Valid @RequestBody CreateForeClosureRequest input) {

        ForeClosureRequest saved =
                foreClosureService.createRequest(customerId, input);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        "Foreclosure request submitted",
                        ForeClosureResponse.from(saved)));
    }

    @GetMapping("/customers/{customerId}/requests")
    public ResponseEntity<ApiResponse<List<ForeClosureResponse>>>
    getCustomerRequests(@PathVariable Integer customerId) {

        List<ForeClosureResponse> result =
                foreClosureService.getCustomerRequests(customerId)
                        .stream()
                        .map(ForeClosureResponse::from)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success("Customer requests", result));
    }

    @GetMapping("/officer/pending")
    public ResponseEntity<ApiResponse<List<ForeClosureResponse>>>
    getPendingRequests() {

        List<ForeClosureResponse> result =
                foreClosureService.getPendingRequests()
                        .stream()
                        .map(ForeClosureResponse::from)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success("Pending requests", result));
    }

    @GetMapping("/officer/requests")
    public ResponseEntity<ApiResponse<List<ForeClosureResponse>>>
    getAllRequests() {

        List<ForeClosureResponse> result =
                foreClosureService.getAllRequests()
                        .stream()
                        .map(ForeClosureResponse::from)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success("All foreclosure requests", result));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ApiResponse<ForeClosureResponse>>
    getRequest(@PathVariable Integer requestId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Foreclosure request details",
                        ForeClosureResponse.from(
                                foreClosureService.getRequest(requestId))));
    }

    @PatchMapping("/{requestId}/review")
    public ResponseEntity<ApiResponse<ForeClosureResponse>> reviewRequest(
            @PathVariable Integer requestId,
            @Valid @RequestBody ReviewForeClosureRequest input) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Request reviewed",
                        ForeClosureResponse.from(
                                foreClosureService.reviewRequest(
                                        requestId, input))));
    }

    @PostMapping(
            "/customers/{customerId}/requests/{requestId}/confirm-payment")
    public ResponseEntity<ApiResponse<ForeClosureResponse>> confirmPayment(
            @PathVariable Integer customerId,
            @PathVariable Integer requestId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Payment recorded for testing",
                        ForeClosureResponse.from(
                                foreClosureService.confirmPayment(
                                        customerId, requestId))));
    }
}