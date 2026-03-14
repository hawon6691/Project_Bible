package com.pbshop.springshop.scripts;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.pbshop.springshop.support.AuthenticatedApiIntegrationSupport;

class MigrationRoundtripScriptTest extends AuthenticatedApiIntegrationSupport {

    @Autowired
    private Flyway flyway;

    @Test
    void flywayCanCleanAndReapplyMigrations() {
        flyway.clean();
        flyway.migrate();

        assertThat(flyway.info().current()).isNotNull();
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("14");
    }
}
