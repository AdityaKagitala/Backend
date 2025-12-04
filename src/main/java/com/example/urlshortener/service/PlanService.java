package com.example.urlshortener.service;

import com.example.urlshortener.model.PlanType;
import com.example.urlshortener.model.User;
import com.example.urlshortener.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final UserRepository userRepo;

    @Transactional
    public User upgradeToPremium(User user, int months) {
        user.setPlan(PlanType.PREMIUM);
        user.setPlanExpiry(Instant.now().plus(months, ChronoUnit.MONTHS));
        return userRepo.save(user);
    }

    @Transactional
    public User downgradeToFree(User user) {
        user.setPlan(PlanType.FREE);
        user.setPlanExpiry(null);
        return userRepo.save(user);
    }

    @Transactional
    public void downgradeExpiredUsers() {
        Instant now = Instant.now();
        userRepo.findAll().stream()
                .filter(u -> u.getPlan() == PlanType.PREMIUM && u.getPlanExpiry() != null && u.getPlanExpiry().isBefore(now))
                .forEach(u -> {
                    u.setPlan(PlanType.FREE);
                    u.setPlanExpiry(null);
                    userRepo.save(u);
                });
    }
}