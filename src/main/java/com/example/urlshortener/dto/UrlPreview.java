package com.example.urlshortener.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UrlPreview {
    private String title;
    private String description;
    private String image;
    private String favicon;
}
