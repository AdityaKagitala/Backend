package com.example.urlshortener.service;

import com.example.urlshortener.model.CustomDomain;
import com.example.urlshortener.model.DomainStatus;
import com.example.urlshortener.repository.CustomDomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DomainAutoVerifier {

    private final CustomDomainRepository repo;

    // runs every 10 minutes (configurable)
    @Scheduled(fixedDelayString = "${domains.autoverify.interval.ms:600000}")
    public void autoVerify() {
        List<CustomDomain> pending = repo.findAllByStatus(DomainStatus.PENDING);
        for (CustomDomain cd : pending) {
            boolean ok = DomainDnsVerifier.txtRecordContains(cd.getDomain(), cd.getVerificationCode());
            cd.setLastChecked(Instant.now());
            if (ok) {
                cd.setVerifiedAt(Instant.now());
                cd.setStatus(DomainStatus.VERIFIED);
            }
            repo.save(cd);
        }
    }
}