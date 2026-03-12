package com.pbshop.springshop.pcbuilder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pc_compatibility_rules")
public class PcCompatibilityRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(name = "source_part_type", nullable = false)
    private String sourcePartType;
    @Column(name = "target_part_type", nullable = false)
    private String targetPartType;
    @Column(name = "rule_type", nullable = false)
    private String ruleType;
    @Column(name = "rule_value_json", columnDefinition = "CLOB")
    private String ruleValueJson;
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSourcePartType() { return sourcePartType; }
    public void setSourcePartType(String sourcePartType) { this.sourcePartType = sourcePartType; }
    public String getTargetPartType() { return targetPartType; }
    public void setTargetPartType(String targetPartType) { this.targetPartType = targetPartType; }
    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }
    public String getRuleValueJson() { return ruleValueJson; }
    public void setRuleValueJson(String ruleValueJson) { this.ruleValueJson = ruleValueJson; }
}
