package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.friend

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import java.time.Instant

class InMemoryFriendRepository(
    users: List<FriendUserRecord>,
    friendships: List<FriendshipRecord>,
    activities: List<FriendActivityRecord>,
    blocks: Set<Pair<Int, Int>>,
) : FriendRepository {
    private val users = users.associateBy { it.id }.toMutableMap()
    private val friendships = linkedMapOf<Int, FriendshipRecord>()
    private val activities = linkedMapOf<Int, FriendActivityRecord>()
    private val blocks = blocks.toMutableSet()
    private var friendshipSequence = 1
    private var activitySequence = 1

    init {
        friendships.forEach {
            this.friendships[it.id] = it
            friendshipSequence = maxOf(friendshipSequence, it.id + 1)
        }
        activities.forEach {
            this.activities[it.id] = it
            activitySequence = maxOf(activitySequence, it.id + 1)
        }
    }

    override fun userExists(userId: Int): Boolean = users.containsKey(userId)

    override fun findFriendshipPair(
        userId: Int,
        targetUserId: Int,
    ): FriendshipRecord? =
        friendships.values.firstOrNull {
            (it.requesterId == userId && it.addresseeId == targetUserId) ||
                (it.requesterId == targetUserId && it.addresseeId == userId)
        }

    override fun findFriendshipById(friendshipId: Int): FriendshipRecord? = friendships[friendshipId]

    override fun saveFriendship(
        requesterId: Int,
        addresseeId: Int,
        status: String,
        friendshipId: Int?,
    ): FriendshipRecord {
        val now = Instant.now()
        val requester = requireUser(requesterId)
        val addressee = requireUser(addresseeId)
        val saved =
            if (friendshipId == null) {
                FriendshipRecord(
                    id = friendshipSequence++,
                    requesterId = requesterId,
                    addresseeId = addresseeId,
                    status = status,
                    createdAt = now,
                    updatedAt = now,
                    requester = requester,
                    addressee = addressee,
                )
            } else {
                val current = friendships[friendshipId] ?: error("Friendship $friendshipId not found")
                current.copy(status = status, updatedAt = now, requester = requester, addressee = addressee)
            }
        friendships[saved.id] = saved
        return saved
    }

    override fun deleteFriendshipPair(
        userId: Int,
        targetUserId: Int,
    ): Boolean =
        friendships.entries.removeIf {
            (it.value.requesterId == userId && it.value.addresseeId == targetUserId) ||
                (it.value.requesterId == targetUserId && it.value.addresseeId == userId)
        }

    override fun listFriends(
        userId: Int,
        page: Int,
        limit: Int,
    ): FriendPageResult<FriendshipRecord> = paginate(friendships.values.filterAcceptedFor(userId), page, limit)

    override fun listReceivedRequests(
        userId: Int,
        page: Int,
        limit: Int,
    ): FriendPageResult<FriendshipRecord> =
        paginate(
            friendships.values
                .filter { it.addresseeId == userId && it.status == "PENDING" }
                .sortedByDescending { it.createdAt },
            page,
            limit,
        )

    override fun listSentRequests(
        userId: Int,
        page: Int,
        limit: Int,
    ): FriendPageResult<FriendshipRecord> =
        paginate(
            friendships.values
                .filter { it.requesterId == userId && it.status == "PENDING" }
                .sortedByDescending { it.createdAt },
            page,
            limit,
        )

    override fun listFeed(
        userId: Int,
        page: Int,
        limit: Int,
    ): FriendPageResult<FriendActivityRecord> {
        val friendIds =
            friendships.values
                .filterAcceptedFor(userId)
                .map { if (it.requesterId == userId) it.addresseeId else it.requesterId }
                .toSet()
        val filtered =
            activities.values
                .filter { friendIds.contains(it.userId) }
                .sortedByDescending { it.createdAt }
        return paginate(filtered, page, limit)
    }

    override fun isBlockedBetween(
        userId: Int,
        targetUserId: Int,
    ): Boolean = blocks.contains(userId to targetUserId) || blocks.contains(targetUserId to userId)

    override fun createBlock(
        userId: Int,
        blockedUserId: Int,
    ) {
        blocks += userId to blockedUserId
    }

    override fun deleteBlock(
        userId: Int,
        blockedUserId: Int,
    ): Boolean = blocks.remove(userId to blockedUserId)

    override fun createActivity(
        userId: Int,
        type: String,
        message: String,
        metadata: Map<String, String>?,
    ) {
        val now = Instant.now()
        val saved =
            FriendActivityRecord(
                id = activitySequence++,
                userId = userId,
                type = type,
                message = message,
                metadata = metadata,
                createdAt = now,
                updatedAt = now,
                user = requireUser(userId),
            )
        activities[saved.id] = saved
    }

    private fun Iterable<FriendshipRecord>.filterAcceptedFor(userId: Int): List<FriendshipRecord> =
        filter { it.status == "ACCEPTED" && (it.requesterId == userId || it.addresseeId == userId) }
            .sortedByDescending { it.updatedAt }

    private fun <T> paginate(
        records: List<T>,
        page: Int,
        limit: Int,
    ): FriendPageResult<T> {
        val offset = (page - 1).coerceAtLeast(0) * limit
        return FriendPageResult(items = records.drop(offset).take(limit), totalCount = records.size)
    }

    private fun requireUser(userId: Int): FriendUserRecord = users[userId] ?: error("User $userId not found")

    companion object {
        fun seeded(): InMemoryFriendRepository {
            val now = Instant.parse("2026-03-01T09:00:00Z")
            val admin = FriendUserRecord(1, "admin@nestshop.com", "관리자", "admin01", null, PbRole.ADMIN.name)
            val user1 = FriendUserRecord(4, "user1@nestshop.com", "홍길동", "hong01", null, PbRole.USER.name)
            val user2 = FriendUserRecord(5, "user2@nestshop.com", "김영희", "kim02", null, PbRole.USER.name)
            val user3 = FriendUserRecord(6, "user3@nestshop.com", "이철수", "lee03", null, PbRole.USER.name)

            val accepted =
                FriendshipRecord(
                    id = 1,
                    requesterId = 4,
                    addresseeId = 5,
                    status = "ACCEPTED",
                    createdAt = now.minusSeconds(86_400),
                    updatedAt = now.minusSeconds(43_200),
                    requester = user1,
                    addressee = user2,
                )
            val pending =
                FriendshipRecord(
                    id = 2,
                    requesterId = 6,
                    addresseeId = 4,
                    status = "PENDING",
                    createdAt = now.minusSeconds(7_200),
                    updatedAt = now.minusSeconds(7_200),
                    requester = user3,
                    addressee = user1,
                )
            val activity =
                FriendActivityRecord(
                    id = 1,
                    userId = 5,
                    type = "REVIEW_CREATED",
                    message = "김영희님이 새 리뷰를 작성했습니다.",
                    metadata = mapOf("reviewId" to "10"),
                    createdAt = now.minusSeconds(3_600),
                    updatedAt = now.minusSeconds(3_600),
                    user = user2,
                )
            return InMemoryFriendRepository(
                users = listOf(admin, user1, user2, user3),
                friendships = listOf(accepted, pending),
                activities = listOf(activity),
                blocks = emptySet(),
            )
        }
    }
}
