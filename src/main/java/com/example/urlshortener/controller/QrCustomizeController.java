package com.example.urlshortener.controller;

import com.example.urlshortener.dto.QrCustomizeRequest;
import com.example.urlshortener.model.CustomQrCode;
import com.example.urlshortener.service.QrCustomizeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/custom-qr")
public class QrCustomizeController {

    private final QrCustomizeService qrCustomizeService;

    public QrCustomizeController(QrCustomizeService qrCustomizeService) {
        this.qrCustomizeService = qrCustomizeService;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> createCustomQr(@RequestBody QrCustomizeRequest req, Principal principal) {

        CustomQrCode qr = qrCustomizeService.saveCustomQr(req, principal.getName());

        return ResponseEntity.ok(Map.of(
                "qrCode", qr.getQrBase64()
        ));
    }

    @GetMapping("/history")
    public ResponseEntity<?> getUserQrHistory(Principal principal) {

        String username = principal.getName();

        List<CustomQrCode> list = qrCustomizeService.getQrHistory(username);

        return ResponseEntity.ok(list);
    }
}