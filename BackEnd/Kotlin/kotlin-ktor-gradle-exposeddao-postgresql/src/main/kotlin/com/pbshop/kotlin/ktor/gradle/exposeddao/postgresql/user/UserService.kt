package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.user

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth.AuthTokenCodec
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth.AuthUserStatus
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.image.ImageService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.image.ImageUploadInput
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpStatusCode
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant

data class UserPrincipal(
    val role: PbRole?,
    val userIdHeader: Int?,
    val bearerToken: String?,
)

class UserService(
    private val repository: UserRepository,
    private val imageService: ImageService? = null,
) {
    fun currentUser(principal: UserPrincipal): StubResponse {
        val user = resolveCurrentUser(principal)
        return StubResponse(data = userPayload(user))
    }

    fun updateCurrentUser(
        principal: UserPrincipal,
        request: UserUpdateMeRequest,
    ): StubResponse {
        val user = resolveCurrentUser(principal)
        val normalizedName = request.name?.trim()?.takeIf { it.isNotBlank() }
        val normalizedPhone = request.phone?.let(::normalizePhone)
        val passwordHash =
            request.password
                ?.takeIf { it.isNotBlank() }
                ?.also(::validatePassword)
                ?.let { BCrypt.hashpw(it, BCrypt.gensalt()) }

        if (normalizedName != null && normalizedName.length !in 2..20) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "이름은 2자 이상 20자 이하로 입력해주세요.")
        }
        if (normalizedPhone != null && !PHONE_REGEX.matches(normalizedPhone)) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "전화번호 형식이 올바르지 않습니다.")
        }

        return StubResponse(
            data =
                userPayload(
                    repository.updateUser(
                        userId = user.id,
                        name = normalizedName,
                        phone = normalizedPhone,
                        passwordHash = passwordHash,
                    ),
                ),
        )
    }

    fun deleteCurrentUser(principal: UserPrincipal): StubResponse {
        val user = resolveCurrentUser(principal)
        repository.softDeleteUser(user.id)
        return StubResponse(data = mapOf("message" to "회원 탈퇴가 완료되었습니다."))
    }

    fun adminUserList(
        page: Int,
        limit: Int,
        search: String?,
        status: String?,
        role: String?,
    ): StubResponse {
        val normalizedPage = if (page < 1) 1 else page
        val normalizedLimit = limit.coerceIn(1, 100)
        val query =
            UserListQuery(
                page = normalizedPage,
                limit = normalizedLimit,
                search = search?.trim()?.takeIf { it.isNotBlank() },
                status = status?.takeIf { it.isNotBlank() }?.let(::parseStatus),
                role = role?.takeIf { it.isNotBlank() }?.let(::parseRole),
            )
        val result = repository.listUsers(query)

        return StubResponse(
            data = result.items.map(::userPayload),
            meta =
                mapOf(
                    "page" to normalizedPage,
                    "limit" to normalizedLimit,
                    "totalCount" to result.totalCount,
                    "totalPages" to if (result.totalCount == 0) 0 else ((result.totalCount + normalizedLimit - 1) / normalizedLimit),
                ),
        )
    }

    fun updateUserStatus(
        userId: Int,
        request: UserStatusUpdateRequest,
    ): StubResponse {
        val updated = repository.updateUserStatus(userId, parseStatus(request.status))
        return StubResponse(data = userPayload(updated))
    }

    fun profile(userId: Int): StubResponse {
        val user =
            repository.findUserById(userId)
                ?.takeIf { it.deletedAt == null }
                ?: throw PbShopException(HttpStatusCode.NotFound, "USER_NOT_FOUND", "해당 사용자를 찾을 수 없습니다.")
        return StubResponse(data = profilePayload(user))
    }

    fun updateMyProfile(
        principal: UserPrincipal,
        request: UserProfileUpdateRequest,
    ): StubResponse {
        val user = resolveCurrentUser(principal)
        val nickname = request.nickname?.trim()?.takeIf { it.isNotBlank() }
        val bio = request.bio?.trim()

        if (nickname != null && nickname.length !in 2..30) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "닉네임은 2자 이상 30자 이하로 입력해주세요.")
        }
        if (bio != null && bio.length > 200) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "소개글은 200자 이하로 입력해주세요.")
        }

        val updated = repository.updateUserProfile(user.id, nickname, bio)
        return StubResponse(data = profilePayload(updated))
    }

    fun updateProfileImage(
        principal: UserPrincipal,
        imageUrl: String,
    ): StubResponse {
        val user = resolveCurrentUser(principal)
        val updated = repository.updateProfileImage(user.id, imageUrl)
        return StubResponse(
            data =
                mapOf(
                    "imageUrl" to (updated.profileImageUrl ?: imageUrl),
                ),
        )
    }

    fun deleteProfileImage(principal: UserPrincipal): StubResponse {
        val user = resolveCurrentUser(principal)
        repository.updateProfileImage(user.id, null)
        return StubResponse(data = mapOf("message" to "프로필 이미지가 기본 이미지로 초기화되었습니다."))
    }

    fun updateProfileImageFromUpload(
        principal: UserPrincipal,
        upload: UserImageUploadRequest,
    ): StubResponse {
        val user = resolveCurrentUser(principal)
        val uploaded =
            imageService?.storeImage(
                ImageUploadInput(
                    uploadedByUserId = user.id,
                    originalFilename = upload.fileName,
                    mimeType = upload.mimeType,
                    size = upload.size,
                    category = "profile",
                ),
            )
                ?: throw PbShopException(HttpStatusCode.InternalServerError, "INTERNAL_ERROR", "이미지 서비스가 준비되지 않았습니다.")
        val updated = repository.updateProfileImage(user.id, imageService.preferredUrl(uploaded))
        return StubResponse(data = mapOf("imageUrl" to updated.profileImageUrl))
    }

    private fun resolveCurrentUser(principal: UserPrincipal): UserRecord {
        val userIdFromToken = principal.bearerToken?.let(AuthTokenCodec::decodeAccessToken)?.userId
        val resolvedUser =
            when {
                userIdFromToken != null -> repository.findUserById(userIdFromToken)
                principal.userIdHeader != null -> repository.findUserById(principal.userIdHeader)
                principal.role != null -> repository.findFirstUserByRole(principal.role)
                else -> null
            }

        return resolvedUser
            ?.takeIf { it.deletedAt == null }
            ?: throw PbShopException(HttpStatusCode.Unauthorized, "AUTH_REQUIRED", "현재 사용자 정보를 확인할 수 없습니다.")
    }

    private fun userPayload(user: UserRecord): Map<String, Any?> =
        mapOf(
            "id" to user.id,
            "email" to user.email,
            "name" to user.name,
            "phone" to formatPhone(user.phone),
            "role" to user.role.name,
            "status" to user.status.name,
            "point" to user.point,
            "nickname" to user.nickname,
            "bio" to user.bio,
            "profileImageUrl" to user.profileImageUrl,
            "badges" to repository.findBadgesByUserId(user.id).map(::badgePayload),
            "createdAt" to user.createdAt.toString(),
        )

    private fun profilePayload(user: UserRecord): Map<String, Any?> =
        mapOf(
            "id" to user.id,
            "nickname" to user.nickname,
            "bio" to user.bio,
            "profileImageUrl" to user.profileImageUrl,
        )

    private fun badgePayload(badge: UserBadgeRecord): Map<String, Any?> =
        mapOf(
            "id" to badge.id,
            "name" to badge.name,
            "iconUrl" to badge.iconUrl,
        )

    private fun parseStatus(value: String): AuthUserStatus =
        runCatching { AuthUserStatus.valueOf(value.trim().uppercase()) }
            .getOrElse {
                throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "유효하지 않은 사용자 상태입니다.")
            }

    private fun parseRole(value: String): PbRole =
        PbRole.fromHeader(value)
            ?: throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "유효하지 않은 사용자 역할입니다.")

    private fun validatePassword(password: String) {
        if (!PASSWORD_REGEX.matches(password)) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "비밀번호는 8자 이상이며 영문, 숫자, 특수문자를 포함해야 합니다.")
        }
    }

    private fun normalizePhone(phone: String): String = phone.filter(Char::isDigit)

    private fun formatPhone(phone: String): String =
        when (phone.length) {
            11 -> "${phone.substring(0, 3)}-${phone.substring(3, 7)}-${phone.substring(7)}"
            10 -> "${phone.substring(0, 3)}-${phone.substring(3, 6)}-${phone.substring(6)}"
            else -> phone
        }

    companion object {
        private val PHONE_REGEX = Regex("^\\d{10,11}$")
        private val PASSWORD_REGEX = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$")
    }
}
