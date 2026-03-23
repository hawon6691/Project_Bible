package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.EmailVerificationEntity
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.EmailVerificationsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UserEntity
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UsersTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import java.time.Instant

class ExposedDaoAuthRepository(
    private val databaseFactory: DatabaseFactory,
) : AuthRepository {
    override fun createUser(newUser: NewAuthUser): AuthUserRecord =
        databaseFactory.withTransaction {
            UserEntity.new {
                email = newUser.email
                password = newUser.passwordHash
                name = newUser.name
                phone = newUser.phone
                role = newUser.role
                status = newUser.status
                emailVerified = newUser.emailVerified
                emailVerifiedAt = null
                nickname = newUser.nickname
                bio = null
                profileImageUrl = null
                point = 0
                refreshToken = null
                createdAt = Instant.now()
                updatedAt = Instant.now()
                deletedAt = null
            }.toAuthUserRecord()
        }

    override fun findUserByEmail(email: String): AuthUserRecord? =
        databaseFactory.withTransaction {
            UserEntity.find { UsersTable.email eq email.lowercase() }
                .limit(1)
                .firstOrNull()
                ?.toAuthUserRecord()
        }

    override fun findUserById(id: Int): AuthUserRecord? =
        databaseFactory.withTransaction {
            UserEntity.findById(id)?.toAuthUserRecord()
        }

    override fun findUserByRefreshToken(refreshToken: String): AuthUserRecord? =
        databaseFactory.withTransaction {
            UserEntity.find { UsersTable.refreshToken eq refreshToken }
                .limit(1)
                .firstOrNull()
                ?.toAuthUserRecord()
        }

    override fun saveRefreshToken(
        userId: Int,
        refreshToken: String?,
    ) {
        databaseFactory.withTransaction {
            UserEntity.findById(userId)?.apply {
                this.refreshToken = refreshToken
                updatedAt = Instant.now()
            }
        }
    }

    override fun markEmailVerified(
        userId: Int,
        verifiedAt: Instant,
    ) {
        databaseFactory.withTransaction {
            UserEntity.findById(userId)?.apply {
                emailVerified = true
                emailVerifiedAt = verifiedAt
                status = AuthUserStatus.ACTIVE
                updatedAt = Instant.now()
            }
        }
    }

    override fun updatePassword(
        userId: Int,
        passwordHash: String,
    ) {
        databaseFactory.withTransaction {
            UserEntity.findById(userId)?.apply {
                password = passwordHash
                refreshToken = null
                updatedAt = Instant.now()
            }
        }
    }

    override fun createVerification(
        userId: Int,
        type: AuthVerificationType,
        code: String,
        expiresAt: Instant,
    ): AuthVerificationRecord =
        databaseFactory.withTransaction {
            val user = requireNotNull(UserEntity.findById(userId)) { "User $userId not found" }
            EmailVerificationEntity.new {
                this.userId = user.id
                this.type = type
                this.code = code
                attemptCount = 0
                isUsed = false
                this.expiresAt = expiresAt
                createdAt = Instant.now()
            }.toAuthVerificationRecord()
        }

    override fun findLatestActiveVerification(
        userId: Int,
        type: AuthVerificationType,
    ): AuthVerificationRecord? =
        databaseFactory.withTransaction {
            EmailVerificationEntity.find {
                (EmailVerificationsTable.user eq userId) and
                    (EmailVerificationsTable.type eq type) and
                    (EmailVerificationsTable.isUsed eq false)
            }.orderBy(EmailVerificationsTable.createdAt to SortOrder.DESC)
                .limit(1)
                .firstOrNull()
                ?.toAuthVerificationRecord()
        }

    override fun findVerificationById(id: Int): AuthVerificationRecord? =
        databaseFactory.withTransaction {
            EmailVerificationEntity.findById(id)?.toAuthVerificationRecord()
        }

    override fun updateVerificationAttemptCount(
        verificationId: Int,
        attemptCount: Int,
    ) {
        databaseFactory.withTransaction {
            EmailVerificationEntity.findById(verificationId)?.attemptCount = attemptCount
        }
    }

    override fun markVerificationUsed(verificationId: Int) {
        databaseFactory.withTransaction {
            EmailVerificationEntity.findById(verificationId)?.isUsed = true
        }
    }

    private fun UserEntity.toAuthUserRecord(): AuthUserRecord =
        AuthUserRecord(
            id = id.value,
            email = email,
            passwordHash = password,
            name = name,
            phone = phone,
            role = role,
            status = status,
            emailVerified = emailVerified,
            emailVerifiedAt = emailVerifiedAt,
            nickname = nickname,
            refreshToken = refreshToken,
        )

    private fun EmailVerificationEntity.toAuthVerificationRecord(): AuthVerificationRecord =
        AuthVerificationRecord(
            id = id.value,
            userId = userId.value,
            type = type,
            code = code,
            attemptCount = attemptCount,
            isUsed = isUsed,
            expiresAt = expiresAt,
            createdAt = createdAt,
        )
}
