package com.pbshop.java.spring.maven.jpa.postgresql.e2e;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pbshop.java.spring.maven.jpa.postgresql.searchsync.SearchIndexOutbox;
import com.pbshop.java.spring.maven.jpa.postgresql.searchsync.SearchIndexOutboxRepository;
import com.pbshop.java.spring.maven.jpa.postgresql.support.AuthenticatedApiIntegrationSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class OpsDashboardDependencyFailuresE2ETest extends AuthenticatedApiIntegrationSupport {

    @Autowired
    private SearchIndexOutboxRepository searchIndexOutboxRepository;

    @Test
    void opsDashboardShowsDependencyFailureSignals() throws Exception {
        String adminToken = createUserAndLogin("ADMIN");

        SearchIndexOutbox outbox = new SearchIndexOutbox();
        outbox.setEntityType("PRODUCT");
        outbox.setEntityId(1L);
        outbox.setStatus("FAILED");
        searchIndexOutboxRepository.save(outbox);

        mockMvc.perform(get("/api/v1/admin/ops-dashboard/summary")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overallStatus").value("degraded"))
                .andExpect(jsonPath("$.data.queue.items[2].counts.failed").value(1));
    }
}
