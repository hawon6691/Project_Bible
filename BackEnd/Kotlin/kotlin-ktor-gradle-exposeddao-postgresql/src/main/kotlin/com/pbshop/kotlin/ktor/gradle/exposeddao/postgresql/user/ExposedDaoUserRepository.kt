package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.user

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth.AuthUserStatus
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.BadgesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UserBadgesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UserEntity
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UsersTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class ExposedDaoUserRepository(
    private val databaseFactory: DatabaseFactory,
) : UserRepository {
    override fun findUserById(id: Int): UserRecord? =
        databaseFactory.withTransaction {
            UserEntity.findById(id)?.toUserRecord()
        }

    override fun findFirstUserByRole(role: PbRole): UserRecord? =
        databaseFactory.withTransaction {
            UserEntity.find { (UsersTable.role eq role) and UsersTable.deletedAt.isNull() }
                .orderBy(UsersTable.id to SortOrder.ASC)
                .limit(1)
                .firstOrNull()
                ?.toUserRecord()
        }

    override fun listUsers(query: UserListQuery): UserListResult =
        databaseFactory.withTransaction {
            val predicate = buildQueryPredicate(query)
            val totalCount = UsersTable.select(UsersTable.id.count()).where { predicate }
                .single()[UsersTable.id.count()].toInt()
            val offset = ((query.page - 1) * query.limit).toLong()
            val items =
                UsersTable.selectAll()
                    .where { predicate }
                    .orderBy(UsersTable.id to SortOrder.ASC)
                    .limit(query.limit, offset)
                    .map(::toUserRecord)

            UserListResult(items = items, totalCount = totalCount)
        }

    override fun findBadgesByUserId(userId: Int): List<UserBadgeRecord> =
        databaseFactory.withTransaction {
            UserBadgesTable.innerJoin(BadgesTable)
                .select(BadgesTable.id, BadgesTable.name, BadgesTable.iconUrl)
                .where { UserBadgesTable.user eq userId }
                .orderBy(BadgesTable.id to SortOrder.ASC)
                .map { row ->
                    UserBadgeRecord(
                        id = row[BadgesTable.id].value,
                        name = row[BadgesTable.name],
                        iconUrl = row[BadgesTable.iconUrl],
                    )
                }
        }

    override fun updateUser(
        userId: Int,
        name: String?,
        phone: String?,
        passwordHash: String?,
    ): UserRecord =
        databaseFactory.withTransaction {
            val user = requireNotNull(UserEntity.findById(userId)) { "User $userId not found" }
            user.apply {
                this.name = name ?: this.name
                this.phone = phone ?: this.phone
                password = passwordHash ?: password
                updatedAt = Instant.now()
            }.toUserRecord()
        }

    override fun updateUserStatus(
        userId: Int,
        status: AuthUserStatus,
    ): UserRecord =
        databaseFactory.withTransaction {
            val user = requireNotNull(UserEntity.findById(userId)) { "User $userId not found" }
            user.apply {
                this.status = status
                updatedAt = Instant.now()
            }.toUserRecord()
        }

    override fun updateUserProfile(
        userId: Int,
        nickname: String?,
        bio: String?,
    ): UserRecord =
        databaseFactory.withTransaction {
            val user = requireNotNull(UserEntity.findById(userId)) { "User $userId not found" }
            user.apply {
                this.nickname = nickname ?: this.nickname
                this.bio = bio ?: this.bio
                updatedAt = Instant.now()
            }.toUserRecord()
        }

    override fun updateProfileImage(
        userId: Int,
        profileImageUrl: String?,
    ): UserRecord =
        databaseFactory.withTransaction {
            val user = requireNotNull(UserEntity.findById(userId)) { "User $userId not found" }
            user.apply {
                this.profileImageUrl = profileImageUrl
                updatedAt = Instant.now()
            }.toUserRecord()
        }

    override fun softDeleteUser(userId: Int) {
        databaseFactory.withTransaction {
            UserEntity.findById(userId)?.apply {
                status = AuthUserStatus.INACTIVE
                deletedAt = Instant.now()
                updatedAt = Instant.now()
            }
        }
    }

    private fun buildQueryPredicate(query: UserListQuery): Op<Boolean> {
        var predicate: Op<Boolean> = UsersTable.deletedAt.isNull()
        query.search?.trim()?.takeIf { it.isNotBlank() }?.let { raw ->
            val search = "%$raw%"
            predicate =
                predicate and (
                    UsersTable.email like search or
                        (UsersTable.name like search)
                )
        }
        query.status?.let { predicate = predicate and (UsersTable.status eq it) }
        query.role?.let { predicate = predicate and (UsersTable.role eq it) }
        return predicate
    }

    private fun UserEntity.toUserRecord(): UserRecord =
        UserRecord(
            id = id.value,
            email = email,
            passwordHash = password,
            name = name,
            phone = phone,
            role = role,
            status = status,
            point = point,
            nickname = nickname,
            bio = bio,
            profileImageUrl = profileImageUrl,
            emailVerified = emailVerified,
            createdAt = createdAt,
            deletedAt = deletedAt,
        )

    private fun toUserRecord(row: ResultRow): UserRecord =
        UserRecord(
            id = row[UsersTable.id].value,
            email = row[UsersTable.email],
            passwordHash = row[UsersTable.password],
            name = row[UsersTable.name],
            phone = row[UsersTable.phone],
            role = row[UsersTable.role],
            status = row[UsersTable.status],
            point = row[UsersTable.point],
            nickname = row[UsersTable.nickname],
            bio = row[UsersTable.bio],
            profileImageUrl = row[UsersTable.profileImageUrl],
            emailVerified = row[UsersTable.emailVerified],
            createdAt = row[UsersTable.createdAt],
            deletedAt = row[UsersTable.deletedAt],
        )
}
