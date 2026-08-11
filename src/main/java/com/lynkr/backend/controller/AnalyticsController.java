package com.lynkr.backend.controller;

import com.lynkr.backend.dto.AnalyticsSummaryResponse;
import com.lynkr.backend.dto.ApiResponse;
import com.lynkr.backend.exception.BadRequestException;
import com.lynkr.backend.model.User;
import com.lynkr.backend.security.SecurityUtils;
import com.lynkr.backend.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final SecurityUtils securityUtils;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<AnalyticsSummaryResponse>> getAnalyticsSummary() {
        User user = securityUtils.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("Authentication required"));
        AnalyticsSummaryResponse summary = analyticsService.getAnalyticsSummary(user);
        return ResponseEntity.ok(ApiResponse.success("Analytics summary retrieved successfully", summary));
    }
}
