package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.category

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun categoryOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/categories", "Category", "Category list") {
            StubResponse(
                data =
                    listOf(
                        mapOf(
                            "id" to 1,
                            "name" to "컴퓨터",
                            "parentId" to null,
                            "sortOrder" to 1,
                            "children" to
                                listOf(
                                    mapOf("id" to 2, "name" to "노트북", "parentId" to 1, "sortOrder" to 1, "children" to emptyList<Map<String, Any?>>()),
                                    mapOf("id" to 3, "name" to "데스크탑", "parentId" to 1, "sortOrder" to 2, "children" to emptyList<Map<String, Any?>>()),
                                ),
                        ),
                    ),
            )
        },
        endpoint(HttpMethod.Get, "/categories/{id}", "Category", "Category detail") {
            StubResponse(data = mapOf("id" to 2, "name" to "노트북", "parentId" to 1, "sortOrder" to 1, "children" to emptyList<Map<String, Any?>>()))
        },
        endpoint(HttpMethod.Post, "/categories", "Category", "Create category", roles = setOf(PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 6, "name" to "태블릿", "parentId" to null, "sortOrder" to 3, "children" to emptyList<Map<String, Any?>>()))
        },
        endpoint(HttpMethod.Patch, "/categories/{id}", "Category", "Update category", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("id" to 2, "name" to "노트북", "parentId" to 1, "sortOrder" to 10, "children" to emptyList<Map<String, Any?>>()))
        },
        endpoint(HttpMethod.Delete, "/categories/{id}", "Category", "Delete category", roles = setOf(PbRole.ADMIN)) {
            message("카테고리가 삭제되었습니다.")
        },
    )
