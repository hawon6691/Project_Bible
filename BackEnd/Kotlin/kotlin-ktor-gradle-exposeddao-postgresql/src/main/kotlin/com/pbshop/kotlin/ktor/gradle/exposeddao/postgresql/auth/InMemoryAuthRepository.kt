package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant

class InMemoryAuthRepository(
    seededUsers: List<AuthUserRecord> = emptyList(),
    seededVerifications: List<AuthVerificationRecord> = emptyList(),
) : AuthRepository {
    private val users = linkedMapOf<Int, AuthUserRecord>()
    private val verifications = linkedMapOf<Int, AuthVerificationRecord>()
    private var nextUserId: Int = 1
    private var nextVerificationId: Int = 1

    init {
        seededUsers.forEach { user ->
            users[user.id] = user
            nextUserId = maxOf(nextUserId, user.id + 1)
        }
        seededVerifications.forEach { verification ->
            verifications[verification.id] = verification
            nextVerificationId = maxOf(nextVerificationId, verification.id + 1)
        }
    }

    override fun createUser(newUser: NewAuthUser): AuthUserRecord {
        val user =
            AuthUserRecord(
                id = nextUserId++,
                email = newUser.email,
                passwordHash = newUser.passwordHash,
                name = newUser.name,
                phone = newUser.phone,
                role = newUser.role,
                status = newUser.status,
                emailVerified = newUser.emailVerified,
                emailVerifiedAt = if (newUser.emailVerified) Instant.now() else null,
                nickname = newUser.nickname,
                refreshToken = null,
            )
        users[user.id] = user
        return user
    }

    override fun findUserByEmail(email: String): AuthUserRecord? = users.values.firstOrNull { it.email.equals(email, ignoreCase = true) }

    override fun findUserById(id: Int): AuthUserRecord? = users[id]

    override fun findUserByRefreshToken(refreshToken: String): AuthUserRecord? = users.values.firstOrNull { it.refreshToken == refreshToken }

    override fun saveRefreshToken(
        userId: Int,
        refreshToken: String?,
    ) {
        val current = users[userId] ?: return
        users[userId] = current.copy(refreshToken = refreshToken)
    }

    override fun markEmailVerified(
        userId: Int,
        verifiedAt: Instant,
    ) {
        val current = users[userId] ?: return
        users[userId] =
            current.copy(
                emailVerified = true,
                emailVerifiedAt = verifiedAt,
                status = AuthUserStatus.ACTIVE,
            )
    }

    override fun updatePassword(
        userId: Int,
        passwordHash: String,
    ) {
        val current = users[userId] ?: return
        users[userId] =
            current.copy(
                passwordHash = passwordHash,
                refreshToken = null,
            )
    }

    override fun createVerification(
        userId: Int,
        type: AuthVerificationType,
        code: String,
        expiresAt: Instant,
    ): AuthVerificationRecord {
        val verification =
            AuthVerificationRecord(
                id = nextVerificationId++,
                userId = userId,
                type = type,
                code = code,
                attemptCount = 0,
                isUsed = false,
                expiresAt = expiresAt,
                createdAt = Instant.now(),
            )
        verifications[verification.id] = verification
        return verification
    }

    override fun findLatestActiveVerification(
        userId: Int,
        type: AuthVerificationType,
    ): AuthVerificationRecord? =
        verifications.values
            .filter { it.userId == userId && it.type == type && !it.isUsed }
            .maxByOrNull { it.createdAt }

    override fun findVerificationById(id: Int): AuthVerificationRecord? = verifications[id]

    override fun updateVerificationAttemptCount(
        verificationId: Int,
        attemptCount: Int,
    ) {
        val current = verifications[verificationId] ?: return
        verifications[verificationId] = current.copy(attemptCount = attemptCount)
    }

    override fun markVerificationUsed(verificationId: Int) {
        val current = verifications[verificationId] ?: return
        verifications[verificationId] = current.copy(isUsed = true)
    }

    fun latestVerificationCode(
        email: String,
        type: AuthVerificationType,
    ): String? =
        findUserByEmail(email)
            ?.let { user -> findLatestActiveVerification(user.id, type) }
            ?.code

    companion object {
        fun seeded(): InMemoryAuthRepository {
            val samplePasswordHash = BCrypt.hashpw("Password1!", BCrypt.gensalt())
            val users =
                listOf(
                    AuthUserRecord(
                        id = 1,
                        email = "admin@nestshop.com",
                        passwordHash = samplePasswordHash,
                        name = "관리자",
                        phone = "01012340001",
                        role = PbRole.ADMIN,
                        status = AuthUserStatus.ACTIVE,
                        emailVerified = true,
                        emailVerifiedAt = Instant.now(),
                        nickname = "admin01",
                        refreshToken = null,
                    ),
                    AuthUserRecord(
                        id = 2,
                        email = "seller1@nestshop.com",
                        passwordHash = samplePasswordHash,
                        name = "셀러원",
                        phone = "01012340002",
                        role = PbRole.SELLER,
                        status = AuthUserStatus.ACTIVE,
                        emailVerified = true,
                        emailVerifiedAt = Instant.now(),
                        nickname = "seller01",
                        refreshToken = null,
                    ),
                    AuthUserRecord(
                        id = 4,
                        email = "user1@nestshop.com",
                        passwordHash = samplePasswordHash,
                        name = "홍길동",
                        phone = "01012345678",
                        role = PbRole.USER,
                        status = AuthUserStatus.ACTIVE,
                        emailVerified = true,
                        emailVerifiedAt = Instant.now(),
                        nickname = "hong01",
                        refreshToken = null,
                    ),
                    AuthUserRecord(
                        id = 6,
                        email = "user3@nestshop.com",
                        passwordHash = samplePasswordHash,
                        name = "이철수",
                        phone = "01034567890",
                        role = PbRole.USER,
                        status = AuthUserStatus.INACTIVE,
                        emailVerified = false,
                        emailVerifiedAt = null,
                        nickname = "lee03",
                        refreshToken = null,
                    ),
                )
            val verifications =
                listOf(
                    AuthVerificationRecord(
                        id = 1,
                        userId = 6,
                        type = AuthVerificationType.SIGNUP,
                        code = "123456",
                        attemptCount = 0,
                        isUsed = false,
                        expiresAt = Instant.now().plusSeconds(600),
                        createdAt = Instant.now().minusSeconds(120),
                    ),
                )
            return InMemoryAuthRepository(users, verifications)
        }
    }
}
