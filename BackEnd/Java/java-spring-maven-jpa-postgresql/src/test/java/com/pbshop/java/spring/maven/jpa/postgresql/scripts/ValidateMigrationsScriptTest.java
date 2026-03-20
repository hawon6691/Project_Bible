package com.pbshop.java.spring.maven.jpa.postgresql.scripts;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.pbshop.java.spring.maven.jpa.postgresql.support.AuthenticatedApiIntegrationSupport;

class ValidateMigrationsScriptTest extends AuthenticatedApiIntegrationSupport {

    @Autowired
    private Flyway flyway;

    @Test
    void migrationMetadataIsPresent() {
        assertThat(flyway.info().all()).hasSize(14);
        assertThat(flyway.info().current()).isNotNull();
    }
}
