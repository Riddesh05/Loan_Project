package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.Entity.Customer;
import com.example.Loan_Project.Entity.CustomerNotification;
import com.example.Loan_Project.Enum.NotificationType;
import com.example.Loan_Project.Repository.CustomerNotificationRepository;
import com.example.Loan_Project.Repository.CustomerRepository;
import com.example.Loan_Project.Service.CustomerNotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CustomerNotificationServiceImpl implements CustomerNotificationService {

    private final CustomerNotificationRepository notificationRepository;
    private final CustomerRepository customerRepository;

    public CustomerNotificationServiceImpl(
            CustomerNotificationRepository notificationRepository,
            CustomerRepository customerRepository) {

        this.notificationRepository = notificationRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public CustomerNotification createLoanSanctionedNotification(
            Long customerId,
            String letterNumber,
            BigDecimal amount) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found"));

        CustomerNotification notification = new CustomerNotification();

        notification.setCustomer(customer);
        notification.setNotificationType(NotificationType.LOAN_SANCTIONED);
        notification.setMessage(
                "Your loan has been sanctioned for INR "
                        + amount.toPlainString()
                        + ". Sanction letter: "
                        + letterNumber
        );
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerNotification> getCustomerNotifications(
            Long customerId) {

        if (!customerRepository.existsById(customerId)) {
            throw new RuntimeException("Customer not found");
        }

        return notificationRepository
                .findByCustomer_CustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Override
    @Transactional
    public CustomerNotification markAsRead(
            Long customerId,
            Long notificationId) {

        CustomerNotification notification = notificationRepository
                .findByNotificationIdAndCustomer_CustomerId(
                        notificationId,
                        customerId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Notification not found for this customer"
                        ));

        notification.setIsRead(true);

        return notificationRepository.save(notification);
    }
    @Override
    @Transactional
    public CustomerNotification createLoanDisbursedNotification(
            Long customerId,
            String loanAccountNumber,
            BigDecimal disbursedAmount,
            String disbursementReference) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found"));

        CustomerNotification notification = new CustomerNotification();

        notification.setCustomer(customer);
        notification.setNotificationType(NotificationType.LOAN_DISBURSED);
        notification.setMessage(
                "Your loan has been disbursed successfully. Amount: INR "
                        + disbursedAmount.toPlainString()
                        + ". Loan Account: "
                        + loanAccountNumber
                        + ". Reference: "
                        + disbursementReference
        );
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }
}