package com.example.Loan_Project.Entity;

import com.example.Loan_Project.Entity.EmiSchedule;
import com.example.Loan_Project.Repository.EmiScheduleRepository;
import com.example.Loan_Project.Service.NotificationService;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EmiNotificationScheduler {

    private static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Kolkata");

    private final EmiScheduleRepository emiScheduleRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 10 0 * * *", zone = "Asia/Kolkata")
    public void checkEmiNotifications() {

        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        LocalDate reminderDate = today.plusDays(3);

        List<EmiSchedule> pendingEmis =
                emiScheduleRepository
                        .findByPaymentStatusIgnoreCase("Pending");

        for (EmiSchedule schedule : pendingEmis) {

            if (schedule.getDueDate() == null) {
                continue;
            }

            LocalDate dueDate = schedule.getDueDate();

            if (dueDate.equals(reminderDate)) {
                notificationService.createEmiNotification(
                        schedule,
                        "EMI_DUE_SOON",
                        "EMI Due Soon",
                        "Your EMI of Rs. " + schedule.getEmi()
                                + " is due on " + dueDate
                                + ". Please pay on time."
                );
            }

            if (dueDate.equals(today)) {
                notificationService.createEmiNotification(
                        schedule,
                        "EMI_DUE_TODAY",
                        "EMI Due Today",
                        "Your EMI of Rs. " + schedule.getEmi()
                                + " is due today. Please make the payment."
                );
            }

            if (dueDate.isBefore(today)) {
                notificationService.createEmiNotification(
                        schedule,
                        "EMI_OVERDUE",
                        "EMI Overdue",
                        "Your EMI of Rs. " + schedule.getEmi()
                                + " was due on " + dueDate
                                + " and is still pending. Please clear it."
                );
            }
        }
    }
}
