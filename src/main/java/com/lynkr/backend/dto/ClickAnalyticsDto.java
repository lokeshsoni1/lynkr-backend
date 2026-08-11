package com.lynkr.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClickAnalyticsDto {
    private Long id;
    private LocalDateTime clickedAt;
    private String deviceType;
    private String browser;
    private String referrer;
    private String ipAddress;
    private String shortCode;
}
