package com.pbshop.springshop.system;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long> {
    Optional<SystemSetting> findBySettingGroupAndSettingKey(String settingGroup, String settingKey);
}
