package com.example.urlshortener.controller;

import com.example.urlshortener.model.User;
import com.example.urlshortener.service.PlanService;
import com.example.urlshortener.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final PlanService planService;
    private final SecurityUtils securityUtils;

    /**
     * Client calls this to create a payment session (Stripe, Razorpay etc).
     * Here we only simulate creation - integrate with provider SDK for real flows.
     */
    @PostMapping("/create-checkout")
    public ResponseEntity<?> createCheckout(@RequestParam(defaultValue = "1") int months) {
        // Return a stub object - replace with real provider session creation
        return ResponseEntity.ok().body(java.util.Map.of(
                "checkoutUrl", "https://payment-gateway.example/checkout-session-id",
                "months", months
        ));
    }

    /**
     * Payment webhook endpoint – called by payment provider.
     * You must validate signature & event, then apply upgrade.
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> paymentWebhook(@RequestBody String payload, @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
        // TODO: Validate signature, parse event, find user by metadata, then:
        // Example: userId found -> planService.upgradeToPremium(user, months)
        // For now just return OK placeholder.
        return ResponseEntity.ok("received");
    }

    /**
     * For manual upgrade (testing) - developer/admin use only.
     */
    @PostMapping("/upgrade-test")
    public ResponseEntity<?> upgradeTest(@RequestParam int months) {
        User user = securityUtils.getCurrentUser();
        planService.upgradeToPremium(user, months);
        return ResponseEntity.ok("upgraded");
    }
}