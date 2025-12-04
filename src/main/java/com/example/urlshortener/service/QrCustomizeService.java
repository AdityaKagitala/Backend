package com.example.urlshortener.service;

import com.example.urlshortener.dto.QrCustomizeRequest;
import com.example.urlshortener.dto.UrlPreviewForQr;
import com.example.urlshortener.model.CustomQrCode;
import com.example.urlshortener.model.User;
import com.example.urlshortener.repository.CustomQrCodeRepository;
import com.example.urlshortener.repository.UserRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QrCustomizeService {

    private final CustomQrCodeRepository repo;
    private final UserRepository userRepo;

    // ------------------- SAVE QR & PREVIEW -------------------
    public CustomQrCode saveCustomQr(QrCustomizeRequest req, String username) {

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String url = req.getUrl();

        UrlPreviewForQr preview = fetchPreviewMetadata(url);

        byte[] png = generateCustomQr(
                req.getUrl(),
                req.getColor(),
                req.getBgColor(),
                req.getSize(),
                req.getLogoUrl()
        );

        String base64 = Base64.getEncoder().encodeToString(png);

        CustomQrCode qr = CustomQrCode.builder()
                .url(url)
                .color(req.getColor())
                .bgColor(req.getBgColor())
                .size(req.getSize())
                .logoUrl(req.getLogoUrl())
                .qrBase64(base64)
                .createdAt(Instant.now())
                .user(user)

                // ⭐ saved preview fields
                .title(preview.getTitle())
                .description(preview.getDescription())
                .imageUrl(preview.getImage())
                .faviconUrl(preview.getFavicon())

                .build();

        return repo.save(qr);
    }

    // ------------------- GENERATE QR IMAGE -------------------
    public byte[] generateCustomQr(String url, String colorHex, String bgHex, int size, String logoUrl) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, size, size);

            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);

            int qrColor = Color.decode(colorHex).getRGB();
            int bgColor = Color.decode(bgHex).getRGB();

            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    img.setRGB(x, y, matrix.get(x, y) ? qrColor : bgColor);
                }
            }

            // Add logo
            if (logoUrl != null && !logoUrl.isBlank()) {
                BufferedImage logo = ImageIO.read(new URL(logoUrl));
                int logoSize = size / 5;
                int pos = (size - logoSize) / 2;

                Graphics2D g = img.createGraphics();
                g.drawImage(logo, pos, pos, logoSize, logoSize, null);
                g.dispose();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("QR generation failed: " + e.getMessage());
        }
    }

    // ------------------- FETCH SAVED QR BY ID -------------------
    public Optional<CustomQrCode> getQrById(Long id) {
        return repo.findById(id);
    }

    // ------------------- QR HISTORY -------------------
    public List<CustomQrCode> getQrHistory(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return repo.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    // ------------------- URL PREVIEW (for saving) -------------------
    public UrlPreviewForQr fetchPreviewMetadata(String url) {
        try {
            Document doc = Jsoup.connect(url).timeout(5000).get();

            String title = doc.select("meta[property=og:title]").attr("content");
            if (title.isBlank()) title = doc.title();

            String desc = doc.select("meta[property=og:description]").attr("content");
            if (desc.isBlank())
                desc = doc.select("meta[name=description]").attr("content");

            String image = doc.select("meta[property=og:image]").attr("content");

            String favicon = extractDomain(url) + "/favicon.ico";

            return new UrlPreviewForQr(
                    title,
                    desc,
                    image,
                    favicon
            );

        } catch (Exception e) {
            return new UrlPreviewForQr(
                    "Preview Not Available",
                    "No description",
                    null,
                    "https://www.google.com/s2/favicons?domain=" + url
            );
        }
    }

    // ------------------- DOMAIN -------------------
    private String extractDomain(String url) {
        try {
            if (!(url.startsWith("http://") || url.startsWith("https://"))) {
                url = "https://" + url;
            }
            URI uri = new URI(url);
            return uri.getScheme() + "://" + uri.getHost();
        } catch (Exception e) {
            return "https://www.google.com";
        }
    }
}