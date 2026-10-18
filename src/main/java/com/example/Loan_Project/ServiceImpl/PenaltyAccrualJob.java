package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.Entity.EmiSchedule;
import com.example.Loan_Project.Entity.LoanAccount;
import com.example.Loan_Project.Enum.LoanAccountStatus;
import com.example.Loan_Project.Repository.EmiScheduleRepository;
import com.example.Loan_Project.Repository.LoanAccountRepository;
import com.example.Loan_Project.Service.PenaltyService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PenaltyAccrualJob {

    private static final Logger log = LoggerFactory.getLogger(PenaltyAccrualJob.class);
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Kolkata");

    private final EmiScheduleRepository emiScheduleRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final PenaltyService penaltyService;


    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Kolkata")
    @Transactional
    public void processOverdueEmis() {
        LocalDate today = LocalDate.now(BUSINESS_ZONE);

        for (LoanAccount account : loanAccountRepository.findAll()) {
            List<EmiSchedule> schedules =
                    emiScheduleRepository.findSchedules(account.getLoanAccountId());

            for (EmiSchedule schedule : schedules) {
                if ("Pending".equalsIgnoreCase(schedule.getPaymentStatus())
                        && schedule.getDueDate() != null
                        && today.isAfter(schedule.getDueDate())) {
                    penaltyService.accruePenaltiesForSchedule(schedule);
                }
            }

            if (account.getLoanStatus() == LoanAccountStatus.CLOSED
                    || account.getLoanStatus() == LoanAccountStatus.BLOCKED) {
                continue;
            }

            int consecutiveMisses = 0;
            int maxConsecutiveMisses = 0;
            for (EmiSchedule schedule : schedules) {
                boolean overdueAndUnpaid =
                        "Pending".equalsIgnoreCase(schedule.getPaymentStatus())
                        && schedule.getDueDate() != null
                        && today.isAfter(schedule.getDueDate());

                if (overdueAndUnpaid) {
                    consecutiveMisses++;
                    maxConsecutiveMisses = Math.max(maxConsecutiveMisses, consecutiveMisses);
                } else {
                    consecutiveMisses = 0;
                }
            }

            boolean warningAlreadyLogged = Boolean.TRUE.equals(account.getMissedEmiWarningLogged());
            if (maxConsecutiveMisses >= 2 && !warningAlreadyLogged) {
                log.warn("Loan account {} has two or more consecutive missed EMIs; review required",
                        account.getLoanAccountId());
                account.setMissedEmiWarningLogged(true);
                loanAccountRepository.save(account);
            } else if (maxConsecutiveMisses < 2 && warningAlreadyLogged) {
                account.setMissedEmiWarningLogged(false);
                loanAccountRepository.save(account);
            }

            if (maxConsecutiveMisses >= 3) {
                account.setLoanStatus(LoanAccountStatus.BLOCKED);
                loanAccountRepository.save(account);
                log.warn("Loan account {} blocked after three consecutive missed EMIs",
                        account.getLoanAccountId());
            }
        }
    }
}
