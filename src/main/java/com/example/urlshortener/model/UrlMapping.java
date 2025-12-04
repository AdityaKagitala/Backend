package com.example.urlshortener.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Entity
@Table(name = "url_mappings", indexes = {
        @Index(name = "idx_shortcode", columnList = "shortCode", unique = true),
        @Index(name = "idx_domain_shortcode", columnList = "domain, shortCode") // ⭐ required
})
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ⭐ Custom Domain
    // If null → use default domain (your main site)
    @Column(nullable = true, length = 255)
    private String domain;
    // Examples:
    // null → https://yourapp.com/r/abc123
    // "mybrand.com" → https://mybrand.com/abc123

    @Column(nullable = false, length = 2000)
    private String originalUrl;

    @Column(nullable = false, unique = false, length = 50)
    private String shortCode;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Long clickCount = 0L;

    private String faviconUrl;

    // Link Preview
    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String description;
    private String imageUrl;

    // User
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Analytics
    @OneToMany(mappedBy = "urlMapping", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ViewLink> viewLinks = new ArrayList<>();

    public void incrementClickCount() {
        this.clickCount = this.clickCount + 1;
    }
}