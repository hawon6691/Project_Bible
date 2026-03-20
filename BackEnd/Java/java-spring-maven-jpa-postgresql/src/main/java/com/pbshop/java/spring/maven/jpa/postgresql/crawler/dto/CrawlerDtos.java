package com.pbshop.java.spring.maven.jpa.postgresql.crawler.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;

public final class CrawlerDtos {
    private CrawlerDtos() {
    }

    public record CreateCrawlerJobRequest(
            @NotBlank String name,
            @NotBlank String jobType,
            String status,
            Map<String, Object> payload
    ) { }

    public record UpdateCrawlerJobRequest(
            String name,
            String jobType,
            String status,
            Map<String, Object> payload
    ) { }

    public record TriggerCrawlerRequest(
            Long jobId,
            String targetType
    ) { }
}
