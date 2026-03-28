package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth.AuthTokenCodec
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.server.testing.testApplication
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchSyncApiTest {
    @Test
    fun search_outbox_summary_and_requeue_routes_follow_the_contract() = testApplication {
        installPbShopApp()

        val initialSummary = client.get("/api/v1/search/admin/index/outbox/summary") { pbHeaders(role = "ADMIN", clientId = "search-outbox-summary") }
        val requeue = client.post("/api/v1/search/admin/index/outbox/requeue-failed?limit=1") { pbHeaders(role = "ADMIN", clientId = "search-outbox-requeue") }
        val afterRequeue = client.get("/api/v1/search/admin/index/outbox/summary") { pbHeaders(role = "ADMIN", clientId = "search-outbox-summary-2") }
        val reindexAll = client.post("/api/v1/search/admin/index/reindex") { pbHeaders(role = "ADMIN", clientId = "search-reindex-all-outbox") }
        val reindexProduct = client.post("/api/v1/search/admin/index/products/1/reindex") { pbHeaders(role = "ADMIN", clientId = "search-reindex-product-outbox") }
        val finalSummary = client.get("/api/v1/search/admin/index/outbox/summary") { pbHeaders(role = "ADMIN", clientId = "search-outbox-summary-3") }

        assertEquals(HttpStatusCode.OK, initialSummary.status)
        assertEquals(HttpStatusCode.OK, requeue.status)
        assertEquals(HttpStatusCode.OK, afterRequeue.status)
        assertEquals(HttpStatusCode.Created, reindexAll.status)
        assertEquals(HttpStatusCode.Created, reindexProduct.status)
        assertEquals(HttpStatusCode.OK, finalSummary.status)

        val initialData = Json.parseToJsonElement(initialSummary.bodyAsText()).jsonObject["data"]!!.jsonObject
        val requeueData = Json.parseToJsonElement(requeue.bodyAsText()).jsonObject["data"]!!.jsonObject
        val afterRequeueData = Json.parseToJsonElement(afterRequeue.bodyAsText()).jsonObject["data"]!!.jsonObject
        val finalData = Json.parseToJsonElement(finalSummary.bodyAsText()).jsonObject["data"]!!.jsonObject

        assertEquals("1", initialData["failed"]!!.jsonPrimitive.content)
        assertEquals("1", requeueData["requeuedCount"]!!.jsonPrimitive.content)
        assertEquals("0", afterRequeueData["failed"]!!.jsonPrimitive.content)
        assertEquals("1", afterRequeueData["pending"]!!.jsonPrimitive.content)
        assertEquals("4", finalData["total"]!!.jsonPrimitive.content)
        assertEquals("3", finalData["pending"]!!.jsonPrimitive.content)
    }

    @Test
    fun search_outbox_admin_routes_keep_auth_boundaries() = testApplication {
        installPbShopApp()

        val unauthorized = client.get("/api/v1/search/admin/index/outbox/summary") { pbHeaders(clientId = "search-outbox-unauthorized") }
        val forbidden = client.get("/api/v1/search/admin/index/outbox/summary") { pbHeaders(role = "USER", clientId = "search-outbox-forbidden") }

        assertEquals(HttpStatusCode.Unauthorized, unauthorized.status)
        assertEquals(HttpStatusCode.Forbidden, forbidden.status)
    }
}

class ChatSocketApiTest {
    @Test
    fun authenticated_member_can_join_room_and_receive_new_message_event() = testApplication {
        installPbShopApp()

        val wsClient = createClient { install(WebSockets) }
        val token = AuthTokenCodec.createAccessToken(4, "user1@nestshop.com", PbRole.USER, Instant.now().plusSeconds(3600))
        val session =
            wsClient.webSocketSession {
                url {
                    protocol = URLProtocol.WS
                    host = "localhost"
                    port = 80
                    encodedPathSegments = listOf("api", "v1", "chat", "ws")
                }
                header(HttpHeaders.Authorization, "Bearer $token")
            }

        session.outgoing.send(Frame.Text("""{"event":"joinRoom","data":{"roomId":1}}"""))
        val joined = (session.incoming.receive() as Frame.Text).readText()
        session.outgoing.send(Frame.Text("""{"event":"sendMessage","data":{"roomId":1,"content":"웹소켓 메시지 테스트"}}"""))
        val newMessage = (session.incoming.receive() as Frame.Text).readText()

        assertTrue(joined.contains("\"event\":\"joinedRoom\""))
        assertTrue(newMessage.contains("\"event\":\"newMessage\""))
        assertTrue(newMessage.contains("웹소켓 메시지 테스트"))

        session.outgoing.send(Frame.Close())
    }

    @Test
    fun websocket_rejects_unauthenticated_and_non_member_requests() = testApplication {
        installPbShopApp()

        val wsClient = createClient { install(WebSockets) }

        val unauthenticatedSession =
            wsClient.webSocketSession {
                url {
                    protocol = URLProtocol.WS
                    host = "localhost"
                    port = 80
                    encodedPathSegments = listOf("api", "v1", "chat", "ws")
                }
            }
        val unauthenticatedMessage = (unauthenticatedSession.incoming.receive() as Frame.Text).readText()
        assertTrue(unauthenticatedMessage.contains("\"event\":\"error\""))
        unauthenticatedSession.outgoing.send(Frame.Close())

        val nonMemberToken = AuthTokenCodec.createAccessToken(99, "outsider@nestshop.com", PbRole.USER, Instant.now().plusSeconds(3600))
        val nonMemberSession =
            wsClient.webSocketSession {
                url {
                    protocol = URLProtocol.WS
                    host = "localhost"
                    port = 80
                    encodedPathSegments = listOf("api", "v1", "chat", "ws")
                }
                header(HttpHeaders.Authorization, "Bearer $nonMemberToken")
            }
        nonMemberSession.outgoing.send(Frame.Text("""{"event":"joinRoom","data":{"roomId":1}}"""))
        val forbiddenJoin = (nonMemberSession.incoming.receive() as Frame.Text).readText()
        nonMemberSession.outgoing.send(Frame.Text("""{"event":"sendMessage","data":{"roomId":1,"content":"권한 없는 메시지"}}"""))
        val forbiddenSend = (nonMemberSession.incoming.receive() as Frame.Text).readText()

        assertTrue(forbiddenJoin.contains("\"event\":\"error\""))
        assertTrue(forbiddenSend.contains("\"event\":\"error\""))

        nonMemberSession.outgoing.send(Frame.Close())
    }
}
