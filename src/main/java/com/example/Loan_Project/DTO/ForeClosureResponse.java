package com.example.Loan_Project.DTO;

import com.example.Loan_Project.Entity.ForeClosureRequest;
import com.example.Loan_Project.Entity.ForeClosureType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ForeClosureResponse {

    private Integer requestId;
    private Integer loanAccountId;
    private String loanAccountNo;
    private BigDecimal foreClosureAmount;
    private BigDecimal partialAmount;
    private BigDecimal principalAmount;
    private BigDecimal accruedInterest;
    private BigDecimal penaltyAmount;
    private BigDecimal bounceCharges;
    private BigDecimal otherCharges;
    private BigDecimal totalPayable;
    private LocalDateTime requestedDate;
    private LocalDateTime expectedClosureDate;
    private String reason;
    private String status;
    private LocalDateTime approvedDate;
    private Boolean isPaid;
    private LocalDateTime paidDate;
    private ForeClosureType foreClosureType;

    public static ForeClosureResponse from(ForeClosureRequest request) {
        return ForeClosureResponse.builder()
                .requestId(request.getRequestId())
                .loanAccountId(
                        request.getLoanAccount().getLoanAccountId())
                .loanAccountNo(
                        request.getLoanAccount().getLoanAccountNo())
                .foreClosureType(request.getForeClosureType())
                .foreClosureAmount(request.getForeClosureAmount())
                .partialAmount(request.getPartialAmount())
                .principalAmount(request.getPrincipalAmount())
                .accruedInterest(request.getAccruedInterest())
                .penaltyAmount(request.getPenaltyAmount())
                .bounceCharges(request.getBounceCharges())
                .otherCharges(request.getOtherCharges())
                .totalPayable(request.getTotalPayable())
                .requestedDate(request.getRequestedDate())
                .expectedClosureDate(request.getExpectedClosureDate())
                .reason(request.getReason())
                .status(request.getStatus() == null ? null : request.getStatus().name())
                .approvedDate(request.getApprovedDate())
                .isPaid(request.getIsPaid())
                .paidDate(request.getPaidDate())
                .build();
    }
}