package com.example.urlshortener.controller;

import com.example.urlshortener.model.CustomQrCode;
import com.example.urlshortener.repository.CustomQrCodeRepository;
import com.example.urlshortener.service.QrTrackingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/custom-qr")
public class QrScanController {

    @Autowired
    private CustomQrCodeRepository qrRepo;

    @Autowired
    private QrTrackingService trackingService;

    @GetMapping("/scan/{id}")
    public void scanQr(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) throws IOException {

        CustomQrCode qr = qrRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("QR code not found"));

        trackingService.trackScan(qr, request);

        response.sendRedirect(qr.getUrl()); // redirect to real URL
    }
}
