package com.example.urlshortener.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "custom_qr_codes")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomQrCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;

    @Column(columnDefinition = "LONGTEXT")
    private String qrBase64;

    private String color;
    private String bgColor;
    private int size;
    private String logoUrl;

    //Link Preview
    private String title;
    private String description;
    private String imageUrl;
    private String faviconUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private Instant createdAt;
}
