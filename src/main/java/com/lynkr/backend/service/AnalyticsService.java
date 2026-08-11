package com.lynkr.backend.service;

import com.lynkr.backend.dto.AnalyticsSummaryResponse;
import com.lynkr.backend.dto.ClickAnalyticsDto;
import com.lynkr.backend.model.ClickAnalytics;
import com.lynkr.backend.model.User;
import com.lynkr.backend.repository.ClickAnalyticsRepository;
import com.lynkr.backend.repository.UrlMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ClickAnalyticsRepository clickAnalyticsRepository;
    private final UrlMappingRepository urlMappingRepository;
    private final UrlService urlService;

    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse getAnalyticsSummary(User user) {
        Long userId = user.getId();

        long totalClicks = clickAnalyticsRepository.countTotalClicksByUserId(userId);
        long uniqueVisitors = clickAnalyticsRepository.countUniqueVisitorsByUserId(userId);
        long totalLinks = urlMappingRepository.countByUserId(userId);
        long activeLinks = urlMappingRepository.countActiveLinksByUserId(userId, LocalDateTime.now());

        Map<String, Long> deviceStats = mapListToMap(clickAnalyticsRepository.countClicksByDeviceType(userId));
        Map<String, Long> browserStats = mapListToMap(clickAnalyticsRepository.countClicksByBrowser(userId));
        Map<String, Long> referrerStats = mapListToMap(clickAnalyticsRepository.countClicksByReferrer(userId));

        List<ClickAnalytics> recentClicksList = clickAnalyticsRepository.findRecentClicksByUserId(userId);
        List<ClickAnalyticsDto> recentClicks = recentClicksList.stream()
                .limit(20)
                .map(urlService::mapToClickAnalyticsDto)
                .collect(Collectors.toList());

        return AnalyticsSummaryResponse.builder()
                .totalClicks(totalClicks)
                .uniqueVisitors(uniqueVisitors)
                .totalLinks(totalLinks)
                .activeLinks(activeLinks)
                .deviceStats(deviceStats)
                .browserStats(browserStats)
                .referrerStats(referrerStats)
                .recentClicks(recentClicks)
                .build();
    }

    private Map<String, Long> mapListToMap(List<Object[]> queryResults) {
        Map<String, Long> map = new LinkedHashMap<>();
        if (queryResults != null) {
            for (Object[] row : queryResults) {
                String key = row[0] != null ? row[0].toString() : "Unknown";
                Long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                map.put(key, count);
            }
        }
        return map;
    }
}
