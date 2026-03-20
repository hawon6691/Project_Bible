package com.pbshop.java.spring.maven.jpa.postgresql.auth;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthRefreshTokenRepository extends JpaRepository<AuthRefreshToken, Long> {

    Optional<AuthRefreshToken> findByTokenAndRevokedAtIsNullAndExpiresAtAfter(String token, OffsetDateTime now);

    List<AuthRefreshToken> findByUserIdAndRevokedAtIsNull(Long userId);
}
