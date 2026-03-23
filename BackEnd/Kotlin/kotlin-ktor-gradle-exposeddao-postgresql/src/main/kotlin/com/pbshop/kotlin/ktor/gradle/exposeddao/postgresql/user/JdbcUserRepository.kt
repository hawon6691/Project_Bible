package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.user

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth.AuthUserStatus
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import java.sql.ResultSet

class JdbcUserRepository(
    private val databaseFactory: DatabaseFactory,
) : UserRepository {
    override fun findUserById(id: Int): UserRecord? =
        databaseFactory.withConnection { connection ->
            connection.prepareStatement(
                """
                SELECT id, email, password, name, phone, role, status, point, nickname, bio, profile_image_url, email_verified, created_at, deleted_at
                FROM users
                WHERE id = ?
                LIMIT 1
                """.trimIndent(),
            ).use { statement ->
                statement.setInt(1, id)
                statement.executeQuery().use { resultSet -> resultSet.singleUserOrNull() }
            }
        }

    override fun findFirstUserByRole(role: PbRole): UserRecord? =
        databaseFactory.withConnection { connection ->
            connection.prepareStatement(
                """
                SELECT id, email, password, name, phone, role, status, point, nickname, bio, profile_image_url, email_verified, created_at, deleted_at
                FROM users
                WHERE role = CAST(? AS user_role)
                  AND deleted_at IS NULL
                ORDER BY id ASC
                LIMIT 1
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, role.name)
                statement.executeQuery().use { resultSet -> resultSet.singleUserOrNull() }
            }
        }

    override fun listUsers(query: UserListQuery): UserListResult =
        databaseFactory.withConnection { connection ->
            val conditions = mutableListOf("deleted_at IS NULL")
            val bindValues = mutableListOf<Any>()

            query.search?.let {
                conditions += "(email ILIKE ? OR name ILIKE ?)"
                bindValues += "%$it%"
                bindValues += "%$it%"
            }
            query.status?.let {
                conditions += "status = CAST(? AS user_status)"
                bindValues += it.name
            }
            query.role?.let {
                conditions += "role = CAST(? AS user_role)"
                bindValues += it.name
            }

            val whereClause = conditions.joinToString(" AND ")

            val totalCount =
                connection.prepareStatement(
                    "SELECT COUNT(*) FROM users WHERE $whereClause",
                ).use { statement ->
                    bind(statement, bindValues)
                    statement.executeQuery().use { resultSet ->
                        resultSet.next()
                        resultSet.getInt(1)
                    }
                }

            val offset = (query.page - 1) * query.limit
            val items =
                connection.prepareStatement(
                    """
                    SELECT id, email, password, name, phone, role, status, point, nickname, bio, profile_image_url, email_verified, created_at, deleted_at
                    FROM users
                    WHERE $whereClause
                    ORDER BY id ASC
                    LIMIT ? OFFSET ?
                    """.trimIndent(),
                ).use { statement ->
                    bind(statement, bindValues + listOf(query.limit, offset))
                    statement.executeQuery().use { resultSet ->
                        buildList {
                            while (resultSet.next()) {
                                add(resultSet.toUserRecord())
                            }
                        }
                    }
                }

            UserListResult(items = items, totalCount = totalCount)
        }

    override fun findBadgesByUserId(userId: Int): List<UserBadgeRecord> =
        databaseFactory.withConnection { connection ->
            connection.prepareStatement(
                """
                SELECT b.id, b.name, b.icon_url
                FROM user_badges ub
                INNER JOIN badges b ON b.id = ub.badge_id
                WHERE ub.user_id = ?
                ORDER BY b.id ASC
                """.trimIndent(),
            ).use { statement ->
                statement.setInt(1, userId)
                statement.executeQuery().use { resultSet ->
                    buildList {
                        while (resultSet.next()) {
                            add(
                                UserBadgeRecord(
                                    id = resultSet.getInt("id"),
                                    name = resultSet.getString("name"),
                                    iconUrl = resultSet.getString("icon_url"),
                                ),
                            )
                        }
                    }
                }
            }
        }

    override fun updateUser(
        userId: Int,
        name: String?,
        phone: String?,
        passwordHash: String?,
    ): UserRecord =
        databaseFactory.withConnection { connection ->
            val current = requireNotNull(findUserById(userId)) { "User $userId not found" }
            connection.prepareStatement(
                """
                UPDATE users
                SET name = ?,
                    phone = ?,
                    password = ?,
                    updated_at = NOW()
                WHERE id = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, name ?: current.name)
                statement.setString(2, phone ?: current.phone)
                statement.setString(3, passwordHash ?: current.passwordHash)
                statement.setInt(4, userId)
                statement.executeUpdate()
            }
            requireNotNull(findUserById(userId))
        }

    override fun updateUserStatus(
        userId: Int,
        status: AuthUserStatus,
    ): UserRecord =
        databaseFactory.withConnection { connection ->
            connection.prepareStatement(
                """
                UPDATE users
                SET status = CAST(? AS user_status),
                    updated_at = NOW()
                WHERE id = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, status.name)
                statement.setInt(2, userId)
                statement.executeUpdate()
            }
            requireNotNull(findUserById(userId))
        }

    override fun updateUserProfile(
        userId: Int,
        nickname: String?,
        bio: String?,
    ): UserRecord =
        databaseFactory.withConnection { connection ->
            val current = requireNotNull(findUserById(userId)) { "User $userId not found" }
            connection.prepareStatement(
                """
                UPDATE users
                SET nickname = ?,
                    bio = ?,
                    updated_at = NOW()
                WHERE id = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, nickname ?: current.nickname)
                statement.setString(2, bio ?: current.bio)
                statement.setInt(3, userId)
                statement.executeUpdate()
            }
            requireNotNull(findUserById(userId))
        }

    override fun updateProfileImage(
        userId: Int,
        profileImageUrl: String?,
    ): UserRecord =
        databaseFactory.withConnection { connection ->
            connection.prepareStatement(
                """
                UPDATE users
                SET profile_image_url = ?,
                    updated_at = NOW()
                WHERE id = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, profileImageUrl)
                statement.setInt(2, userId)
                statement.executeUpdate()
            }
            requireNotNull(findUserById(userId))
        }

    override fun softDeleteUser(userId: Int) {
        databaseFactory.withConnection { connection ->
            connection.prepareStatement(
                """
                UPDATE users
                SET status = 'INACTIVE',
                    deleted_at = NOW(),
                    updated_at = NOW()
                WHERE id = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setInt(1, userId)
                statement.executeUpdate()
            }
        }
    }

    private fun bind(
        statement: java.sql.PreparedStatement,
        values: List<Any>,
    ) {
        values.forEachIndexed { index, value ->
            when (value) {
                is String -> statement.setString(index + 1, value)
                is Int -> statement.setInt(index + 1, value)
                else -> statement.setObject(index + 1, value)
            }
        }
    }

    private fun ResultSet.singleUserOrNull(): UserRecord? {
        if (!next()) {
            return null
        }
        return toUserRecord()
    }

    private fun ResultSet.toUserRecord(): UserRecord =
        UserRecord(
            id = getInt("id"),
            email = getString("email"),
            passwordHash = getString("password"),
            name = getString("name"),
            phone = getString("phone"),
            role = PbRole.valueOf(getString("role")),
            status = AuthUserStatus.valueOf(getString("status")),
            point = getInt("point"),
            nickname = getString("nickname"),
            bio = getString("bio"),
            profileImageUrl = getString("profile_image_url"),
            emailVerified = getBoolean("email_verified"),
            createdAt = getTimestamp("created_at").toInstant(),
            deletedAt = getTimestamp("deleted_at")?.toInstant(),
        )
}
