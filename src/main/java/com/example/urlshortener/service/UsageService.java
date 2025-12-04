package com.example.urlshortener.service;

import com.example.urlshortener.model.PlanType;
import com.example.urlshortener.model.User;
import com.example.urlshortener.model.UserUsage;
import com.example.urlshortener.repository.UserUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsageService {

    private final UserUsageRepository usageRepo;

    // Free limits (configurable)
    private final int FREE_MONTHLY_LINKS = 100;
    private final int FREE_MONTHLY_QR = 50;

    @Transactional
    public UserUsage getOrCreate(User user) {
        return usageRepo.findByUser(user).orElseGet(() -> {
            UserUsage u = UserUsage.builder().user(user).linksCreatedThisMonth(0).qrCreatedThisMonth(0).build();
            return usageRepo.save(u);
        });
    }

    @Transactional
    public void ensureCanCreateLink(User user) {
        if (user.getPlan() == PlanType.PREMIUM) return;
        UserUsage usage = getOrCreate(user);
        if (usage.getLinksCreatedThisMonth() >= FREE_MONTHLY_LINKS) {
            throw new RuntimeException("Monthly link creation limit reached. Upgrade to Premium.");
        }
    }

    @Transactional
    public void incrementLinks(User user, int amount) {
        UserUsage usage = getOrCreate(user);
        usage.setLinksCreatedThisMonth(usage.getLinksCreatedThisMonth() + amount);
        usageRepo.save(usage);
    }

    @Transactional
    public void ensureCanCreateQr(User user) {
        if (user.getPlan() == PlanType.PREMIUM) return;
        UserUsage usage = getOrCreate(user);
        if (usage.getQrCreatedThisMonth() >= FREE_MONTHLY_QR) {
            throw new RuntimeException("Monthly QR generation limit reached. Upgrade to Premium.");
        }
    }

    @Transactional
    public void incrementQr(User user, int amount) {
        UserUsage usage = getOrCreate(user);
        usage.setQrCreatedThisMonth(usage.getQrCreatedThisMonth() + amount);
        usageRepo.save(usage);
    }

    @Transactional
    public void resetUsage(UserUsage usage) {
        usage.setLinksCreatedThisMonth(0);
        usage.setQrCreatedThisMonth(0);
        usage.setLastReset(java.time.Instant.now());
        usageRepo.save(usage);
    }
}