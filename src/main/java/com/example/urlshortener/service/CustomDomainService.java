package com.example.urlshortener.service;

import com.example.urlshortener.model.CustomDomain;
import com.example.urlshortener.model.User;
import com.example.urlshortener.model.DomainStatus;
import com.example.urlshortener.repository.CustomDomainRepository;
import com.example.urlshortener.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomDomainService {

    private final CustomDomainRepository domainRepo;
    private final UserRepository userRepo;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private String normalizeDomain(String raw) {
        if (raw == null) throw new IllegalArgumentException("domain missing");
        String d = raw.trim().toLowerCase(Locale.ROOT);
        d = d.replaceAll("^https?://", "");
        d = d.replaceAll("/$", "");
        // remove possible trailing ports
        d = d.replaceAll(":\\d+$", "");
        return d;
    }

    /**
     * Create domain request. Returns entity including verificationCode to show user.
     */
    @Transactional
    public CustomDomain addDomain(String rawDomain) {
        String domain = normalizeDomain(rawDomain);

        // basic validation (reject IPs)
        if (domain.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$")) {
            throw new IllegalArgumentException("Invalid domain");
        }

        if (domainRepo.existsByDomain(domain)) {
            throw new IllegalArgumentException("Domain already registered");
        }

        User user = getCurrentUser();

        String verificationCode = "urlmaster-verification=" + UUID.randomUUID().toString().replace("-", "");

        CustomDomain cd = CustomDomain.builder()
                .domain(domain)
                .verificationCode(verificationCode)
                .status(DomainStatus.PENDING)
                .user(user)
                .createdAt(Instant.now())
                .build();

        return domainRepo.save(cd);
    }

    /**
     * List domains for current user
     */
    public List<CustomDomain> listDomains() {
        User user = getCurrentUser();
        return domainRepo.findAllByUser(user);
    }

    /**
     * Verify a domain now (by id). Returns true if verified.
     */
    @Transactional
    public boolean verifyDomainNow(Long id) {
        CustomDomain cd = domainRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Domain not found"));

        // already verified
        if(cd.getStatus() == DomainStatus.VERIFIED){
            return true;
        }

        boolean ok = DomainDnsVerifier.txtRecordContains(cd.getDomain(), cd.getVerificationCode());
        cd.setLastChecked(Instant.now());

        if (ok) {
            cd.setVerifiedAt(Instant.now());
            cd.setStatus(DomainStatus.VERIFIED);
        } else {
            cd.setStatus(DomainStatus.PENDING);
        }

        domainRepo.save(cd);
        return ok;
    }

    /**
     * Delete domain (owner only)
     */
    @Transactional
    public void deleteDomain(Long id) {
        CustomDomain cd = domainRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Domain not found"));
        User user = getCurrentUser();
        if (!cd.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Not allowed");
        }
        domainRepo.delete(cd);
    }

    /**
     * Find domain entity by domain string (used by UrlService)
     */
    public CustomDomain findByDomain(String domain) {
        return domainRepo.findByDomain(normalizeDomain(domain)).orElse(null);
    }
}