package com.example.Loan_Project.Service;

import com.example.Loan_Project.DTO.*;

import java.util.List;

public interface EmiScheduleService {

    void generateEmiSchedule(GenerateEmiScheduleRequest request);

    List<EmiScheduleResponse> getEmiSchedule(Long loanAccountId);

    EmiScheduleSummaryResponse getSummary(Long loanAccountId);

    PaymentBreakdownResponse getPaymentBreakdown(Integer emiScheduleId);

    void verifyPayment(PaymentVerifyRequest request);

    /** Internal hook; invoke only after a trusted provider confirms a bounce. */
    void recordBouncePenalty(Integer emiScheduleId);
    List<PenaltyChargeResponse> getPenaltiesByLoanAccountId(
            Long loanAccountId
    );
}
