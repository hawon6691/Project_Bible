package com.pbshop.springshop.push;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PushPreferenceRepository extends JpaRepository<PushPreference, Long> {

    Optional<PushPreference> findByUserId(Long userId);
}
