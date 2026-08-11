package com.lynkr.backend.dto;

import com.lynkr.backend.model.ExpirationType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShortenRequest {

    @NotBlank(message = "Destination URL is required")
    private String originalUrl;

    private String customAlias;

    private ExpirationType expirationType = ExpirationType.NEVER;

    private LocalDateTime customExpiresAt;
}
