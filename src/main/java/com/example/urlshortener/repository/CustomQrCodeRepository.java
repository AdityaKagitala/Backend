package com.example.urlshortener.repository;

import com.example.urlshortener.model.CustomQrCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomQrCodeRepository extends JpaRepository<CustomQrCode, Long> {
    Optional<CustomQrCode>findById(Long id);
    List<CustomQrCode> findByUserIdOrderByCreatedAtDesc(Long userId);
}
