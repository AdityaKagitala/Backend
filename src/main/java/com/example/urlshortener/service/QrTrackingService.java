package com.example.urlshortener.service;

import com.example.urlshortener.model.CustomQrCode;
import com.example.urlshortener.model.CustomQrTracking;
import com.example.urlshortener.repository.CustomQrTrackingRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class QrTrackingService {

    @Autowired
    private CustomQrTrackingRepository trackingRepo;

    public void trackScan(CustomQrCode qr, HttpServletRequest request) {

        String agent = request.getHeader("User-Agent");

        CustomQrTracking t = CustomQrTracking.builder()
                .qrCode(qr)
                .scannedAt(Instant.now())
                .ipAddress(request.getRemoteAddr())
                .browser(parseBrowser(agent))
                .deviceType(parseDevice(agent))
                .country(geoLookup(request.getRemoteAddr()))
                .userAgent(agent)
                .build();

        trackingRepo.save(t);
    }

    // SIMPLE USER AGENT PARSERS
    private String parseBrowser(String agent) {
        if (agent == null) return "Unknown";
        if (agent.contains("Chrome")) return "Chrome";
        if (agent.contains("Firefox")) return "Firefox";
        if (agent.contains("Safari")) return "Safari";
        if (agent.contains("Edge")) return "Edge";
        return "Other";
    }

    private String parseDevice(String agent) {
        if (agent == null) return "Unknown";
        if (agent.contains("Mobile")) return "Mobile";
        return "Desktop";
    }

    private String geoLookup(String ip) {
        return "Unknown"; // You can integrate real API later
    }
}
