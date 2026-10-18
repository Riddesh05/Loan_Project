package com.example.Loan_Project.Controller;

import com.example.Loan_Project.DTO.PaymentBreakdownResponse;
import com.example.Loan_Project.DTO.PaymentHistoryResponse;
import com.example.Loan_Project.DTO.PaymentVerifyRequest;
import com.example.Loan_Project.DTO.PenaltyChargeResponse;
import com.example.Loan_Project.Entity.LoanPayment;
import com.example.Loan_Project.Repository.LoanAccountRepository;
import com.example.Loan_Project.Repository.LoanPaymentRepository;
import com.example.Loan_Project.Service.EmiScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final EmiScheduleService emiScheduleService;
    private final LoanPaymentRepository loanPaymentRepository;
    private final LoanAccountRepository loanAccountRepository;


    @GetMapping("/breakdown/{emiScheduleId}")
    public ResponseEntity<PaymentBreakdownResponse> getPaymentBreakdown(
            @PathVariable Integer emiScheduleId) {

        return ResponseEntity.ok(
                emiScheduleService.getPaymentBreakdown(emiScheduleId)
        );
    }

//    @PostMapping("/verify")
//    public ResponseEntity<String> verifyPayment(
//            @Valid @RequestBody PaymentVerifyRequest request) {
//
//        emiScheduleService.verifyPayment(request);
//
//        return ResponseEntity.ok(
//                "Payment verified successfully"
//        );
//    }

    @GetMapping("/penalties/{loanAccountId}")
    public ResponseEntity<List<PenaltyChargeResponse>> getPenalties(
            @PathVariable Long loanAccountId) {

        return ResponseEntity.ok(
                emiScheduleService.getPenaltiesByLoanAccountId(
                        loanAccountId
                )
        );
    }



    @GetMapping("/history/{loanAccountId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PaymentHistoryResponse>> getPaymentHistory(
            @PathVariable Long loanAccountId) {

        loanAccountRepository.findById(loanAccountId)
                .orElseThrow(() ->
                        new RuntimeException("Loan account not found"));

        List<PaymentHistoryResponse> history =
                loanPaymentRepository
                        .findByLoanAccount_LoanAccountIdOrderByPaymentDateDesc(
                                loanAccountId
                        )
                        .stream()
                        .map(this::toHistoryResponse)
                        .toList();

        return ResponseEntity.ok(history);
    }

    private PaymentHistoryResponse toHistoryResponse(LoanPayment payment) {
        return new PaymentHistoryResponse(
                payment.getPaymentId(),
                payment.getLoanAccount().getLoanAccountId(),
                payment.getEmiScheduleId(),
                payment.getPaymentAmount(),
                payment.getPaymentDate(),
                payment.getPaymentStatus(),
                payment.getPaymentName(),
                payment.getTransactionRefNo()
        );
    }

}