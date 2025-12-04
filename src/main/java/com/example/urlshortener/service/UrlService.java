package com.example.urlshortener.service;

import com.example.urlshortener.dto.UrlPreview;
import com.example.urlshortener.dto.UrlRequest;
import com.example.urlshortener.model.CustomDomain;
import com.example.urlshortener.model.DomainStatus;
import com.example.urlshortener.model.UrlMapping;
import com.example.urlshortener.model.User;
import com.example.urlshortener.repository.UrlRepository;

import com.example.urlshortener.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private final ShortCodeGenerator codeGenerator;
    private final SecurityUtils securityUtils;
    private final UsageService usageService;
    private final CustomDomainService customDomainService;

    @Value("${app.shortcode.length:6}")
    private int codeLength;

    private static final int MAX_ATTEMPTS = 6;


    // ──────────────────────────────────────────
    // CREATE SHORT URL (supports: alias, domain, usage limits)
    // ──────────────────────────────────────────
    @Transactional
    public UrlMapping createShortUrl(UrlRequest request) {

        User user = securityUtils.getCurrentUser();

        // Enforce free plan limits
        usageService.ensureCanCreateLink(user);

        String originalUrl = request.getOriginalUrl();
        String alias = request.getCustomAlias();
        String submittedDomain = request.getCustomDomain();

        // Preview meta extraction
        UrlPreview preview = fetchPreviewMetadata(originalUrl);

        // DOMAIN LOGIC
        String domainToUse = null;
        if (submittedDomain != null && !submittedDomain.isBlank()) {
            domainToUse = submittedDomain.trim().toLowerCase();

            CustomDomain cd = customDomainService.findByDomain(domainToUse);
            if (cd == null || cd.getStatus() != DomainStatus.VERIFIED)
                throw new RuntimeException("Custom domain not verified.");

            if (!cd.getUser().getId().equals(user.getId()))
                throw new RuntimeException("This domain belongs to another user.");
        }


        // ──────────────────────────────────────────
        // CASE 1 → CUSTOM ALIAS
        // ──────────────────────────────────────────
        if (alias != null && !alias.isBlank()) {

            String code = alias.trim().toLowerCase();

            boolean exists = (domainToUse == null)
                    ? urlRepository.existsByDomainIsNullAndShortCode(code)
                    : urlRepository.existsByDomainAndShortCode(domainToUse, code);

            if (exists)
                throw new RuntimeException("Alias already used.");

            UrlMapping mapping = buildUrlMapping(originalUrl, code, domainToUse, user, preview);
            UrlMapping saved = urlRepository.save(mapping);

            usageService.incrementLinks(user, 1);

            return saved;
        }


        // ──────────────────────────────────────────
        // CASE 2 → AUTO-GENERATED SHORTCODE
        // ──────────────────────────────────────────
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String code = codeGenerator.random(codeLength);

            boolean exists = (domainToUse == null)
                    ? urlRepository.existsByDomainIsNullAndShortCode(code)
                    : urlRepository.existsByDomainAndShortCode(domainToUse, code);

            if (exists) continue;

            UrlMapping mapping = buildUrlMapping(originalUrl, code, domainToUse, user, preview);

            try {
                UrlMapping saved = urlRepository.save(mapping);
                usageService.incrementLinks(user, 1);
                return saved;
            } catch (DataIntegrityViolationException ignore) {
                // collision → retry
            }
        }

        throw new RuntimeException("Could not generate a unique shortcode. Try again.");
    }



    // ──────────────────────────────────────────
    // BUILD URL ENTITY
    // ──────────────────────────────────────────
    private UrlMapping buildUrlMapping(String originalUrl, String code, String domain,
                                       User user, UrlPreview preview) {

        return UrlMapping.builder()
                .originalUrl(originalUrl)
                .shortCode(code)
                .domain(domain)
                .createdAt(Instant.now())
                .clickCount(0L)
                .user(user)

                // preview
                .title(preview.getTitle())
                .description(preview.getDescription())
                .imageUrl(preview.getImage())
                .faviconUrl(getFaviconUrl(originalUrl))

                .build();
    }



    // ──────────────────────────────────────────
    // REDIRECTION HELPERS
    // ──────────────────────────────────────────
    public Optional<UrlMapping> findByShortCodeDefault(String code) {
        return urlRepository.findByDomainIsNullAndShortCode(code);
    }

    public Optional<UrlMapping> findByDomainAndShortCode(String domain, String code) {
        return urlRepository.findByDomainAndShortCode(domain, code);
    }



    // ──────────────────────────────────────────
    // INCREMENT CLICKS
    // ──────────────────────────────────────────
    @Transactional
    public void incrementClicks(UrlMapping mapping) {
        mapping.incrementClickCount();
        urlRepository.save(mapping);
    }



    // ──────────────────────────────────────────
    // URL HISTORY
    // ──────────────────────────────────────────
    public java.util.List<UrlMapping> getHistory() {
        User user = securityUtils.getCurrentUser();
        return urlRepository.findAllByUser(user);
    }



    // ──────────────────────────────────────────
    // DELETE URL
    // ──────────────────────────────────────────
    @Transactional
    public boolean deleteByShortCode(String code) {

        // default domain
        Optional<UrlMapping> defaultMap = urlRepository.findByDomainIsNullAndShortCode(code);
        if (defaultMap.isPresent()) {
            urlRepository.delete(defaultMap.get());
            return true;
        }

        // custom domain
        Optional<UrlMapping> customMap = urlRepository.findByShortCode(code);
        if (customMap.isPresent()) {
            urlRepository.delete(customMap.get());
            return true;
        }

        return false;
    }



    // ──────────────────────────────────────────
    // METADATA SCRAPER (JSOUP)
    // ──────────────────────────────────────────
    public UrlPreview fetchPreviewMetadata(String url) {
        try {
            Document doc = Jsoup.connect(url).timeout(4000).get();

            String title = doc.select("meta[property=og:title]").attr("content");
            if (title.isEmpty()) title = doc.title();

            String description = doc.select("meta[property=og:description]").attr("content");
            if (description.isEmpty())
                description = doc.select("meta[name=description]").attr("content");

            String image = doc.select("meta[property=og:image]").attr("content");

            return new UrlPreview(
                    title != null ? title : "No title",
                    description != null ? description : "No description",
                    image,
                    getFaviconUrl(url)
            );

        } catch (Exception e) {
            return new UrlPreview(
                    "Preview Not Available",
                    "No description found",
                    null,
                    getFaviconFallback(url)
            );
        }
    }



    // ──────────────────────────────────────────
    // DOMAIN UTILITIES
    // ──────────────────────────────────────────
    public String extractDomain(String url) {
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://"))
                url = "https://" + url;

            URI uri = new URI(url);
            return uri.getScheme() + "://" + uri.getHost();

        } catch (Exception e) {
            return "https://google.com";
        }
    }

    public String getFaviconUrl(String url) {
        return extractDomain(url) + "/favicon.ico";
    }

    public String getFaviconFallback(String url) {
        return "https://www.google.com/s2/favicons?domain=" + url;
    }
}