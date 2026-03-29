package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.friend

import java.time.Instant

data class FriendUserRecord(
    val id: Int,
    val email: String?,
    val name: String,
    val nickname: String?,
    val profileImageUrl: String?,
    val role: String?,
)

data class FriendshipRecord(
    val id: Int,
    val requesterId: Int,
    val addresseeId: Int,
    val status: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val requester: FriendUserRecord,
    val addressee: FriendUserRecord,
)

data class FriendActivityRecord(
    val id: Int,
    val userId: Int,
    val type: String,
    val message: String,
    val metadata: Map<String, String>?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val user: FriendUserRecord,
)

data class FriendPageResult<T>(
    val items: List<T>,
    val totalCount: Int,
)

interface FriendRepository {
    fun userExists(userId: Int): Boolean

    fun findFriendshipPair(userId: Int, targetUserId: Int): FriendshipRecord?

    fun findFriendshipById(friendshipId: Int): FriendshipRecord?

    fun saveFriendship(
        requesterId: Int,
        addresseeId: Int,
        status: String,
        friendshipId: Int? = null,
    ): FriendshipRecord

    fun deleteFriendshipPair(userId: Int, targetUserId: Int): Boolean

    fun listFriends(userId: Int, page: Int, limit: Int): FriendPageResult<FriendshipRecord>

    fun listReceivedRequests(userId: Int, page: Int, limit: Int): FriendPageResult<FriendshipRecord>

    fun listSentRequests(userId: Int, page: Int, limit: Int): FriendPageResult<FriendshipRecord>

    fun listFeed(userId: Int, page: Int, limit: Int): FriendPageResult<FriendActivityRecord>

    fun isBlockedBetween(userId: Int, targetUserId: Int): Boolean

    fun createBlock(userId: Int, targetUserId: Int)

    fun deleteBlock(userId: Int, targetUserId: Int): Boolean

    fun createActivity(
        userId: Int,
        type: String,
        message: String,
        metadata: Map<String, String>?,
    )
}
