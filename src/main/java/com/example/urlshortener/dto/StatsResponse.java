package com.example.urlshortener.dto;

import java.time.Instant;

public class StatsResponse {
    private String shortCode;
    private String originalUrl;
    private Long clickCount;
    private Instant createdAt;

    public StatsResponse() {}

    public StatsResponse(String shortCode, String originalUrl, Long clickCount, Instant createdAt) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.clickCount = clickCount;
        this.createdAt = createdAt;
    }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }

    public Long getClickCount() { return clickCount; }
    public void setClickCount(Long clickCount) { this.clickCount = clickCount; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}