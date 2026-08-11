package com.lynkr.backend.controller;

import com.lynkr.backend.dto.ApiResponse;
import com.lynkr.backend.dto.ClickAnalyticsDto;
import com.lynkr.backend.dto.ShortenRequest;
import com.lynkr.backend.dto.UrlResponse;
import com.lynkr.backend.exception.BadRequestException;
import com.lynkr.backend.model.User;
import com.lynkr.backend.security.SecurityUtils;
import com.lynkr.backend.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/urls")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;
    private final SecurityUtils securityUtils;

    @PostMapping("/shorten")
    public ResponseEntity<ApiResponse<UrlResponse>> shortenUrl(@Valid @RequestBody ShortenRequest request) {
        User user = securityUtils.getCurrentUser().orElse(null);
        UrlResponse response = urlService.shortenUrl(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("URL shortened successfully", response));
    }

    @GetMapping("/my-links")
    public ResponseEntity<ApiResponse<List<UrlResponse>>> getMyLinks() {
        User user = securityUtils.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("Authentication required"));
        List<UrlResponse> responses = urlService.getUserUrls(user);
        return ResponseEntity.ok(ApiResponse.success("Fetched user links successfully", responses));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUrl(@PathVariable Long id) {
        User user = securityUtils.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("Authentication required"));
        urlService.deleteUrl(id, user);
        return ResponseEntity.ok(ApiResponse.success("Link deleted successfully", null));
    }

    @GetMapping("/{id}/analytics")
    public ResponseEntity<ApiResponse<List<ClickAnalyticsDto>>> getUrlAnalytics(@PathVariable Long id) {
        User user = securityUtils.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("Authentication required"));
        List<ClickAnalyticsDto> analytics = urlService.getUrlAnalytics(id, user);
        return ResponseEntity.ok(ApiResponse.success("Fetched link analytics successfully", analytics));
    }
}
