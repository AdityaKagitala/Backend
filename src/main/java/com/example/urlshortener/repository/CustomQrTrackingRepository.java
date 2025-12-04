package com.example.urlshortener.repository;

import com.example.urlshortener.model.CustomQrTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomQrTrackingRepository extends JpaRepository<CustomQrTracking, Long> {

    long countByQrCodeId(Long qrId);

    List<CustomQrTracking> findByQrCodeIdOrderByScannedAtDesc(Long qrId);

    @Query("SELECT t.deviceType, COUNT(t) FROM CustomQrTracking t WHERE t.qrCode.id = :qrId GROUP BY t.deviceType")
    List<Object[]> countByDeviceType(@Param("qrId") Long qrId);

    @Query("SELECT t.browser, COUNT(t) FROM CustomQrTracking t WHERE t.qrCode.id = :qrId GROUP BY t.browser")
    List<Object[]> countByBrowser(@Param("qrId") Long qrId);

    @Query("SELECT t.country, COUNT(t) FROM CustomQrTracking t WHERE t.qrCode.id = :qrId GROUP BY t.country")
    List<Object[]> countByCountry(@Param("qrId") Long qrId);

    @Query("SELECT DATE(t.scannedAt), COUNT(t) FROM CustomQrTracking t WHERE t.qrCode.id = :qrId GROUP BY DATE(t.scannedAt)")
    List<Object[]> countByDate(@Param("qrId") Long qrId);
}
