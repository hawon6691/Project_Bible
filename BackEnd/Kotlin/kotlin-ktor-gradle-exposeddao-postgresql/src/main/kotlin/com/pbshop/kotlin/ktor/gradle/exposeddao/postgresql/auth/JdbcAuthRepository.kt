package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import java.sql.ResultSet
import java.sql.Types
import java.time.Instant

class JdbcAuthRepository(
    private val databaseFactory: DatabaseFactory,
) : AuthRepository {
    override fun createUser(newUser: NewAuthUser): AuthUserRecord =
        databaseFactory.withConnection { connection ->
            connection.prepareStatement(
                """
                INSERT INTO users (
                    email,
                    password,
                    name,
                    phone,
                    role,
                    status,
                    email_verified,
                    email_verified_at,
                    nickname,
                    refresh_token
                ) VALUES (
                    ?,
                    ?,
                    ?,
                    ?,
                    CAST(? AS user_role),
                    CAST(? AS user_status),
                    ?,
                    ?,
                    ?,
                    NULL
                )
                RETURNING id, email, password, name, phone, role, status, email_verified, email_verified_at, nickname, refresh_token
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, newUser.email)
                statement.setString(2, newUser.passwordHash)
                statement.setString(3, newUser.name)
                statement.setString(4, newUser.phone)
                statement.setString(5, newUser.role.name)
                statement.setString(6, newUser.status.name)
                statement.setBoolean(7, newUser.emailVerified)
                statement.setNull(8, Types.TIMESTAMP)
                statement.setString(9, newUser.nickname)
                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                    resultSet.toAuthUserRecord()
                }
            }
        }

    override fun findUserByEmail(email: String): AuthUserRecord? =
        databaseFactory.withConnection { connection ->
            connection.prepareStatement(
                """
                SELECT id, email, password, name, phone, role, status, email_verified, email_verified_at, nickname, refresh_token
                FROM users
                WHERE lower(email) = ?
                LIMIT 1
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, email.lowercase())
                statement.executeQuery().use { resultSet -> resultSet.singleUserOrNull() }
            }
        }

    override fun findUserById(id: Int): AuthUserRecord? =
        databaseFactory.withConnection { connection ->
            connection.prepareStatement(
                """
                SELECT id, email, password, name, phone, role, status, email_verified, email_verified_at, nickname, refresh_token
                FROM users
                WHERE id = ?
                LIMIT 1
                """.trimIndent(),
            ).use { statement ->
                statement.setInt(1, id)
                statement.executeQuery().use { resultSet -> resultSet.singleUserOrNull() }
            }
        }

    override fun findUserByRefreshToken(refreshToken: String): AuthUserRecord? =
        databaseFactory.withConnection { connection ->
            connection.prepareStatement(
                """
                SELECT id, email, password, name, phone, role, status, email_verified, email_verified_at, nickname, refresh_token
                FROM users
                WHERE refresh_token = ?
                LIMIT 1
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, refreshToken)
                statement.executeQuery().use { resultSet -> resultSet.singleUserOrNull() }
            }
        }

    override fun saveRefreshToken(
        userId: Int,
        refreshToken: String?,
    ) {
        databaseFactory.withConnection { connection ->
            connection.prepareStatement(
                """
                UPDATE users
                SET refresh_token = ?, updated_at = NOW()
                WHERE id = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, refreshToken)
                statement.setInt(2, userId)
                statement.executeUpdate()
            }
        }
    }

    override fun markEmailVerified(
        userId: Int,
        verifiedAt: Instant,
    ) {
        databaseFactory.withConnection { connection ->
            connection.prepareStatement(
                """
                UPDATE users
                SET email_verified = true,
                    email_verified_at = ?,
                    status = 'ACTIVE',
                    updated_at = NOW()
                WHERE id = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setTimestamp(1, java.sql.Timestamp.from(verifiedAt))
                statement.setInt(2, userId)
                statement.executeUpdate()
            }
        }
    }

    override fun updatePassword(
        userId: Int,
        passwordHash: String,
    ) {
        databaseFactory.withConnection { connection ->
            connection.prepareStatement(
                """
                UPDATE users
                SET password = ?,
                    refresh_token = NULL,
                    updated_at = NOW()
                WHERE id = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, passwordHash)
                statement.setInt(2, userId)
                statement.executeUpdate()
            }
        }
    }

    override fun createVerification(
        userId: Int,
        type: AuthVerificationType,
        code: String,
        expiresAt: Instant,
    ): AuthVerificationRecord =
        databaseFactory.withConnection { connection ->
            connection.prepareStatement(
                """
                INSERT INTO email_verifications (
                    user_id,
                    type,
                    code,
                    attempt_count,
                    is_used,
                    expires_at
                ) VALUES (
                    ?,
                    CAST(? AS verification_type),
                    ?,
                    0,
                    false,
                    ?
                )
                RETURNING id, user_id, type, code, attempt_count, is_used, expires_at, created_at
                """.trimIndent(),
            ).use { statement ->
                statement.setInt(1, userId)
                statement.setString(2, type.name)
                statement.setString(3, code)
                statement.setTimestamp(4, java.sql.Timestamp.from(expiresAt))
                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                    resultSet.toVerificationRecord()
                }
            }
        }

    override fun findLatestActiveVerification(
        userId: Int,
        type: AuthVerificationType,
    ): AuthVerificationRecord? =
        databaseFactory.withConnection { connection ->
            connection.prepareStatement(
                """
                SELECT id, user_id, type, code, attempt_count, is_used, expires_at, created_at
                FROM email_verifications
                WHERE user_id = ?
                  AND type = CAST(? AS verification_type)
                  AND is_used = false
                ORDER BY created_at DESC
                LIMIT 1
                """.trimIndent(),
            ).use { statement ->
                statement.setInt(1, userId)
                statement.setString(2, type.name)
                statement.executeQuery().use { resultSet -> resultSet.singleVerificationOrNull() }
            }
        }

    override fun findVerificationById(id: Int): AuthVerificationRecord? =
        databaseFactory.withConnection { connection ->
            connection.prepareStatement(
                """
                SELECT id, user_id, type, code, attempt_count, is_used, expires_at, created_at
                FROM email_verifications
                WHERE id = ?
                LIMIT 1
                """.trimIndent(),
            ).use { statement ->
                statement.setInt(1, id)
                statement.executeQuery().use { resultSet -> resultSet.singleVerificationOrNull() }
            }
        }

    override fun updateVerificationAttemptCount(
        verificationId: Int,
        attemptCount: Int,
    ) {
        databaseFactory.withConnection { connection ->
            connection.prepareStatement(
                """
                UPDATE email_verifications
                SET attempt_count = ?
                WHERE id = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setInt(1, attemptCount)
                statement.setInt(2, verificationId)
                statement.executeUpdate()
            }
        }
    }

    override fun markVerificationUsed(verificationId: Int) {
        databaseFactory.withConnection { connection ->
            connection.prepareStatement(
                """
                UPDATE email_verifications
                SET is_used = true
                WHERE id = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setInt(1, verificationId)
                statement.executeUpdate()
            }
        }
    }

    private fun ResultSet.singleUserOrNull(): AuthUserRecord? {
        if (!next()) {
            return null
        }
        return toAuthUserRecord()
    }

    private fun ResultSet.singleVerificationOrNull(): AuthVerificationRecord? {
        if (!next()) {
            return null
        }
        return toVerificationRecord()
    }

    private fun ResultSet.toAuthUserRecord(): AuthUserRecord =
        AuthUserRecord(
            id = getInt("id"),
            email = getString("email"),
            passwordHash = getString("password"),
            name = getString("name"),
            phone = getString("phone"),
            role = PbRole.valueOf(getString("role")),
            status = AuthUserStatus.valueOf(getString("status")),
            emailVerified = getBoolean("email_verified"),
            emailVerifiedAt = getTimestamp("email_verified_at")?.toInstant(),
            nickname = getString("nickname"),
            refreshToken = getString("refresh_token"),
        )

    private fun ResultSet.toVerificationRecord(): AuthVerificationRecord =
        AuthVerificationRecord(
            id = getInt("id"),
            userId = getInt("user_id"),
            type = AuthVerificationType.valueOf(getString("type")),
            code = getString("code"),
            attemptCount = getInt("attempt_count"),
            isUsed = getBoolean("is_used"),
            expiresAt = getTimestamp("expires_at").toInstant(),
            createdAt = getTimestamp("created_at").toInstant(),
        )
}
