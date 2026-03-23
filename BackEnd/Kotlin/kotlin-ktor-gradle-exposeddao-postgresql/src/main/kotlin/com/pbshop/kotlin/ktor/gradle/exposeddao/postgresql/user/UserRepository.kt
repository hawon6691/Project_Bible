package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.user

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth.AuthUserStatus
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import java.time.Instant

data class UserBadgeRecord(
    val id: Int,
    val name: String,
    val iconUrl: String,
)

data class UserRecord(
    val id: Int,
    val email: String,
    val passwordHash: String,
    val name: String,
    val phone: String,
    val role: PbRole,
    val status: AuthUserStatus,
    val point: Int,
    val nickname: String,
    val bio: String?,
    val profileImageUrl: String?,
    val emailVerified: Boolean,
    val createdAt: Instant,
    val deletedAt: Instant?,
)

data class UserListQuery(
    val page: Int,
    val limit: Int,
    val search: String?,
    val status: AuthUserStatus?,
    val role: PbRole?,
)

data class UserListResult(
    val items: List<UserRecord>,
    val totalCount: Int,
)

interface UserRepository {
    fun findUserById(id: Int): UserRecord?

    fun findFirstUserByRole(role: PbRole): UserRecord?

    fun listUsers(query: UserListQuery): UserListResult

    fun findBadgesByUserId(userId: Int): List<UserBadgeRecord>

    fun updateUser(
        userId: Int,
        name: String?,
        phone: String?,
        passwordHash: String?,
    ): UserRecord

    fun updateUserStatus(
        userId: Int,
        status: AuthUserStatus,
    ): UserRecord

    fun updateUserProfile(
        userId: Int,
        nickname: String?,
        bio: String?,
    ): UserRecord

    fun updateProfileImage(
        userId: Int,
        profileImageUrl: String?,
    ): UserRecord

    fun softDeleteUser(userId: Int)
}
