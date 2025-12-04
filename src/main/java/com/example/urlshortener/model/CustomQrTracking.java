package com.example.urlshortener.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "custom_qr_tracking")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomQrTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private CustomQrCode qrCode;

    private Instant scannedAt;
    private String ipAddress;
    private String country;
    private String deviceType;
    private String browser;

    @Column(columnDefinition = "TEXT")
    private String userAgent;
}
