package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.Entity.EmiSchedule;
import com.example.Loan_Project.Repository.EmiScheduleRepository;
import com.example.Loan_Project.Service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EmiReminderScheduler {

    private final EmiScheduleRepository emiScheduleRepository;
    private final NotificationService notificationService;

    private static final ZoneId ZONE =
            ZoneId.of("Asia/Kolkata");

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Kolkata")
    @Transactional
    public void generateEmiReminders() {

        LocalDate today = LocalDate.now(ZONE);
        LocalDate tomorrow = today.plusDays(1);

        List<EmiSchedule> schedules =
                emiScheduleRepository.findAll();

        for (EmiSchedule schedule : schedules) {

            if (!"Pending".equalsIgnoreCase(
                    schedule.getPaymentStatus())) {
                continue;
            }

            LocalDate dueDate = schedule.getDueDate();

            if (dueDate == null) {
                continue;
            }

            if (dueDate.isBefore(today)) {

                notificationService.createEmiNotification(
                        schedule,
                        "OVERDUE",
                        "EMI Overdue",
                        "Your EMI installment "
                                + schedule.getInstallmentNo()
                                + " was due on "
                                + dueDate
                                + ". Please make the payment."
                );

            } else if (dueDate.equals(tomorrow)) {

                notificationService.createEmiNotification(
                        schedule,
                        "DUE_TOMORROW",
                        "EMI Due Tomorrow",
                        "Your EMI installment "
                                + schedule.getInstallmentNo()
                                + " is due tomorrow, "
                                + dueDate
                                + ". Please make the payment."
                );
            }
        }
    }
}