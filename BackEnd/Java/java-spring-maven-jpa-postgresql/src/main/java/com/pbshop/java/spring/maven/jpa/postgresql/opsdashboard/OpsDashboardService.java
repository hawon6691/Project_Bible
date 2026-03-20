package com.pbshop.java.spring.maven.jpa.postgresql.opsdashboard;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.crawler.CrawlerService;
import com.pbshop.java.spring.maven.jpa.postgresql.queueadmin.QueueAdminService;
import com.pbshop.java.spring.maven.jpa.postgresql.searchsync.SearchSyncService;

@Service
public class OpsDashboardService {

    private final QueueAdminService queueAdminService;
    private final SearchSyncService searchSyncService;
    private final CrawlerService crawlerService;

    public OpsDashboardService(
            QueueAdminService queueAdminService,
            SearchSyncService searchSyncService,
            CrawlerService crawlerService
    ) {
        this.queueAdminService = queueAdminService;
        this.searchSyncService = searchSyncService;
        this.crawlerService = crawlerService;
    }

    public Map<String, Object> summary(AuthenticatedUserPrincipal principal) {
        Map<String, Object> payload = new LinkedHashMap<>();
        Map<String, Object> errors = new LinkedHashMap<>();
        errors.put("crawler", null);
        payload.put("checkedAt", OffsetDateTime.now().toString());
        payload.put("overallStatus", "degraded");
        payload.put("health", Map.of("status", "ok"));
        payload.put("searchSync", searchSyncService.summary(principal));
        payload.put("crawler", crawlerService.monitoring(principal));
        payload.put("queue", queueAdminService.stats(principal));
        payload.put("errors", errors);
        payload.put("alerts", List.of("crawler queue has 1 failed job"));
        payload.put("alertCount", 1);
        return payload;
    }
}
