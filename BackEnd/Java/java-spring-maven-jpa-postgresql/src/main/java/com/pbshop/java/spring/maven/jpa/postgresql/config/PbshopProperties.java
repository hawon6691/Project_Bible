package com.pbshop.java.spring.maven.jpa.postgresql.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pbshop")
public record PbshopProperties(
        Api api,
        Locale locale,
        Docs docs,
        String frontendUrl
) {

    public record Api(String basePath) {}

    public record Locale(String defaultLocale, List<String> supported) {}

    public record Docs(String swaggerPath, String openapiPath) {}
}
