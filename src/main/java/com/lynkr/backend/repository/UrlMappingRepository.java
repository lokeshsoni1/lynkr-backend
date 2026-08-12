package com.lynkr.backend.repository;

import com.lynkr.backend.model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {
    Optional<UrlMapping> findByShortCode(String shortCode);
    Optional<UrlMapping> findByCustomAlias(String customAlias);
    
    @Query("SELECT u FROM UrlMapping u WHERE u.shortCode = :code OR u.customAlias = :code")
    Optional<UrlMapping> findByShortCodeOrCustomAlias(@Param("code") String code);

    boolean existsByShortCode(String shortCode);
    boolean existsByCustomAlias(String customAlias);

    List<UrlMapping> findByUserIdOrderByCreatedAtDesc(Long userId);
    long countByUserId(Long userId);
    long countByUser(User user);

    @Query("SELECT COUNT(u) FROM UrlMapping u WHERE u.user.id = :userId AND (u.expiresAt IS NULL OR u.expiresAt > :now)")
    long countActiveLinksByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
