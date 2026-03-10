package com.pbshop.springshop.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PbshopProperties.class)
public class PbshopConfiguration {
}
