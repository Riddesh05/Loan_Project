package com.example.Loan_Project.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RazorpayCreateOrderResponse {

    private String orderId;
    private String currency;
    private Long amount; // Paise
    private String keyId;
    private Integer emiScheduleId;
    private Long loanAccountId;
}