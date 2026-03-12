package com.pbshop.springshop.pcbuilder;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PcCompatibilityRuleRepository extends JpaRepository<PcCompatibilityRule, Long> {
    List<PcCompatibilityRule> findAllByOrderByIdAsc();
}
