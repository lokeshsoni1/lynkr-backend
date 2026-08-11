package com.lynkr.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSummaryResponse {
    private long totalClicks;
    private long uniqueVisitors;
    private long totalLinks;
    private long activeLinks;
    private Map<String, Long> deviceStats;
    private Map<String, Long> browserStats;
    private Map<String, Long> referrerStats;
    private List<ClickAnalyticsDto> recentClicks;
}
