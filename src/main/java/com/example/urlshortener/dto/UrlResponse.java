package com.example.urlshortener.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UrlResponse {
    private String originalUrl;
    private String shortUrl;
    private String shortCode;
    private String faviconUrl;

 //   private String qrCode;

}
