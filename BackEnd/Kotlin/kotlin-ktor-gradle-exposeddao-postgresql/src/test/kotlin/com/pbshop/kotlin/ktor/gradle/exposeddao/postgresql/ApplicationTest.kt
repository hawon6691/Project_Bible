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
        assertTrue(response.bodyAsText().contains("\"baselineTrack\": \"kotlin-ktor-gradle-exposeddao-postgresql\""))
    }

    @Test
    fun `docs status endpoint returns planned message`() = testApplication {
        val response = client.get("/api/v1/docs-status")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"docsPath\": \"/docs\""))
        assertTrue(response.bodyAsText().contains("aligned /docs path"))
    }

    @Test
    fun `root endpoint exposes baseline metadata`() = testApplication {
        val response = client.get("/")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"apiPrefix\": \"/api/v1\""))
        assertTrue(response.bodyAsText().contains("\"baselineTrack\": \"kotlin-ktor-gradle-exposeddao-postgresql\""))
    }
}
