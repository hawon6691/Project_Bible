package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DbHealthCheckResult
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PublicApiE2ETest {
    @Test
    fun public_routes_expose_health_and_root_metadata() = testApplication {
        installPbShopApp()

        val root = client.get("/") { pbHeaders(clientId = "public-root") }
        val health = client.get("/health") { pbHeaders(clientId = "public-health") }

        assertEquals(HttpStatusCode.OK, root.status)
        assertEquals(HttpStatusCode.OK, health.status)
        assertTrue(root.bodyAsText().contains("\"routeCount\""))
    }
}

class ContractPublicApiE2ETest {
    @Test
    fun public_contract_includes_success_request_id_and_timestamp() = testApplication {
        installPbShopApp()

        val response = client.get("/api/v1/products") { pbHeaders(clientId = "contract") }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"success\": true"))
        assertTrue(response.bodyAsText().contains("\"requestId\": \"req-test\""))
        assertTrue(response.bodyAsText().contains("\"timestamp\""))
    }
}

class AuthSearchE2ETest {
    @Test
    fun auth_and_search_routes_can_run_in_the_same_flow() = testApplication {
        installPbShopApp()

        val login = client.post("/api/v1/auth/login") { pbHeaders(clientId = "auth-search-login") }
        val search = client.get("/api/v1/search") { pbHeaders(clientId = "auth-search-query") }

        assertEquals(HttpStatusCode.OK, login.status)
        assertEquals(HttpStatusCode.OK, search.status)
    }
}

class AdminAuthorizationBoundaryE2ETest {
    @Test
    fun admin_endpoints_reject_missing_or_wrong_roles() = testApplication {
        installPbShopApp()

        val unauthorized = client.get("/api/v1/admin/queues/stats") { pbHeaders(clientId = "admin-none") }
        val forbidden = client.get("/api/v1/admin/queues/stats") { pbHeaders(role = "USER", clientId = "admin-user") }

        assertEquals(HttpStatusCode.Unauthorized, unauthorized.status)
        assertEquals(HttpStatusCode.Forbidden, forbidden.status)
    }
}

class AdminPlatformE2ETest {
    @Test
    fun admin_platform_routes_work_with_admin_role() = testApplication {
        installPbShopApp()

        val response = client.get("/api/v1/admin/settings/extensions") { pbHeaders(role = "ADMIN", clientId = "admin-platform") }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"extensions\""))
    }
}

class ObservabilityE2ETest {
    @Test
    fun observability_routes_are_exposed_for_admins() = testApplication {
        installPbShopApp()

        val response = client.get("/api/v1/admin/observability/metrics") { pbHeaders(role = "ADMIN", clientId = "observability-e2e") }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"totalRequests\""))
    }
}

class OpsDashboardE2ETest {
    @Test
    fun ops_dashboard_summary_returns_operational_snapshot() = testApplication {
        installPbShopApp()

        val response = client.get("/api/v1/admin/ops-dashboard/summary") { pbHeaders(role = "ADMIN", clientId = "ops-summary") }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"overallStatus\""))
    }
}

class OpsDashboardDependencyFailuresE2ETest {
    @Test
    fun degraded_health_is_reflected_when_db_is_down() = testApplication {
        installPbShopApp(
            dbResult =
                DbHealthCheckResult.down(
                    engine = "postgresql",
                    database = "pbdb",
                    message = "Connection refused",
                ),
        )

        val response = client.get("/api/v1/health") { pbHeaders(clientId = "ops-db-down") }

        assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
        assertTrue(response.bodyAsText().contains("Connection refused"))
    }
}

class OpsDashboardResilienceE2ETest {
    @Test
    fun resilience_snapshot_endpoint_is_accessible() = testApplication {
        installPbShopApp()

        val response = client.get("/api/v1/resilience/circuit-breakers/payment-provider") { pbHeaders(role = "ADMIN", clientId = "resilience-e2e") }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"payment-provider\""))
    }
}

class OpsDashboardThresholdsE2ETest {
    @Test
    fun queue_stats_can_be_used_as_threshold_input() = testApplication {
        installPbShopApp()

        val response = client.get("/api/v1/admin/queues/stats") { pbHeaders(role = "ADMIN", clientId = "ops-thresholds") }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"failed\""))
    }
}

class QueueAdminE2ETest {
    @Test
    fun queue_admin_routes_cover_stats_and_failed_jobs() = testApplication {
        installPbShopApp()

        val stats = client.get("/api/v1/admin/queues/stats") { pbHeaders(role = "ADMIN", clientId = "queue-stats") }
        val failed = client.get("/api/v1/admin/queues/crawler/failed") { pbHeaders(role = "ADMIN", clientId = "queue-failed") }

        assertEquals(HttpStatusCode.OK, stats.status)
        assertEquals(HttpStatusCode.OK, failed.status)
    }
}

class RateLimitRegressionE2ETest {
    @Test
    fun auth_rate_limit_returns_too_many_requests_after_threshold() = testApplication {
        installPbShopApp(generalPerMinute = 5, authPerMinute = 2)

        repeat(2) { index ->
            val response = client.post("/api/v1/auth/login") { pbHeaders(clientId = "rate-limit-auth", requestId = "rate-$index") }
            assertEquals(HttpStatusCode.OK, response.status)
        }

        val limited = client.post("/api/v1/auth/login") { pbHeaders(clientId = "rate-limit-auth", requestId = "rate-final") }

        assertEquals(HttpStatusCode.TooManyRequests, limited.status)
        assertTrue(limited.bodyAsText().contains("\"COMMON_004\""))
    }
}

class ResilienceAutoTuneE2ETest {
    @Test
    fun resilience_policy_endpoint_returns_tuning_payload() = testApplication {
        installPbShopApp()

        val response = client.get("/api/v1/resilience/circuit-breakers/policies") { pbHeaders(role = "ADMIN", clientId = "resilience-policy") }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"failureThreshold\""))
    }
}

class SecurityRegressionE2ETest {
    @Test
    fun security_headers_and_request_id_are_added_to_responses() = testApplication {
        installPbShopApp()

        val response = client.get("/api/v1/products") { pbHeaders(clientId = "security", requestId = "security-req") }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("security-req", response.headers["X-Request-Id"])
        assertTrue(response.headers.contains("X-Frame-Options"))
        assertTrue(response.headers.contains("X-Content-Type-Options"))
    }
}

class SwaggerDocsE2ETest {
    @Test
    fun docs_status_openapi_and_swagger_routes_are_enabled() = testApplication {
        installPbShopApp()

        val docsStatus = client.get("/api/v1/docs-status") { pbHeaders(clientId = "docs-status") }
        val openapi = client.get("/docs/openapi") { pbHeaders(clientId = "docs-openapi") }
        val swagger = client.get("/docs/swagger") { pbHeaders(clientId = "docs-swagger") }

        assertEquals(HttpStatusCode.OK, docsStatus.status)
        assertEquals(HttpStatusCode.OK, openapi.status)
        assertEquals(HttpStatusCode.OK, swagger.status)
        assertTrue(openapi.bodyAsText().contains("\"openapi\""))
        assertTrue(swagger.bodyAsText().contains("SwaggerUIBundle"))
    }
}
