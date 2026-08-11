package com.lynkr.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "click_analytics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClickAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_mapping_id", nullable = false)
    private UrlMapping urlMapping;

    @Column(nullable = false)
    private LocalDateTime clickedAt;

    private String deviceType;
    private String browser;
    private String referrer;
    private String ipAddress;

    @PrePersist
    protected void onCreate() {
        if (this.clickedAt == null) {
            this.clickedAt = LocalDateTime.now();
        }
    }
}
