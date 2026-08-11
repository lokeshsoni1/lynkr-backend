package com.lynkr.backend.repository;

import com.lynkr.backend.model.ClickAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClickAnalyticsRepository extends JpaRepository<ClickAnalytics, Long> {
    List<ClickAnalytics> findByUrlMappingIdOrderByClickedAtDesc(Long urlMappingId);

    @Query("SELECT COUNT(c) FROM ClickAnalytics c WHERE c.urlMapping.user.id = :userId")
    long countTotalClicksByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(DISTINCT c.ipAddress) FROM ClickAnalytics c WHERE c.urlMapping.user.id = :userId")
    long countUniqueVisitorsByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM ClickAnalytics c WHERE c.urlMapping.user.id = :userId ORDER BY c.clickedAt DESC")
    List<ClickAnalytics> findRecentClicksByUserId(@Param("userId") Long userId);

    @Query("SELECT c.deviceType, COUNT(c) FROM ClickAnalytics c WHERE c.urlMapping.user.id = :userId GROUP BY c.deviceType")
    List<Object[]> countClicksByDeviceType(@Param("userId") Long userId);

    @Query("SELECT c.browser, COUNT(c) FROM ClickAnalytics c WHERE c.urlMapping.user.id = :userId GROUP BY c.browser")
    List<Object[]> countClicksByBrowser(@Param("userId") Long userId);

    @Query("SELECT c.referrer, COUNT(c) FROM ClickAnalytics c WHERE c.urlMapping.user.id = :userId GROUP BY c.referrer")
    List<Object[]> countClicksByReferrer(@Param("userId") Long userId);
}
