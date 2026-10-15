package com.example.Loan_Project.Service;

import com.example.Loan_Project.DTO.RazorpayCreateOrderRequest;
import com.example.Loan_Project.DTO.RazorpayCreateOrderResponse;
import com.example.Loan_Project.DTO.RazorpayVerifyRequest;

import java.util.Map;

public interface RazorpayPaymentService {

    Map<String, Object> getOrderDetails(String orderId);

    RazorpayCreateOrderResponse createOrder(
            RazorpayCreateOrderRequest request
    );

    void verifyPayment(RazorpayVerifyRequest request);
}