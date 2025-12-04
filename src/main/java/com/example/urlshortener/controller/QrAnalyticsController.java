package com.example.urlshortener.controller;

import com.example.urlshortener.dto.UrlPreviewForQr;
import com.example.urlshortener.model.CustomQrCode;
import com.example.urlshortener.repository.CustomQrTrackingRepository;
import com.example.urlshortener.service.QrCustomizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/custom-qr/analytics")
public class QrAnalyticsController {

    @Autowired
    private CustomQrTrackingRepository repo;
    @Autowired
    private  QrCustomizeService qrService;

    @GetMapping("/{qrId}")
    public ResponseEntity<?> getAnalytics(@PathVariable Long qrId) {

        Map<String, Object> data = new HashMap<>();

        CustomQrCode qr = qrService.getQrById(qrId).orElse(null);
        if (qr == null) return ResponseEntity.notFound().build();

        UrlPreviewForQr dto = new UrlPreviewForQr(
                qr.getTitle(),
                qr.getDescription(),
                qr.getImageUrl(),
                qr.getFaviconUrl()
        );


        data.put("totalScans", repo.countByQrCodeId(qrId));

        data.put("devices", repo.countByDeviceType(qrId));
        data.put("browsers", repo.countByBrowser(qrId));
        data.put("countries", repo.countByCountry(qrId));
        data.put("timeline", repo.countByDate(qrId));
        data.put("list", repo.findByQrCodeIdOrderByScannedAtDesc(qrId));
        data.put("preview",dto);

        return ResponseEntity.ok(data);
    }
}
