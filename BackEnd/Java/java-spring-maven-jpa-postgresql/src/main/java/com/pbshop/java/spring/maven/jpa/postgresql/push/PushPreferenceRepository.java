package com.pbshop.java.spring.maven.jpa.postgresql.push;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PushPreferenceRepository extends JpaRepository<PushPreference, Long> {

    Optional<PushPreference> findByUserId(Long userId);
}
