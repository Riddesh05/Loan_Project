
package com.example.Loan_Project.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentHistoryResponse(
        Integer paymentId,
        Long loanAccountId,
        Integer emiScheduleId,
        BigDecimal paymentAmount,
        LocalDateTime paymentDate,
        String paymentStatus,
        String paymentName,
        String transactionRefNo
) {
}
