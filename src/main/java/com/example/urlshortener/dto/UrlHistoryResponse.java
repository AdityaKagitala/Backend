package com.example.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UrlHistoryResponse {
    private String originalUrl;
    private String shortUrl;
    private Instant createdAt;
    private String shortCode;
    private String faviconUrl;
    private String imageUrl;

    private String title;
    private String description;
}
