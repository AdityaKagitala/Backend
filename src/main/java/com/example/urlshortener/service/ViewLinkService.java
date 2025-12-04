package com.example.urlshortener.service;

import com.example.urlshortener.model.UrlMapping;
import com.example.urlshortener.model.ViewLink;
import com.example.urlshortener.repository.UrlRepository;
import com.example.urlshortener.repository.ViewLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ViewLinkService {

    private final UrlRepository urlRepository;
    private final ViewLinkRepository viewLinkRepository;


    public void saveClickData(String shortCode, String deviceType, String referrer,String platform,String browser) {
        // ✅ Handle Optional properly
        UrlMapping mapping = urlRepository.findByShortCode(shortCode)
                .orElse(null);

        if (mapping == null) {
            return; // shortCode not found, exit gracefully
        }

        // ✅ Create a new ViewLink
        ViewLink view = new ViewLink();
        view.setShortCode(shortCode);
        view.setDeviceType(deviceType);
        view.setReferrer(referrer);
        view.setClickedAt(LocalDateTime.now());
        view.setPlatform(platform);
        view.setBrowser(browser);
        view.setUrlMapping(mapping);

        // (Optional: Use GeoIP API to set country and region)
        view.setCountry("India");
        view.setRegion("Telangana");

        // ✅ Save to database
        viewLinkRepository.save(view);
    }
}
