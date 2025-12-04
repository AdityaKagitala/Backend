package com.example.urlshortener.controller;

import com.example.urlshortener.dto.ViewLinkAnalyticsDTO;
import com.example.urlshortener.model.UrlMapping;
import com.example.urlshortener.model.ViewLink;
import com.example.urlshortener.repository.ViewLinkRepository;
import com.example.urlshortener.service.UrlService;
import com.example.urlshortener.service.ViewLinkService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Optional;

@Controller
public class RedirectController {

    private final UrlService urlService;
    private final ViewLinkRepository viewLinkRepository;
    private final ViewLinkService viewLinkService;

    public RedirectController(UrlService urlService,
                              ViewLinkRepository viewLinkRepository,
                              ViewLinkService viewLinkService) {
        this.urlService = urlService;
        this.viewLinkRepository = viewLinkRepository;
        this.viewLinkService = viewLinkService;
    }


    // ============================================================
    //   ⭐⭐ CUSTOM DOMAIN + DEFAULT DOMAIN REDIRECT HANDLER ⭐⭐
    // ============================================================
    @GetMapping("/{shortCode}")
    public ResponseEntity<Object> redirect(
            @PathVariable String shortCode,
            HttpServletRequest request
    ) {

        // EXTRACT ACTUAL DOMAIN USED
        String domain = request.getServerName().toLowerCase();

        // 1️⃣ FIRST TRY custom-domain lookup
        Optional<UrlMapping> customDomainUrl =
                urlService.findByDomainAndShortCode(domain, shortCode);

        UrlMapping mapping;

        if (customDomainUrl.isPresent()) {
            mapping = customDomainUrl.get();
        } else {
            // 2️⃣ FALLBACK to default domain (domain=null)
            mapping = urlService.findByShortCodeDefault(shortCode)
                    .orElseThrow(() -> new RuntimeException("Short URL not found"));
        }


        // -------------------------- BOT DETECTION FOR PREVIEW --------------------------
        String agent = request.getHeader("User-Agent") != null
                ? request.getHeader("User-Agent").toLowerCase()
                : "";

        boolean isBot =
                agent.contains("facebook") ||
                        agent.contains("twitter") ||
                        agent.contains("whatsapp") ||
                        agent.contains("telegram") ||
                        agent.contains("slackbot") ||
                        agent.contains("discord");

        if (isBot) {
            ModelAndView mav = new ModelAndView("preview");
            mav.addObject("title", mapping.getTitle());
            mav.addObject("description", mapping.getDescription());
            mav.addObject("image", mapping.getImageUrl());
            mav.addObject("originalUrl", mapping.getOriginalUrl());
            return new ResponseEntity<>(mav, HttpStatus.OK);
        }


        // -------------------------- ANALYTICS TRACKING --------------------------
        String userAgent = request.getHeader("User-Agent") != null
                ? request.getHeader("User-Agent").toLowerCase()
                : "";

        String deviceType = userAgent.contains("mobi") ? "mobile" : "desktop";
        String browser = getBrowser(userAgent);

        String referer = request.getHeader("Referer");
        String platform = getReferringPlatform(referer);

        viewLinkService.saveClickData(
                shortCode,       // code
                deviceType,
                referer,
                platform,
                browser
        );

        urlService.incrementClicks(mapping);


        // -------------------------- REDIRECT USER --------------------------
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", mapping.getOriginalUrl());
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }


    // ============================================================
    //                  HELPERS (unchanged)
    // ============================================================

    private String getReferringPlatform(String referer) {
        if (referer == null || referer.isEmpty()) return "Direct";

        referer = referer.toLowerCase();

        if (referer.contains("facebook.com")) return "Facebook";
        if (referer.contains("twitter.com")) return "Twitter";
        if (referer.contains("t.me") || referer.contains("telegram.me")) return "Telegram";
        if (referer.contains("youtube.com") || referer.contains("youtu.be")) return "YouTube";
        if (referer.contains("linkedin.com")) return "LinkedIn";
        if (referer.contains("instagram.com")) return "Instagram";

        try {
            java.net.URI uri = new java.net.URI(referer);
            String host = uri.getHost();
            if (host == null) return "Unknown";
            if (host.startsWith("www.")) host = host.substring(4);
            return host;
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private String getBrowser(String userAgent) {
        if (userAgent.contains("chrome") && !userAgent.contains("edge") && !userAgent.contains("opr")) return "Chrome";
        if (userAgent.contains("firefox")) return "Firefox";
        if (userAgent.contains("safari") && !userAgent.contains("chrome")) return "Safari";
        if (userAgent.contains("edge")) return "Edge";
        if (userAgent.contains("opr") || userAgent.contains("opera")) return "Opera";
        return "Unknown";
    }


    // ============================================================
    //                ANALYTICS ENDPOINT (unchanged)
    // ============================================================
    @GetMapping("/api/viewLink/{shortCode}")
    public ResponseEntity<List<ViewLinkAnalyticsDTO>> viewLinkDetails(@PathVariable String shortCode) {

        Optional<UrlMapping> mappingOpt = urlService.findByShortCodeDefault(shortCode);

        if (mappingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UrlMapping mapping = mappingOpt.get();
        List<ViewLink> viewLinks = viewLinkRepository.findByUrlMapping(mapping);

        List<ViewLinkAnalyticsDTO> dtoList = viewLinks.stream().map(viewLink -> {
            ViewLinkAnalyticsDTO dto = new ViewLinkAnalyticsDTO();
            dto.setClickCount(mapping.getClickCount());
            dto.setOriginalUrl(mapping.getOriginalUrl());
            dto.setShortCode(mapping.getShortCode());
            dto.setDeviceType(viewLink.getDeviceType());
            dto.setCountry(viewLink.getCountry());
            dto.setRegion(viewLink.getRegion());
            dto.setReferrer(viewLink.getReferrer());
            dto.setClickedAt(viewLink.getClickedAt());
            dto.setPlatform(viewLink.getPlatform());
            dto.setBrowser(viewLink.getBrowser());
            dto.setTitle(mapping.getTitle());
            dto.setDescription(mapping.getDescription());
            dto.setImageUrl(mapping.getImageUrl());
            dto.setFaviconUrl(mapping.getFaviconUrl());
            return dto;
        }).toList();

        return ResponseEntity.ok(dtoList);
    }
}