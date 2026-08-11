package com.lynkr.backend.service;

import com.lynkr.backend.dto.ClickAnalyticsDto;
import com.lynkr.backend.dto.ShortenRequest;
import com.lynkr.backend.dto.UrlResponse;
import com.lynkr.backend.exception.BadRequestException;
import com.lynkr.backend.exception.ResourceNotFoundException;
import com.lynkr.backend.model.ClickAnalytics;
import com.lynkr.backend.model.ExpirationType;
import com.lynkr.backend.model.UrlMapping;
import com.lynkr.backend.model.User;
import com.lynkr.backend.repository.ClickAnalyticsRepository;
import com.lynkr.backend.repository.UrlMappingRepository;
import com.lynkr.backend.util.Base62Generator;
import com.lynkr.backend.util.UserAgentParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlMappingRepository urlMappingRepository;
    private final ClickAnalyticsRepository clickAnalyticsRepository;

    @Value("${lynkr.app.base-url:https://lynkr-backend-3kal.onrender.com}")
    private String baseUrl;

    @Transactional
    public UrlResponse shortenUrl(ShortenRequest request, User user) {
        String originalUrl = request.getOriginalUrl().trim();
        String lowerUrl = originalUrl.toLowerCase();
        if (!lowerUrl.startsWith("http://") && !lowerUrl.startsWith("https://")) {
            originalUrl = "https://" + originalUrl;
        }

        String shortCode;
        String customAlias = null;

        if (StringUtils.hasText(request.getCustomAlias())) {
            customAlias = request.getCustomAlias().trim();
            if (urlMappingRepository.existsByCustomAlias(customAlias) || urlMappingRepository.existsByShortCode(customAlias)) {
                throw new BadRequestException("Custom alias '" + customAlias + "' is already taken!");
            }
            shortCode = customAlias;
        } else {
            do {
                shortCode = Base62Generator.generateShortCode();
            } while (urlMappingRepository.existsByShortCode(shortCode) || urlMappingRepository.existsByCustomAlias(shortCode));
        }

        LocalDateTime expiresAt = calculateExpiration(request.getExpirationType(), request.getCustomExpiresAt());

        UrlMapping urlMapping = UrlMapping.builder()
                .user(user)
                .originalUrl(originalUrl)
                .shortCode(shortCode)
                .customAlias(customAlias)
                .expiresAt(expiresAt)
                .clickCount(0L)
                .build();

        UrlMapping savedMapping = urlMappingRepository.save(urlMapping);
        return mapToUrlResponse(savedMapping);
    }

    @Transactional
    public String handleRedirectAndRecordAnalytics(String code, HttpServletRequest request) {
        UrlMapping urlMapping = urlMappingRepository.findByShortCodeOrCustomAlias(code)
                .orElseThrow(() -> new ResourceNotFoundException("Short URL not found!"));

        if (urlMapping.isExpired()) {
            throw new BadRequestException("This link has expired!");
        }

        // Increment click count
        urlMapping.setClickCount(urlMapping.getClickCount() + 1);
        urlMappingRepository.save(urlMapping);

        // Capture Analytics
        String userAgent = request.getHeader("User-Agent");
        String referrer = request.getHeader("Referer");
        if (!StringUtils.hasText(referrer)) {
            referrer = "Direct / None";
        }
        String ipAddress = extractIpAddress(request);

        String deviceType = UserAgentParser.getDeviceType(userAgent);
        String browser = UserAgentParser.getBrowser(userAgent);

        ClickAnalytics analytics = ClickAnalytics.builder()
                .urlMapping(urlMapping)
                .clickedAt(LocalDateTime.now())
                .deviceType(deviceType)
                .browser(browser)
                .referrer(referrer)
                .ipAddress(ipAddress)
                .build();

        clickAnalyticsRepository.save(analytics);

        return urlMapping.getOriginalUrl();
    }

    @Transactional(readOnly = true)
    public List<UrlResponse> getUserUrls(User user) {
        List<UrlMapping> mappings = urlMappingRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return mappings.stream().map(this::mapToUrlResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteUrl(Long id, User user) {
        UrlMapping urlMapping = urlMappingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("URL Mapping not found!"));

        if (urlMapping.getUser() == null || !urlMapping.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You do not have permission to delete this URL!");
        }

        urlMappingRepository.delete(urlMapping);
    }

    @Transactional(readOnly = true)
    public List<ClickAnalyticsDto> getUrlAnalytics(Long id, User user) {
        UrlMapping urlMapping = urlMappingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("URL Mapping not found!"));

        if (urlMapping.getUser() == null || !urlMapping.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You do not have permission to view analytics for this URL!");
        }

        List<ClickAnalytics> analyticsList = clickAnalyticsRepository.findByUrlMappingIdOrderByClickedAtDesc(id);
        return analyticsList.stream().map(this::mapToClickAnalyticsDto).collect(Collectors.toList());
    }

    public LocalDateTime calculateExpiration(ExpirationType type, LocalDateTime customExpiresAt) {
        if (type == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        return switch (type) {
            case ONE_HOUR -> now.plusHours(1);
            case TWENTY_FOUR_HOURS -> now.plusDays(1);
            case SEVEN_DAYS -> now.plusDays(7);
            case THIRTY_DAYS -> now.plusDays(30);
            case CUSTOM -> customExpiresAt;
            case NEVER -> null;
        };
    }

    public UrlResponse mapToUrlResponse(UrlMapping mapping) {
        String activeCode = StringUtils.hasText(mapping.getCustomAlias()) ? mapping.getCustomAlias() : mapping.getShortCode();
        String effectiveBaseUrl = StringUtils.hasText(baseUrl) ? baseUrl.trim() : "https://lynkr-backend-3kal.onrender.com";
        String fullShortUrl = effectiveBaseUrl.endsWith("/") ? effectiveBaseUrl + activeCode : effectiveBaseUrl + "/" + activeCode;

        return UrlResponse.builder()
                .id(mapping.getId())
                .originalUrl(mapping.getOriginalUrl())
                .shortCode(mapping.getShortCode())
                .customAlias(mapping.getCustomAlias())
                .shortUrl(fullShortUrl)
                .createdAt(mapping.getCreatedAt())
                .expiresAt(mapping.getExpiresAt())
                .clickCount(mapping.getClickCount())
                .expired(mapping.isExpired())
                .build();
    }

    public ClickAnalyticsDto mapToClickAnalyticsDto(ClickAnalytics analytics) {
        String activeCode = StringUtils.hasText(analytics.getUrlMapping().getCustomAlias()) ? 
                analytics.getUrlMapping().getCustomAlias() : analytics.getUrlMapping().getShortCode();

        return ClickAnalyticsDto.builder()
                .id(analytics.getId())
                .clickedAt(analytics.getClickedAt())
                .deviceType(analytics.getDeviceType())
                .browser(analytics.getBrowser())
                .referrer(analytics.getReferrer())
                .ipAddress(analytics.getIpAddress())
                .shortCode(activeCode)
                .build();
    }

    private String extractIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (StringUtils.hasText(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
