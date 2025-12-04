package com.example.urlshortener.scheduler;

import com.example.urlshortener.model.UserUsage;
import com.example.urlshortener.repository.UserUsageRepository;
import com.example.urlshortener.service.PlanService;
import com.example.urlshortener.service.UsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PlanSchedulers {

    private final UserUsageRepository usageRepo;
    private final UsageService usageService;
    private final PlanService planService;

    /**
     * Reset monthly usage at 00:00 on the 1st of every month
     * Cron format: second minute hour day-of-month month day-of-week
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional
    public void resetMonthlyUsage() {
        List<UserUsage> list = usageRepo.findAll();
        list.forEach(usageService::resetUsage);
    }

    /**
     * Downgrade expired premium plans, run every hour.
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void downgradeExpired() {
        planService.downgradeExpiredUsers();
    }
}