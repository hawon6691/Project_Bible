package com.pbshop.java.spring.maven.jpa.postgresql.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PbshopProperties.class)
public class PbshopConfiguration {
}
