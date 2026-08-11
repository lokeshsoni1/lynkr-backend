package com.lynkr.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "url_mappings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(nullable = false, length = 2048)
    private String originalUrl;

    @Column(nullable = false, unique = true)
    private String shortCode;

    @Column(unique = true)
    private String customAlias;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    @Builder.Default
    @Column(nullable = false)
    private Long clickCount = 0L;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.clickCount == null) {
            this.clickCount = 0L;
        }
    }

    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
