package com.example.Loan_Project.Service;

import com.example.Loan_Project.Entity.CustomerNotification;

import java.math.BigDecimal;
import java.util.List;

public interface CustomerNotificationService {

    CustomerNotification createLoanSanctionedNotification(
            Long customerId,
            String letterNumber,
            BigDecimal amount
    );

    List<CustomerNotification> getCustomerNotifications(Long customerId);

    CustomerNotification markAsRead(
            Long customerId,
            Long notificationId
    );

    CustomerNotification createLoanDisbursedNotification(
            Long customerId,
            String loanAccountNumber,
            BigDecimal disbursedAmount,
            String disbursementReference
    );
}