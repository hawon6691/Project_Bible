package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.user

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod

fun userOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/users/me", "User", "Current user", roles = setOf(PbRole.USER)) {
            StubResponse(
                data =
                    mapOf(
                        "id" to 4,
                        "email" to "user1@nestshop.com",
                        "name" to "홍길동",
                        "phone" to "010-1234-5678",
                        "role" to "USER",
                        "status" to "ACTIVE",
                        "point" to 53000,
                        "badges" to listOf(mapOf("id" to 1, "name" to "첫 구매", "iconUrl" to "https://img.example.com/badge-first.png")),
                        "createdAt" to "2026-01-01T00:00:00Z",
                    ),
            )
        },
        endpoint(HttpMethod.Patch, "/users/me", "User", "Update current user", roles = setOf(PbRole.USER)) {
            StubResponse(
                data =
                    mapOf(
                        "id" to 4,
                        "email" to "user1@nestshop.com",
                        "name" to "홍길동",
                        "phone" to "010-1234-5678",
                        "role" to "USER",
                        "status" to "ACTIVE",
                        "point" to 53000,
                        "badges" to emptyList<Map<String, Any?>>(),
                        "createdAt" to "2026-01-01T00:00:00Z",
                    ),
            )
        },
        endpoint(HttpMethod.Delete, "/users/me", "User", "Delete current user", roles = setOf(PbRole.USER)) {
            message("회원 탈퇴가 완료되었습니다.")
        },
        endpoint(HttpMethod.Get, "/users", "User", "Admin user list", roles = setOf(PbRole.ADMIN)) {
            paged(
                listOf(
                    mapOf("id" to 4, "email" to "user1@nestshop.com", "role" to "USER", "status" to "ACTIVE"),
                    mapOf("id" to 2, "email" to "seller1@nestshop.com", "role" to "SELLER", "status" to "ACTIVE"),
                ),
            )
        },
        endpoint(HttpMethod.Patch, "/users/{id}/status", "User", "Update user status", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("id" to 6, "email" to "user3@nestshop.com", "status" to "ACTIVE"))
        },
        endpoint(HttpMethod.Get, "/users/{id}/profile", "User", "Public profile") {
            StubResponse(data = mapOf("id" to 4, "nickname" to "hong01", "bio" to "게이밍 유저", "profileImageUrl" to null))
        },
        endpoint(HttpMethod.Patch, "/users/me/profile", "User", "Update my profile", roles = setOf(PbRole.USER)) {
            StubResponse(data = mapOf("id" to 4, "nickname" to "hong01", "bio" to "게이밍 유저", "profileImageUrl" to null))
        },
        endpoint(HttpMethod.Post, "/users/me/profile-image", "User", "Update profile image", roles = setOf(PbRole.USER)) {
            StubResponse(data = mapOf("imageUrl" to "https://img.pbshop.dev/profiles/4.webp"))
        },
        endpoint(HttpMethod.Delete, "/users/me/profile-image", "User", "Delete profile image", roles = setOf(PbRole.USER)) {
            message("프로필 이미지가 기본 이미지로 초기화되었습니다.")
        },
    )
