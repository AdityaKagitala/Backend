package com.example.urlshortener.repository;

import com.example.urlshortener.model.UrlMapping;
import com.example.urlshortener.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UrlRepository extends JpaRepository<UrlMapping, Long> {

    // ──────────────────────────────────────────
    // EXISTING METHODS (kept as-is)
    // ──────────────────────────────────────────
    // But now these are ONLY for default domain URLs (domain = null)
    Optional<UrlMapping> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    List<UrlMapping> findAllByUser(User user);



    // ──────────────────────────────────────────────
    // NEW METHODS for custom domain support
    // ──────────────────────────────────────────────

    // Find URL by domain + code (custom-domain links)
    Optional<UrlMapping> findByDomainAndShortCode(String domain, String shortCode);

    // Find fallback when domain = null (default URLs)
    Optional<UrlMapping> findByDomainIsNullAndShortCode(String shortCode);

    // Check if code exists for a given domain
    boolean existsByDomainAndShortCode(String domain, String shortCode);

    // For default domain only
    boolean existsByDomainIsNullAndShortCode(String shortCode);
}