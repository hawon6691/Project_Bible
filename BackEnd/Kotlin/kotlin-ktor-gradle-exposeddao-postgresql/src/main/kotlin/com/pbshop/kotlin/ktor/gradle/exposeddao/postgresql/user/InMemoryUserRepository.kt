package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.user

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth.AuthUserStatus
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant

class InMemoryUserRepository(
    seededUsers: List<UserRecord> = emptyList(),
    seededBadges: Map<Int, List<UserBadgeRecord>> = emptyMap(),
) : UserRepository {
    private val users = linkedMapOf<Int, UserRecord>()
    private val badgesByUser = seededBadges.mapValues { (_, value) -> value.toMutableList() }.toMutableMap()

    init {
        seededUsers.forEach { users[it.id] = it }
    }

    override fun findUserById(id: Int): UserRecord? = users[id]

    override fun findFirstUserByRole(role: PbRole): UserRecord? =
        users.values.firstOrNull { it.role == role && it.deletedAt == null }

    override fun listUsers(query: UserListQuery): UserListResult {
        val filtered =
            users.values
                .asSequence()
                .filter { it.deletedAt == null }
                .filter { query.search.isNullOrBlank() || it.email.contains(query.search, ignoreCase = true) || it.name.contains(query.search, ignoreCase = true) }
                .filter { query.status == null || it.status == query.status }
                .filter { query.role == null || it.role == query.role }
                .sortedBy { it.id }
                .toList()

        val offset = (query.page - 1) * query.limit
        return UserListResult(
            items = filtered.drop(offset).take(query.limit),
            totalCount = filtered.size,
        )
    }

    override fun findBadgesByUserId(userId: Int): List<UserBadgeRecord> = badgesByUser[userId]?.toList().orEmpty()

    override fun updateUser(
        userId: Int,
        name: String?,
        phone: String?,
        passwordHash: String?,
    ): UserRecord {
        val current = requireUser(userId)
        val updated =
            current.copy(
                name = name ?: current.name,
                phone = phone ?: current.phone,
                passwordHash = passwordHash ?: current.passwordHash,
            )
        users[userId] = updated
        return updated
    }

    override fun updateUserStatus(
        userId: Int,
        status: AuthUserStatus,
    ): UserRecord {
        val current = requireUser(userId)
        val updated = current.copy(status = status)
        users[userId] = updated
        return updated
    }

    override fun updateUserProfile(
        userId: Int,
        nickname: String?,
        bio: String?,
    ): UserRecord {
        val current = requireUser(userId)
        val updated =
            current.copy(
                nickname = nickname ?: current.nickname,
                bio = bio ?: current.bio,
            )
        users[userId] = updated
        return updated
    }

    override fun updateProfileImage(
        userId: Int,
        profileImageUrl: String?,
    ): UserRecord {
        val current = requireUser(userId)
        val updated = current.copy(profileImageUrl = profileImageUrl)
        users[userId] = updated
        return updated
    }

    override fun softDeleteUser(userId: Int) {
        val current = requireUser(userId)
        users[userId] =
            current.copy(
                status = AuthUserStatus.INACTIVE,
                deletedAt = Instant.now(),
            )
    }

    private fun requireUser(userId: Int): UserRecord =
        users[userId] ?: error("User $userId not found in in-memory repository.")

    companion object {
        fun seeded(): InMemoryUserRepository {
            val passwordHash = BCrypt.hashpw("Password1!", BCrypt.gensalt())
            val now = Instant.now()
            val users =
                listOf(
                    UserRecord(
                        id = 1,
                        email = "admin@nestshop.com",
                        passwordHash = passwordHash,
                        name = "관리자",
                        phone = "01012340001",
                        role = PbRole.ADMIN,
                        status = AuthUserStatus.ACTIVE,
                        point = 100000,
                        nickname = "admin01",
                        bio = "시스템 관리자",
                        profileImageUrl = null,
                        emailVerified = true,
                        createdAt = now.minusSeconds(864000),
                        deletedAt = null,
                    ),
                    UserRecord(
                        id = 2,
                        email = "seller1@nestshop.com",
                        passwordHash = passwordHash,
                        name = "셀러원",
                        phone = "01012340002",
                        role = PbRole.SELLER,
                        status = AuthUserStatus.ACTIVE,
                        point = 12000,
                        nickname = "seller01",
                        bio = "공식 판매자",
                        profileImageUrl = null,
                        emailVerified = true,
                        createdAt = now.minusSeconds(864000),
                        deletedAt = null,
                    ),
                    UserRecord(
                        id = 4,
                        email = "user1@nestshop.com",
                        passwordHash = passwordHash,
                        name = "홍길동",
                        phone = "01012345678",
                        role = PbRole.USER,
                        status = AuthUserStatus.ACTIVE,
                        point = 53000,
                        nickname = "hong01",
                        bio = "게이밍 유저",
                        profileImageUrl = null,
                        emailVerified = true,
                        createdAt = now.minusSeconds(864000),
                        deletedAt = null,
                    ),
                    UserRecord(
                        id = 5,
                        email = "user2@nestshop.com",
                        passwordHash = passwordHash,
                        name = "김영희",
                        phone = "01023456789",
                        role = PbRole.USER,
                        status = AuthUserStatus.ACTIVE,
                        point = 27000,
                        nickname = "kim02",
                        bio = "사무용 유저",
                        profileImageUrl = null,
                        emailVerified = true,
                        createdAt = now.minusSeconds(800000),
                        deletedAt = null,
                    ),
                    UserRecord(
                        id = 6,
                        email = "user3@nestshop.com",
                        passwordHash = passwordHash,
                        name = "이철수",
                        phone = "01034567890",
                        role = PbRole.USER,
                        status = AuthUserStatus.INACTIVE,
                        point = 0,
                        nickname = "lee03",
                        bio = "신규 가입자",
                        profileImageUrl = null,
                        emailVerified = false,
                        createdAt = now.minusSeconds(700000),
                        deletedAt = null,
                    ),
                )

            val badges =
                mapOf(
                    4 to
                        listOf(
                            UserBadgeRecord(1, "첫 구매", "https://img.example.com/badge-first.png"),
                            UserBadgeRecord(3, "운영자 픽", "https://img.example.com/badge-admin.png"),
                        ),
                    5 to
                        listOf(
                            UserBadgeRecord(1, "첫 구매", "https://img.example.com/badge-first.png"),
                        ),
                )

            return InMemoryUserRepository(users, badges)
        }
    }
}
