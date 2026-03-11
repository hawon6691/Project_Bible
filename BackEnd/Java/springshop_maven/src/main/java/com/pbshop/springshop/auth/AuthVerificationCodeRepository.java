package com.pbshop.springshop.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthVerificationCodeRepository extends JpaRepository<AuthVerificationCode, Long> {

    Optional<AuthVerificationCode> findTopByEmailAndPurposeAndConsumedAtIsNullOrderByIdDesc(String email, String purpose);
}
