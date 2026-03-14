package com.pbshop.springshop.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PbshopPropertiesTest {

    @Autowired
    private PbshopProperties pbshopProperties;

    @Test
    void bindsCommonApplicationProperties() {
        assertThat(pbshopProperties.api().basePath()).isEqualTo("/api/v1");
        assertThat(pbshopProperties.frontendUrl()).isEqualTo("http://localhost:3000");
        assertThat(pbshopProperties.locale().defaultLocale()).isEqualTo("ko");
        assertThat(pbshopProperties.locale().supported()).contains("ko", "en", "ja");
        assertThat(pbshopProperties.docs().swaggerPath()).isEqualTo("/docs/swagger");
        assertThat(pbshopProperties.docs().openapiPath()).isEqualTo("/docs/openapi");
    }
}
