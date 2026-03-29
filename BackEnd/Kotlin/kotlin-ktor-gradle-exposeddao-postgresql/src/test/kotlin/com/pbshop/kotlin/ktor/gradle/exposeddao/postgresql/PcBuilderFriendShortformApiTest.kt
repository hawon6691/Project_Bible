package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql

import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
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

class PcBuilderApiTest {
    @Test
    fun pcbuilder_routes_follow_the_actual_contract() = testApplication {
        installPbShopApp()

        val myBuilds = client.get("/api/v1/pc-builds?page=1&limit=10") { pbHeaders(role = "USER", clientId = "pc-builds-list") }
        val create =
            client.post("/api/v1/pc-builds") {
                pbHeaders(role = "USER", clientId = "pc-builds-create")
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("""{"name":"새 견적","description":"스트리밍 데스크탑","purpose":"STREAMING","budget":2500000}""")
            }
        val buildId = Json.parseToJsonElement(create.bodyAsText()).jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val addPart =
            client.post("/api/v1/pc-builds/$buildId/parts") {
                pbHeaders(role = "USER", clientId = "pc-builds-add-part")
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("""{"productId":1,"partType":"CPU","quantity":1}""")
            }
        val compatibility = client.get("/api/v1/pc-builds/$buildId/compatibility") { pbHeaders(clientId = "pc-builds-compatibility") }
        val share = client.get("/api/v1/pc-builds/$buildId/share") { pbHeaders(role = "USER", clientId = "pc-builds-share") }
        val shareCode = Json.parseToJsonElement(share.bodyAsText()).jsonObject["data"]!!.jsonObject["shareCode"]!!.jsonPrimitive.content
        val shared = client.get("/api/v1/pc-builds/shared/$shareCode") { pbHeaders(clientId = "pc-builds-shared") }
        val popular = client.get("/api/v1/pc-builds/popular?page=1&limit=10") { pbHeaders(clientId = "pc-builds-popular") }
        val forbiddenUpdate =
            client.patch("/api/v1/pc-builds/2") {
                pbHeaders(role = "USER", clientId = "pc-builds-forbidden-update")
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("""{"name":"should-fail"}""")
            }
        val createRule =
            client.post("/api/v1/admin/compatibility-rules") {
                pbHeaders(role = "ADMIN", clientId = "pc-rules-create")
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("""{"partType":"GPU","targetPartType":"PSU","title":"전원체크","description":"PSU가 필요합니다.","severity":"MEDIUM","enabled":true}""")
            }
        val ruleId = Json.parseToJsonElement(createRule.bodyAsText()).jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val rules = client.get("/api/v1/admin/compatibility-rules") { pbHeaders(role = "ADMIN", clientId = "pc-rules-list") }
        val deleteRule = client.delete("/api/v1/admin/compatibility-rules/$ruleId") { pbHeaders(role = "ADMIN", clientId = "pc-rules-delete") }
        val removePart = client.delete("/api/v1/pc-builds/$buildId/parts/1") { pbHeaders(role = "USER", clientId = "pc-builds-remove-part") }
        val deleteBuild = client.delete("/api/v1/pc-builds/$buildId") { pbHeaders(role = "USER", clientId = "pc-builds-delete") }

        assertEquals(HttpStatusCode.OK, myBuilds.status)
        assertEquals(HttpStatusCode.Created, create.status)
        assertEquals(HttpStatusCode.OK, addPart.status)
        assertEquals(HttpStatusCode.OK, compatibility.status)
        assertEquals(HttpStatusCode.OK, share.status)
        assertEquals(HttpStatusCode.OK, shared.status)
        assertEquals(HttpStatusCode.OK, popular.status)
        assertEquals(HttpStatusCode.Forbidden, forbiddenUpdate.status)
        assertEquals(HttpStatusCode.Created, createRule.status)
        assertEquals(HttpStatusCode.OK, rules.status)
        assertEquals(HttpStatusCode.OK, deleteRule.status)
        assertEquals(HttpStatusCode.NotFound, removePart.status)
        assertEquals(HttpStatusCode.OK, deleteBuild.status)

        assertTrue(addPart.bodyAsText().contains("\"partType\": \"CPU\""))
        assertTrue(compatibility.bodyAsText().contains("\"status\""))
        assertTrue(shared.bodyAsText().contains("\"shareCode\": \"$shareCode\""))
        assertTrue(rules.bodyAsText().contains("\"title\""))
    }
}

class FriendApiTest {
    @Test
    fun friend_routes_follow_the_actual_contract() = testApplication {
        installPbShopApp()

        val sendRequest = client.post("/api/v1/friends/request/1") { pbHeaders(role = "USER", clientId = "friend-request") }
        val sent = client.get("/api/v1/friends/requests/sent?page=1&limit=10") { pbHeaders(role = "USER", clientId = "friend-sent") }
        val received = client.get("/api/v1/friends/requests/received?page=1&limit=10") { pbHeaders(role = "USER", clientId = "friend-received") }
        val accept = client.patch("/api/v1/friends/request/2/accept") { pbHeaders(role = "USER", clientId = "friend-accept") }
        val list = client.get("/api/v1/friends?page=1&limit=10") { pbHeaders(role = "USER", clientId = "friend-list") }
        val feed = client.get("/api/v1/friends/feed?page=1&limit=10") { pbHeaders(role = "USER", clientId = "friend-feed") }
        val block = client.post("/api/v1/friends/block/5") { pbHeaders(role = "USER", clientId = "friend-block") }
        val blockedRequest = client.post("/api/v1/friends/request/5") { pbHeaders(role = "USER", clientId = "friend-blocked-request") }
        val unblock = client.delete("/api/v1/friends/block/5") { pbHeaders(role = "USER", clientId = "friend-unblock") }
        val deleteFriend = client.delete("/api/v1/friends/6") { pbHeaders(role = "USER", clientId = "friend-delete") }
        val selfRequest = client.post("/api/v1/friends/request/4") { pbHeaders(role = "USER", clientId = "friend-self") }

        assertEquals(HttpStatusCode.OK, sendRequest.status)
        assertEquals(HttpStatusCode.OK, sent.status)
        assertEquals(HttpStatusCode.OK, received.status)
        assertEquals(HttpStatusCode.OK, accept.status)
        assertEquals(HttpStatusCode.OK, list.status)
        assertEquals(HttpStatusCode.OK, feed.status)
        assertEquals(HttpStatusCode.OK, block.status)
        assertEquals(HttpStatusCode.BadRequest, blockedRequest.status)
        assertEquals(HttpStatusCode.OK, unblock.status)
        assertEquals(HttpStatusCode.OK, deleteFriend.status)
        assertEquals(HttpStatusCode.BadRequest, selfRequest.status)

        assertTrue(list.bodyAsText().contains("\"friend\""))
        assertTrue(feed.bodyAsText().contains("\"metadata\""))
    }
}

class ShortformApiTest {
    @Test
    fun shortform_routes_follow_the_actual_contract() = testApplication {
        installPbShopApp()

        val upload =
            client.post("/api/v1/shortforms") {
                pbHeaders(role = "USER", clientId = "shortform-upload")
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("title", "새 숏폼")
                            append("productIds[]", "1")
                            append(
                                "video",
                                "video-binary".encodeToByteArray(),
                                Headers.build {
                                    append(HttpHeaders.ContentDisposition, "filename=shortform.mp4")
                                    append(HttpHeaders.ContentType, ContentType.Video.MP4.toString())
                                },
                            )
                        },
                    ),
                )
            }
        val shortformId = Json.parseToJsonElement(upload.bodyAsText()).jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val feed = client.get("/api/v1/shortforms?limit=10") { pbHeaders(clientId = "shortform-feed") }
        val detail = client.get("/api/v1/shortforms/$shortformId") { pbHeaders(clientId = "shortform-detail") }
        val like = client.post("/api/v1/shortforms/$shortformId/like") { pbHeaders(role = "USER", clientId = "shortform-like") }
        val comment =
            client.post("/api/v1/shortforms/$shortformId/comments") {
                pbHeaders(role = "USER", clientId = "shortform-comment")
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("""{"content":"좋은 영상이에요"}""")
            }
        val comments = client.get("/api/v1/shortforms/$shortformId/comments?page=1&limit=10") { pbHeaders(clientId = "shortform-comments") }
        val ranking = client.get("/api/v1/shortforms/ranking/list?period=week&limit=10") { pbHeaders(clientId = "shortform-ranking") }
        val status = client.get("/api/v1/shortforms/$shortformId/transcode-status") { pbHeaders(clientId = "shortform-status") }
        val retry = client.post("/api/v1/shortforms/$shortformId/transcode/retry") { pbHeaders(role = "USER", clientId = "shortform-retry") }
        val byUser = client.get("/api/v1/shortforms/user/4?page=1&limit=10") { pbHeaders(clientId = "shortform-user") }
        val delete = client.delete("/api/v1/shortforms/$shortformId") { pbHeaders(role = "USER", clientId = "shortform-delete") }

        assertEquals(HttpStatusCode.Created, upload.status)
        assertEquals(HttpStatusCode.OK, feed.status)
        assertEquals(HttpStatusCode.OK, detail.status)
        assertEquals(HttpStatusCode.OK, like.status)
        assertEquals(HttpStatusCode.Created, comment.status)
        assertEquals(HttpStatusCode.OK, comments.status)
        assertEquals(HttpStatusCode.OK, ranking.status)
        assertEquals(HttpStatusCode.OK, status.status)
        assertEquals(HttpStatusCode.OK, retry.status)
        assertEquals(HttpStatusCode.OK, byUser.status)
        assertEquals(HttpStatusCode.OK, delete.status)

        val detailData = Json.parseToJsonElement(detail.bodyAsText()).jsonObject["data"]!!.jsonObject
        assertTrue(detailData["viewCount"]!!.jsonPrimitive.content.toInt() > 0)
        assertTrue(upload.bodyAsText().contains("\"transcodeStatus\": \"PENDING\""))
        assertTrue(like.bodyAsText().contains("\"liked\""))
        assertTrue(comment.bodyAsText().contains("좋은 영상이에요"))
    }
}
