package com.example.urlshortener.dto;

import lombok.Data;

@Data
public class UrlRequest {

    private String originalUrl;

    private String customAlias;

    // ⭐ NEW — custom domain support
    // If null → default domain
    private String customDomain;
}