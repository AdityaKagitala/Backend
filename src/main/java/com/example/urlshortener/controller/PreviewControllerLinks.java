package com.example.urlshortener.controller;

import com.example.urlshortener.dto.UrlPreview;
import com.example.urlshortener.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/preview")
@RequiredArgsConstructor
public class PreviewControllerLinks {

    private final UrlService urlService;

    // -------- 1️⃣ URL Preview from CLIENT (Home page) ----------
    @PostMapping
    public ResponseEntity<UrlPreview> getPreviewByUrl(@RequestBody Map<String, String> body) {
        String url = body.get("url");

        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        UrlPreview preview = urlService.fetchPreviewMetadata(url);
        return ResponseEntity.ok(preview);
    }

    // -------- 2️⃣ Preview from DATABASE using shortCode (Analytics) ----------
    @GetMapping("/{shortCode}")
    public ResponseEntity<UrlPreview> getPreviewFromDb(@PathVariable String shortCode) {

        return urlService.findByShortCodeDefault(shortCode)
                .map(mapping -> ResponseEntity.ok(
                        new UrlPreview(
                                mapping.getTitle(),
                                mapping.getDescription(),
                                mapping.getImageUrl(),
                                mapping.getFaviconUrl()
                        )
                ))
                .orElse(ResponseEntity.notFound().build());
    }
}