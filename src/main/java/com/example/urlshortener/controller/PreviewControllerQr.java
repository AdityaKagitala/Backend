package com.example.urlshortener.controller;

import com.example.urlshortener.dto.UrlPreviewForQr;
import com.example.urlshortener.model.CustomQrCode;
import com.example.urlshortener.service.QrCustomizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qr-preview")
@RequiredArgsConstructor
public class PreviewControllerQr {

    private final QrCustomizeService qrService;

    @GetMapping("/{qrid}")
    public ResponseEntity<UrlPreviewForQr> getPreview(@PathVariable Long qrid) {

        CustomQrCode qr = qrService.getQrById(qrid).orElse(null);
        if (qr == null) return ResponseEntity.notFound().build();

        UrlPreviewForQr dto = new UrlPreviewForQr(
                qr.getTitle(),
                qr.getDescription(),
                qr.getImageUrl(),
                qr.getFaviconUrl()
        );

        return ResponseEntity.ok(dto);
    }
}