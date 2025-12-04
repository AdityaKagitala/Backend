package com.example.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UrlPreviewForQr {

    private String title;
    private String description;
    private String image;
    private String favicon;
}
