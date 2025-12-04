package com.example.urlshortener.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ViewLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String shortCode;
    private String deviceType;
    private String country;
    private String region;
    private String referrer;
    private LocalDateTime clickedAt;
    @Column(length = 100)
    private String platform;
    private String Browser;


    @ManyToOne
    private UrlMapping urlMapping;

}
