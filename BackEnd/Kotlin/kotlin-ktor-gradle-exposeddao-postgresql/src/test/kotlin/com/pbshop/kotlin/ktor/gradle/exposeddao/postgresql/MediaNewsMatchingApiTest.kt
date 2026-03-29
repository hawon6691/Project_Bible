package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql

import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MediaNewsMatchingApiTest {
    @Test
    fun media_routes_follow_the_actual_contract() = testApplication {
        installPbShopApp()

        val upload =
            client.post("/api/v1/media/upload") {
                pbHeaders(role = "USER", clientId = "media-upload")
                header("X-User-Id", "4")
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("ownerType", "PRODUCT")
                            append("ownerId", "1")
                            append(
                                "files",
                                "fake-media".encodeToByteArray(),
                                Headers.build {
                                    append(HttpHeaders.ContentDisposition, "filename=launch.jpg")
                                    append(HttpHeaders.ContentType, ContentType.Image.JPEG.toString())
                                },
                            )
                        },
                    ),
                )
            }
        val uploadedId =
            Json.parseToJsonElement(upload.bodyAsText()).jsonObject["data"]!!.jsonArray.first().jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val presigned =
            client.post("/api/v1/media/presigned-url") {
                pbHeaders(role = "USER", clientId = "media-presigned")
                header("X-User-Id", "4")
                header("Content-Type", "application/json")
                setBody("""{"fileName":"hero.mp4","fileType":"video/mp4","fileSize":2048}""")
            }
        val metadata = client.get("/api/v1/media/$uploadedId/metadata") { pbHeaders(clientId = "media-metadata") }
        val stream = client.get("/api/v1/media/stream/$uploadedId") { pbHeaders(clientId = "media-stream") }
        val forbiddenDelete =
            client.delete("/api/v1/media/$uploadedId") {
                pbHeaders(role = "USER", clientId = "media-delete-forbidden")
                header("X-User-Id", "5")
            }
        val deleteByOwner =
            client.delete("/api/v1/media/$uploadedId") {
                pbHeaders(role = "USER", clientId = "media-delete-owner")
                header("X-User-Id", "4")
            }

        assertEquals(HttpStatusCode.Created, upload.status)
        assertEquals(HttpStatusCode.Created, presigned.status)
        assertEquals(HttpStatusCode.OK, metadata.status)
        assertEquals(HttpStatusCode.PartialContent, stream.status)
        assertEquals(HttpStatusCode.Forbidden, forbiddenDelete.status)
        assertEquals(HttpStatusCode.OK, deleteByOwner.status)
        assertTrue(presigned.bodyAsText().contains("\"uploadUrl\""))
        assertTrue(metadata.bodyAsText().contains("\"mime\""))
        assertTrue(stream.bodyAsText().contains("\"streamable\": true"))
    }

    @Test
    fun news_routes_follow_the_actual_contract() = testApplication {
        installPbShopApp()

        val categories = client.get("/api/v1/news/categories") { pbHeaders(clientId = "news-categories") }
        val list = client.get("/api/v1/news?category=reviews&page=1&limit=10") { pbHeaders(clientId = "news-list") }
        val detail = client.get("/api/v1/news/1") { pbHeaders(clientId = "news-detail") }
        val createCategory =
            client.post("/api/v1/news/categories") {
                pbHeaders(role = "ADMIN", clientId = "news-category-create")
                header("Content-Type", "application/json")
                setBody("""{"name":"가이드","slug":"guides"}""")
            }
        val categoryId = Json.parseToJsonElement(createCategory.bodyAsText()).jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val createNews =
            client.post("/api/v1/news") {
                pbHeaders(role = "ADMIN", clientId = "news-create")
                header("Content-Type", "application/json")
                setBody("""{"title":"신규 소식","content":"새로운 태블릿 소식입니다.","categoryId":$categoryId,"thumbnailUrl":"/uploads/news/new.jpg","productIds":[1,3]}""")
            }
        val newsId = Json.parseToJsonElement(createNews.bodyAsText()).jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val updateNews =
            client.patch("/api/v1/news/$newsId") {
                pbHeaders(role = "ADMIN", clientId = "news-update")
                header("Content-Type", "application/json")
                setBody("""{"title":"수정된 신규 소식","productIds":[3]}""")
            }
        val deleteNews = client.delete("/api/v1/news/$newsId") { pbHeaders(role = "ADMIN", clientId = "news-delete") }
        val deleteCategory = client.delete("/api/v1/news/categories/$categoryId") { pbHeaders(role = "ADMIN", clientId = "news-category-delete") }

        assertEquals(HttpStatusCode.OK, categories.status)
        assertEquals(HttpStatusCode.OK, list.status)
        assertEquals(HttpStatusCode.OK, detail.status)
        assertEquals(HttpStatusCode.Created, createCategory.status)
        assertEquals(HttpStatusCode.Created, createNews.status)
        assertEquals(HttpStatusCode.OK, updateNews.status)
        assertEquals(HttpStatusCode.OK, deleteNews.status)
        assertEquals(HttpStatusCode.OK, deleteCategory.status)
        assertTrue(list.bodyAsText().contains("\"totalCount\""))
        assertTrue(detail.bodyAsText().contains("\"relatedProducts\""))
        assertTrue(updateNews.bodyAsText().contains("수정된 신규 소식"))
    }

    @Test
    fun matching_routes_follow_the_actual_contract() = testApplication {
        installPbShopApp()

        val pending = client.get("/api/v1/matching/pending?page=1&limit=10") { pbHeaders(role = "ADMIN", clientId = "matching-pending") }
        val approve =
            client.patch("/api/v1/matching/1/approve") {
                pbHeaders(role = "ADMIN", clientId = "matching-approve")
                header("X-User-Id", "1")
                header("Content-Type", "application/json")
                setBody("""{"productId":1}""")
            }
        val reject =
            client.patch("/api/v1/matching/1/reject") {
                pbHeaders(role = "ADMIN", clientId = "matching-reject")
                header("X-User-Id", "1")
                header("Content-Type", "application/json")
                setBody("""{"reason":"중복 매핑"}""")
            }
        val autoMatch = client.post("/api/v1/matching/auto-match") { pbHeaders(role = "ADMIN", clientId = "matching-auto-match") }
        val stats = client.get("/api/v1/matching/stats") { pbHeaders(role = "ADMIN", clientId = "matching-stats") }
        val blocked = client.get("/api/v1/matching/pending") { pbHeaders(role = "USER", clientId = "matching-blocked") }

        assertEquals(HttpStatusCode.OK, pending.status)
        assertEquals(HttpStatusCode.OK, approve.status)
        assertEquals(HttpStatusCode.BadRequest, reject.status)
        assertEquals(HttpStatusCode.OK, autoMatch.status)
        assertEquals(HttpStatusCode.OK, stats.status)
        assertEquals(HttpStatusCode.Forbidden, blocked.status)
        assertTrue(pending.bodyAsText().contains("\"sourceName\""))
        assertTrue(approve.bodyAsText().contains("\"status\": \"APPROVED\""))
        assertTrue(autoMatch.bodyAsText().contains("\"matchedCount\""))
        assertTrue(stats.bodyAsText().contains("\"total\""))
    }
}
