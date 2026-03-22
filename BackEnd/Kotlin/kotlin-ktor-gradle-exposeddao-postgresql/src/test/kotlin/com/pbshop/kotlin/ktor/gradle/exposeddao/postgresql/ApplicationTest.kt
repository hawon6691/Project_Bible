package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {
    @Test
    fun `health endpoint returns ok`() = testApplication {
        val response = client.get("/api/v1/health")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"status\": \"UP\""))
    }

    @Test
    fun `docs status endpoint returns planned message`() = testApplication {
        val response = client.get("/api/v1/docs-status")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("OpenAPI and Swagger are planned"))
    }
}
