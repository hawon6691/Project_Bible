package com.pbshop.springshop.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthPasswordResetRequestRepository extends JpaRepository<AuthPasswordResetRequest, Long> {

    Optional<AuthPasswordResetRequest> findTopByEmailAndConsumedAtIsNullOrderByIdDesc(String email);

    Optional<AuthPasswordResetRequest> findByResetTokenAndConsumedAtIsNull(String resetToken);
}
