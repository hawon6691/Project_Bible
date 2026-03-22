package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DbHealthCheckResult
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DbHealthService
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {
    @Test
    fun `health endpoint returns ok`() = testApplication {
        environment {
            config = createTestConfig()
        }

        application {
            module(
                dbHealthService = FakeDbHealthService(
                    DbHealthCheckResult.up(
                        engine = "postgresql",
                        database = "pbdb",
                    ),
                ),
            )
        }

        val response = client.get("/api/v1/health")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"status\": \"UP\""))
        assertTrue(response.bodyAsText().contains("\"baselineTrack\": \"kotlin-ktor-gradle-exposeddao-postgresql\""))
        assertTrue(response.bodyAsText().contains("\"checks\""))
        assertTrue(response.bodyAsText().contains("\"db\""))
        assertTrue(response.bodyAsText().contains("\"engine\": \"postgresql\""))
        assertTrue(response.bodyAsText().contains("\"database\": \"pbdb\""))
    }

    @Test
    fun `health endpoint returns service unavailable when db is down`() = testApplication {
        environment {
            config = createTestConfig()
        }

        application {
            module(
                dbHealthService = FakeDbHealthService(
                    DbHealthCheckResult.down(
                        engine = "postgresql",
                        database = "pbdb",
                        message = "Connection refused",
                    ),
                ),
            )
        }

        val response = client.get("/health")

        assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
        assertTrue(response.bodyAsText().contains("\"status\": \"DOWN\""))
        assertTrue(response.bodyAsText().contains("\"message\": \"Connection refused\""))
    }

    @Test
    fun `docs status endpoint returns planned message`() = testApplication {
        environment {
            config = createTestConfig()
        }

        application {
            module(
                dbHealthService = FakeDbHealthService(
                    DbHealthCheckResult.up(
                        engine = "postgresql",
                        database = "pbdb",
                    ),
                ),
            )
        }

        val response = client.get("/api/v1/docs-status")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"docsPath\": \"/docs\""))
        assertTrue(response.bodyAsText().contains("aligned /docs path"))
    }

    @Test
    fun `root endpoint exposes baseline metadata`() = testApplication {
        environment {
            config = createTestConfig()
        }

        application {
            module(
                dbHealthService = FakeDbHealthService(
                    DbHealthCheckResult.up(
                        engine = "postgresql",
                        database = "pbdb",
                    ),
                ),
            )
        }

        val response = client.get("/")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"apiPrefix\": \"/api/v1\""))
        assertTrue(response.bodyAsText().contains("\"baselineTrack\": \"kotlin-ktor-gradle-exposeddao-postgresql\""))
    }
}

private class FakeDbHealthService(
    private val result: DbHealthCheckResult,
) : DbHealthService {
    override fun check(): DbHealthCheckResult = result
}

private fun createTestConfig(): MapApplicationConfig =
    MapApplicationConfig(
        "pbshop.apiPrefix" to "/api/v1",
        "pbshop.appName" to "pbshop-kotlin-ktor-gradle-exposeddao-postgresql",
        "pbshop.docsPath" to "/docs",
        "pbshop.baselineTrack" to "kotlin-ktor-gradle-exposeddao-postgresql",
        "pbshop.database.url" to "jdbc:postgresql://127.0.0.1:5432/pbdb",
        "pbshop.database.username" to "project_bible",
        "pbshop.database.password" to "project_bible",
        "pbshop.database.driver" to "org.postgresql.Driver",
        "pbshop.database.engine" to "postgresql",
        "pbshop.database.database" to "pbdb",
        "pbshop.database.maxPoolSize" to "5",
        "pbshop.database.minIdle" to "1",
    )
