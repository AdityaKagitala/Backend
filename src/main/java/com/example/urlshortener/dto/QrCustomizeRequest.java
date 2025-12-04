package com.example.urlshortener.dto;

import lombok.Data;

@Data
public class QrCustomizeRequest {
    private String url;
    private String color;     // hex like #000000
    private String bgColor;   // background hex
    private int size;         // example 300
    private String logoUrl;   // optional - add logo inside QR
}
