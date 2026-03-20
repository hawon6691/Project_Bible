package com.pbshop.springshop.auth;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthAccessTokenRepository extends JpaRepository<AuthAccessToken, Long> {

    Optional<AuthAccessToken> findByTokenAndRevokedAtIsNullAndExpiresAtAfter(String token, OffsetDateTime now);
}
