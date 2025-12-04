package com.example.urlshortener.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table( name = "user_usage",indexes = {@Index(name = "idx_user_usage_user",columnList = "user_id")} )
public class UserUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id",unique = true,nullable = false)
    private User user;

    @Column(nullable = false)
    private int linksCreatedThisMonth = 0;

    @Column(nullable = false)
    private int qrCreatedThisMonth = 0;

    private Instant lastReset;
}
