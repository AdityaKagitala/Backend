package com.example.urlshortener.dto;

import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;


@Data
public class ViewLinkAnalyticsDTO {

    private Long clickCount;

    private String originalUrl;

    private String shortCode;
    private String deviceType;
    private String country;
    private String region;
    private String referrer;
    private LocalDateTime clickedAt;
    @Column(length = 100)
    private String platform;
    private String browser;

    private String title;
    private String description;
    private String imageUrl;
    private String faviconUrl;
}
