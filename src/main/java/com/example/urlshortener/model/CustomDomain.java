package com.example.urlshortener.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table( name = "custom_domains",
        uniqueConstraints = { @UniqueConstraint(columnNames = "domain")} )
public class CustomDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The actual domain the user wants to use (ex: links.mybrand.com)
    @Column(nullable = false, unique = true)
    private String domain;

    // DNS TXT verification token (ex: urlmaster-verification=xxxx)
    @Column(nullable = false)
    private String verificationCode;

    // Current verification status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DomainStatus status;

    // The owner of the domain
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Timestamp fields
    private Instant createdAt;

    private Instant verifiedAt;    // when it actually became verified

    private Instant lastChecked;   // last time the system checked DNS
}