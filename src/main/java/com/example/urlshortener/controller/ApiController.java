package com.example.urlshortener.controller;

import com.example.urlshortener.dto.UrlHistoryResponse;
import com.example.urlshortener.dto.UrlRequest;
import com.example.urlshortener.dto.UrlResponse;
import com.example.urlshortener.dto.StatsResponse;
import com.example.urlshortener.model.UrlMapping;
import com.example.urlshortener.service.UrlService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api")
public class ApiController {

    private final UrlService urlService;

    @Value("${app.default-domain}")
    private String defaultDomain;

    @Value("${app.base.url}")  // used for fallback
    private String appBaseUrl;

    public ApiController(UrlService urlService) {
        this.urlService = urlService;
    }


    /** CREATE SHORT URL (supports custom domain) */
    @PostMapping("/shorten")
    public ResponseEntity<UrlResponse> shorten(@Valid @RequestBody UrlRequest req) {

        UrlMapping mapping = urlService.createShortUrl(req);

        String shortUrl;

        // ⭐ If custom domain was used
        if (mapping.getDomain() != null) {
            shortUrl = "https://" + mapping.getDomain() + "/" + mapping.getShortCode();
        }
        // ⭐ Default domain → keep /r/{code}
        else {
            shortUrl = appBaseUrl.endsWith("/")
                    ? appBaseUrl + mapping.getShortCode()
                    : appBaseUrl + "/" + mapping.getShortCode();
        }

        UrlResponse resp = new UrlResponse(
                mapping.getOriginalUrl(),
                shortUrl,
                mapping.getShortCode(),
                mapping.getFaviconUrl()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }



    /** GET STATS (default only — custom domain same code) */
    @GetMapping("/stats/{shortCode}")
    public ResponseEntity<StatsResponse> stats(@PathVariable String shortCode) {
        return urlService.findByShortCodeDefault(shortCode)
                .map(m -> {
                    StatsResponse s = new StatsResponse(
                            m.getShortCode(),
                            m.getOriginalUrl(),
                            m.getClickCount(),
                            m.getCreatedAt()
                    );
                    return ResponseEntity.ok(s);
                })
                .orElse(ResponseEntity.notFound().build());
    }



    /** HISTORY LISTING */
    @GetMapping("/history")
    public ResponseEntity<List<UrlHistoryResponse>> getHistory() {

        List<UrlMapping> mappings = urlService.getHistory();

        List<UrlHistoryResponse> responseList = mappings.stream().map(mapping -> {

            String shortUrl;

            if (mapping.getDomain() != null) {
                shortUrl = mapping.getDomain() + "/" + mapping.getShortCode();
            } else {
                shortUrl = appBaseUrl.endsWith("/")
                        ? appBaseUrl + mapping.getShortCode()
                        : appBaseUrl + "/" + mapping.getShortCode();
            }

            return new UrlHistoryResponse(
                    mapping.getOriginalUrl(),
                    shortUrl,
                    mapping.getCreatedAt(),
                    mapping.getShortCode(),
                    mapping.getFaviconUrl(),
                    mapping.getImageUrl(),
                    mapping.getTitle(),
                    mapping.getDescription()
            );

        }).toList();

        return ResponseEntity.ok(responseList);
    }



    /** DELETE SHORT URL */
    @DeleteMapping("/delete/{shortCode}")
    public ResponseEntity<Void> deleteUrl(@PathVariable String shortCode) {
        boolean deleted = urlService.deleteByShortCode(shortCode);
        return deleted ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}