package com.example.Loan_Project.Service;

import com.example.Loan_Project.DTO.CreateForeClosureRequest;
import com.example.Loan_Project.DTO.ReviewForeClosureRequest;
import com.example.Loan_Project.Entity.ForeClosureRequest;

import java.util.List;

public interface ForeClosureService {

    ForeClosureRequest createRequest(
            Integer customerId,
            CreateForeClosureRequest request);

    List<ForeClosureRequest> getCustomerRequests(
            Integer customerId);

    List<ForeClosureRequest> getPendingRequests();

    List<ForeClosureRequest> getAllRequests();

    ForeClosureRequest getRequest(Integer requestId);

    ForeClosureRequest reviewRequest(
            Integer requestId,
            ReviewForeClosureRequest request);

    ForeClosureRequest confirmPayment(
            Integer customerId,
            Integer requestId);
}