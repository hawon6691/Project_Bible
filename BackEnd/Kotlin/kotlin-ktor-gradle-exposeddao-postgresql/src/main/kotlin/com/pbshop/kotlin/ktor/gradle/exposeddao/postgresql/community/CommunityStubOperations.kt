package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.community

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun communityOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/boards", "Community", "Board list") {
            StubResponse(data = listOf(mapOf("id" to 1, "name" to "Reviews"), mapOf("id" to 2, "name" to "Deals")))
        },
        endpoint(HttpMethod.Get, "/boards/{boardId}/posts", "Community", "Board posts") { paged(listOf(mapOf("id" to 1, "title" to "GalaxyBook 4 usage review", "likeCount" to 45))) },
        endpoint(HttpMethod.Get, "/posts/{id}", "Community", "Post detail") { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "title" to "GalaxyBook 4 usage review", "content" to "Detailed impressions"))
        },
        endpoint(HttpMethod.Post, "/boards/{boardId}/posts", "Community", "Create post", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 5, "title" to "New community post"))
        },
        endpoint(HttpMethod.Patch, "/posts/{id}", "Community", "Update post", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "title" to "Updated post"))
        },
        endpoint(HttpMethod.Delete, "/posts/{id}", "Community", "Delete post", roles = setOf(PbRole.USER, PbRole.ADMIN)) { message("Post deleted.") },
        endpoint(HttpMethod.Post, "/posts/{id}/like", "Community", "Toggle post like", roles = setOf(PbRole.USER)) { StubResponse(data = mapOf("liked" to true, "likeCount" to 46)) },
        endpoint(HttpMethod.Get, "/posts/{id}/comments", "Community", "Post comments") { StubResponse(data = listOf(mapOf("id" to 1, "content" to "Helpful review."))) },
        endpoint(HttpMethod.Post, "/posts/{id}/comments", "Community", "Create post comment", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "content" to "Great insight."))
        },
        endpoint(HttpMethod.Delete, "/comments/{id}", "Community", "Delete post comment", roles = setOf(PbRole.USER, PbRole.ADMIN)) { message("Comment deleted.") },
    )
