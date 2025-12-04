package com.example.urlshortener.service;

import com.example.urlshortener.model.PlanType;
import com.example.urlshortener.model.User;
import org.springframework.stereotype.Service;

@Service
public class FeatureAccessService {

    public void requiredPremium(User user) {
        if(user == null) throw new RuntimeException("Unauthenticated");
        if (user.getPlan() != PlanType.PREMIUM) {
            throw new RuntimeException("Feature restricted to Premium user.Please upgrade.");
        }
    }

    public boolean isPremium(User user) {
        return user != null && user.getPlan() == PlanType.PREMIUM;
    }
}
