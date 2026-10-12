package com.example.Loan_Project.Service;





import com.example.Loan_Project.DTO.*;

import java.util.List;

public interface EmiScheduleService {

//    List<EmiScheduleResponse> generateSchedule(
//            EmiScheduleGenerationRequest request
//    );

    void generateEmiSchedule(GenerateEmiScheduleRequest request);

    List<EmiScheduleResponse> getEmiSchedule(Integer loanAccountId);
    EmiScheduleSummaryResponse getSummary(Integer loanAccountId);
    PaymentBreakdownResponse getPaymentBreakdown(Integer emiScheduleId);
    void verifyPayment(PaymentVerifyRequest request);
//    List<EmiScheduleResponse> getSchedule(
//            Long loanAccountId
//    );
//
//    EmiSummaryResponse getSummary(
//            Long loanAccountId
//    );
//
//    EmiScheduleResponse getNextEmi(
//            Long loanAccountId
//    );


//    List<EmiScheduleResponse> getSchedule(
//            Integer loanAccountId
//    );
//
//    EmiSummaryResponse getSummary(
//            Integer loanAccountId
//    );
//
//    EmiScheduleResponse getNextEmi(
//            Integer loanAccountId
//    );
}
