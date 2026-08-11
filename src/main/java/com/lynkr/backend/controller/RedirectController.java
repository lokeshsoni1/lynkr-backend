package com.lynkr.backend.controller;

import com.lynkr.backend.model.ClickAnalytics;
import com.lynkr.backend.model.UrlMapping;
import com.lynkr.backend.repository.ClickAnalyticsRepository;
import com.lynkr.backend.repository.UrlMappingRepository;
import com.lynkr.backend.util.UserAgentParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final UrlMappingRepository urlMappingRepository;
    private final ClickAnalyticsRepository clickAnalyticsRepository;

    @GetMapping("/{code}")
    public ResponseEntity<?> redirect(@PathVariable String code, HttpServletRequest request) {
        Optional<UrlMapping> optionalUrlMapping = urlMappingRepository.findByShortCodeOrCustomAlias(code);

        if (optionalUrlMapping.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Link Not Found");
        }

        UrlMapping urlMapping = optionalUrlMapping.get();

        if (urlMapping.getExpiresAt() != null && urlMapping.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.GONE).body("Link Expired");
        }

        // Increment click count
        urlMapping.setClickCount(urlMapping.getClickCount() + 1);
        urlMappingRepository.save(urlMapping);

        // Record Click Analytics
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

        // HTTP 302 Redirect
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(urlMapping.getOriginalUrl()));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
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
