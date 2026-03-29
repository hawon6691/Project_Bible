package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.friend

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.FriendActivitiesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.FriendBlocksTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.FriendshipsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UsersTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant

class ExposedDaoFriendRepository(
    private val databaseFactory: DatabaseFactory,
) : FriendRepository {
    override fun userExists(userId: Int): Boolean =
        databaseFactory.withTransaction { !UsersTable.selectAll().where { UsersTable.id eq userId }.empty() }

    override fun findFriendshipPair(userId: Int, targetUserId: Int): FriendshipRecord? =
        databaseFactory.withTransaction {
            FriendshipsTable.selectAll()
                .where {
                    ((FriendshipsTable.requester eq userId) and (FriendshipsTable.addressee eq targetUserId)) or
                        ((FriendshipsTable.requester eq targetUserId) and (FriendshipsTable.addressee eq userId))
                }
                .singleOrNull()
                ?.let(::toFriendshipRecord)
        }

    override fun findFriendshipById(friendshipId: Int): FriendshipRecord? =
        databaseFactory.withTransaction {
            FriendshipsTable.selectAll().where { FriendshipsTable.id eq friendshipId }.singleOrNull()?.let(::toFriendshipRecord)
        }

    override fun saveFriendship(requesterId: Int, addresseeId: Int, status: String, friendshipId: Int?): FriendshipRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val targetId =
                if (friendshipId == null) {
                    FriendshipsTable.insert {
                        it[requester] = EntityID(requesterId, UsersTable)
                        it[addressee] = EntityID(addresseeId, UsersTable)
                        it[FriendshipsTable.status] = status
                        it[createdAt] = now
                        it[updatedAt] = now
                    } get FriendshipsTable.id
                } else {
                    FriendshipsTable.update({ FriendshipsTable.id eq friendshipId }) {
                        it[requester] = EntityID(requesterId, UsersTable)
                        it[addressee] = EntityID(addresseeId, UsersTable)
                        it[FriendshipsTable.status] = status
                        it[updatedAt] = now
                    }
                    EntityID(friendshipId, FriendshipsTable)
                }
            findFriendshipById(targetId.value) ?: error("Friendship ${targetId.value} not found")
        }

    override fun deleteFriendshipPair(userId: Int, targetUserId: Int): Boolean =
        databaseFactory.withTransaction {
            FriendshipsTable.deleteWhere {
                ((requester eq userId) and (addressee eq targetUserId)) or
                    ((requester eq targetUserId) and (addressee eq userId))
            } > 0
        }

    override fun listFriends(userId: Int, page: Int, limit: Int): FriendPageResult<FriendshipRecord> =
        paginateFriendships(
            page = page,
            limit = limit,
            predicate = ((FriendshipsTable.requester eq userId) or (FriendshipsTable.addressee eq userId)) and (FriendshipsTable.status eq "ACCEPTED"),
        )

    override fun listReceivedRequests(userId: Int, page: Int, limit: Int): FriendPageResult<FriendshipRecord> =
        paginateFriendships(page, limit, (FriendshipsTable.addressee eq userId) and (FriendshipsTable.status eq "PENDING"))

    override fun listSentRequests(userId: Int, page: Int, limit: Int): FriendPageResult<FriendshipRecord> =
        paginateFriendships(page, limit, (FriendshipsTable.requester eq userId) and (FriendshipsTable.status eq "PENDING"))

    override fun listFeed(userId: Int, page: Int, limit: Int): FriendPageResult<FriendActivityRecord> =
        databaseFactory.withTransaction {
            val friendIds =
                FriendshipsTable.selectAll()
                    .where {
                        ((FriendshipsTable.requester eq userId) or (FriendshipsTable.addressee eq userId)) and (FriendshipsTable.status eq "ACCEPTED")
                    }
                    .map { if (it[FriendshipsTable.requester].value == userId) it[FriendshipsTable.addressee].value else it[FriendshipsTable.requester].value }
                    .toSet()
            if (friendIds.isEmpty()) {
                return@withTransaction FriendPageResult(emptyList(), 0)
            }
            val rows =
                FriendActivitiesTable.selectAll()
                    .where { FriendActivitiesTable.user inList friendIds.toList() }
                    .orderBy(FriendActivitiesTable.createdAt to SortOrder.DESC)
                    .toList()
            val offset = (page - 1).coerceAtLeast(0) * limit
            FriendPageResult(rows.drop(offset).take(limit).map(::toActivityRecord), rows.size)
        }

    override fun isBlockedBetween(userId: Int, targetUserId: Int): Boolean =
        databaseFactory.withTransaction {
            !FriendBlocksTable.selectAll()
                .where {
                    ((FriendBlocksTable.user eq userId) and (FriendBlocksTable.blockedUser eq targetUserId)) or
                        ((FriendBlocksTable.user eq targetUserId) and (FriendBlocksTable.blockedUser eq userId))
                }
                .empty()
        }

    override fun createBlock(userId: Int, targetUserId: Int) {
        databaseFactory.withTransaction {
            val exists =
                FriendBlocksTable.selectAll()
                    .where { (FriendBlocksTable.user eq userId) and (FriendBlocksTable.blockedUser eq targetUserId) }
                    .singleOrNull()
            if (exists == null) {
                val now = Instant.now()
                FriendBlocksTable.insert {
                    it[user] = EntityID(userId, UsersTable)
                    it[blockedUser] = EntityID(targetUserId, UsersTable)
                    it[createdAt] = now
                    it[updatedAt] = now
                }
            }
        }
    }

    override fun deleteBlock(userId: Int, targetUserId: Int): Boolean =
        databaseFactory.withTransaction {
            FriendBlocksTable.deleteWhere { (FriendBlocksTable.user eq userId) and (FriendBlocksTable.blockedUser eq targetUserId) } > 0
        }

    override fun createActivity(userId: Int, type: String, message: String, metadata: Map<String, String>?) {
        databaseFactory.withTransaction {
            val now = Instant.now()
            FriendActivitiesTable.insert {
                it[user] = EntityID(userId, UsersTable)
                it[FriendActivitiesTable.type] = type
                it[FriendActivitiesTable.message] = message
                it[FriendActivitiesTable.metadata] = metadata?.let(::encodeMetadata)
                it[createdAt] = now
                it[updatedAt] = now
            }
        }
    }

    private fun paginateFriendships(
        page: Int,
        limit: Int,
        predicate: org.jetbrains.exposed.sql.Op<Boolean>,
    ): FriendPageResult<FriendshipRecord> =
        databaseFactory.withTransaction {
            val rows =
                FriendshipsTable.selectAll()
                    .where { predicate }
                    .orderBy(FriendshipsTable.updatedAt to SortOrder.DESC)
                    .toList()
            val offset = (page - 1).coerceAtLeast(0) * limit
            FriendPageResult(rows.drop(offset).take(limit).map(::toFriendshipRecord), rows.size)
        }

    private fun toFriendshipRecord(row: org.jetbrains.exposed.sql.ResultRow): FriendshipRecord =
        FriendshipRecord(
            id = row[FriendshipsTable.id].value,
            requesterId = row[FriendshipsTable.requester].value,
            addresseeId = row[FriendshipsTable.addressee].value,
            status = row[FriendshipsTable.status],
            createdAt = row[FriendshipsTable.createdAt],
            updatedAt = row[FriendshipsTable.updatedAt],
            requester = loadUser(row[FriendshipsTable.requester].value),
            addressee = loadUser(row[FriendshipsTable.addressee].value),
        )

    private fun toActivityRecord(row: org.jetbrains.exposed.sql.ResultRow): FriendActivityRecord =
        FriendActivityRecord(
            id = row[FriendActivitiesTable.id].value,
            userId = row[FriendActivitiesTable.user].value,
            type = row[FriendActivitiesTable.type],
            message = row[FriendActivitiesTable.message],
            metadata = row[FriendActivitiesTable.metadata]?.let(::decodeMetadata),
            createdAt = row[FriendActivitiesTable.createdAt],
            updatedAt = row[FriendActivitiesTable.updatedAt],
            user = loadUser(row[FriendActivitiesTable.user].value),
        )

    private fun loadUser(userId: Int): FriendUserRecord {
        val row = UsersTable.selectAll().where { UsersTable.id eq userId }.single()
        return FriendUserRecord(
            id = row[UsersTable.id].value,
            email = row[UsersTable.email],
            name = row[UsersTable.name],
            nickname = row[UsersTable.nickname],
            profileImageUrl = row[UsersTable.profileImageUrl],
            role = row[UsersTable.role].name,
        )
    }

    private fun encodeMetadata(metadata: Map<String, String>): String =
        metadata.entries.joinToString(prefix = "{", postfix = "}") { (key, value) -> "\"$key\":\"$value\"" }

    private fun decodeMetadata(raw: String): Map<String, String> =
        raw.removePrefix("{").removeSuffix("}")
            .split(",")
            .mapNotNull {
                val parts = it.split(":")
                if (parts.size == 2) parts[0].trim().trim('"') to parts[1].trim().trim('"') else null
            }.toMap()
}
