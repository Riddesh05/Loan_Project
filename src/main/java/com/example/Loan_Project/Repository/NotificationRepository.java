
package com.example.Loan_Project.Repository;

import com.example.Loan_Project.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {
    boolean existsByEmiSchedule_EmiScheduleIdAndNotificationType(
            Integer emiScheduleId,
            String notificationType
    );

    List<Notification>
    findByLoanAccount_LoanAccountIdOrderByCreatedAtDesc(
            Long loanAccountId
    );
}
