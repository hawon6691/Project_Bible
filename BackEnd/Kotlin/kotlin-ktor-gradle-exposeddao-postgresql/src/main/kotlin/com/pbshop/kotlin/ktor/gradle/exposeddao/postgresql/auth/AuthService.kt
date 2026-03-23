package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpStatusCode
import org.mindrot.jbcrypt.BCrypt
import java.time.Duration
import java.time.Instant
import kotlin.random.Random

class AuthService(
    private val repository: AuthRepository,
) {
    fun signup(request: AuthSignupRequest): StubResponse {
        val email = normalizeEmail(request.email)
        val phone = normalizePhone(request.phone)
        validateSignupRequest(email, request.password, request.name, phone)

        if (repository.findUserByEmail(email) != null) {
            throw PbShopException(HttpStatusCode.Conflict, "DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다.")
        }

        val user =
            repository.createUser(
                NewAuthUser(
                    email = email,
                    passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt()),
                    name = request.name.trim(),
                    phone = phone,
                    nickname = createSignupNickname(email),
                ),
            )

        createVerification(user.id, AuthVerificationType.SIGNUP, SIGNUP_CODE_TTL)

        return StubResponse(
            status = HttpStatusCode.Created,
            data =
                mapOf(
                    "id" to user.id,
                    "email" to user.email,
                    "name" to user.name,
                    "message" to "인증 메일이 발송되었습니다. 이메일을 확인해주세요.",
                ),
        )
    }

    fun verifyEmail(request: AuthVerifyEmailRequest): StubResponse {
        val user = findUserByEmailOrThrow(request.email)
        if (user.emailVerified) {
            throw PbShopException(HttpStatusCode.Conflict, "ALREADY_VERIFIED", "이미 인증된 이메일입니다.")
        }

        validateVerification(
            user = user,
            type = AuthVerificationType.SIGNUP,
            code = request.code,
        )

        repository.markEmailVerified(user.id, Instant.now())

        return StubResponse(
            data =
                mapOf(
                    "message" to "이메일 인증이 완료되었습니다.",
                    "verified" to true,
                ),
        )
    }

    fun resendVerification(request: AuthEmailOnlyRequest): StubResponse {
        val user = findUserByEmailOrThrow(request.email)
        if (user.emailVerified) {
            throw PbShopException(HttpStatusCode.Conflict, "ALREADY_VERIFIED", "이미 인증된 이메일입니다.")
        }

        ensureResendWindow(user.id, AuthVerificationType.SIGNUP)
        createVerification(user.id, AuthVerificationType.SIGNUP, SIGNUP_CODE_TTL)

        return StubResponse(
            data = mapOf("message" to "인증 메일이 재발송되었습니다."),
        )
    }

    fun login(request: AuthLoginRequest): StubResponse {
        val email = normalizeEmail(request.email)
        val user = repository.findUserByEmail(email)
            ?: throw PbShopException(HttpStatusCode.Unauthorized, "INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다.")

        if (!BCrypt.checkpw(request.password, user.passwordHash)) {
            throw PbShopException(HttpStatusCode.Unauthorized, "INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다.")
        }
        if (user.status == AuthUserStatus.BLOCKED) {
            throw PbShopException(HttpStatusCode.Forbidden, "USER_BLOCKED", "차단된 계정입니다.")
        }
        if (!user.emailVerified) {
            throw PbShopException(HttpStatusCode.Forbidden, "EMAIL_NOT_VERIFIED", "이메일 인증이 완료되지 않았습니다. 인증 메일을 확인해주세요.")
        }
        if (user.status != AuthUserStatus.ACTIVE) {
            throw PbShopException(HttpStatusCode.Forbidden, "USER_INACTIVE", "현재 사용할 수 없는 계정입니다.")
        }

        return StubResponse(data = issueTokens(user))
    }

    fun logout(accessToken: String?): StubResponse {
        val claims = accessToken?.let(AuthTokenCodec::decodeAccessToken)
        if (claims != null) {
            repository.saveRefreshToken(claims.userId, null)
        }

        return StubResponse(data = mapOf("message" to "로그아웃되었습니다."))
    }

    fun refresh(request: AuthRefreshRequest): StubResponse {
        val refreshToken = request.refreshToken.trim()
        if (refreshToken.isBlank()) {
            throw PbShopException(HttpStatusCode.Unauthorized, "INVALID_REFRESH_TOKEN", "유효하지 않은 refresh token 입니다.")
        }

        val user = repository.findUserByRefreshToken(refreshToken)
            ?: throw PbShopException(HttpStatusCode.Unauthorized, "INVALID_REFRESH_TOKEN", "유효하지 않은 refresh token 입니다.")

        return StubResponse(data = issueTokens(user))
    }

    fun passwordResetRequest(request: AuthPasswordResetRequest): StubResponse {
        val user = findUserByEmailOrThrow(request.email)
        val phone = normalizePhone(request.phone)

        if (normalizePhone(user.phone) != phone) {
            throw PbShopException(HttpStatusCode.BadRequest, "PHONE_MISMATCH", "등록된 전화번호와 일치하지 않습니다.")
        }

        ensureResendWindow(user.id, AuthVerificationType.PASSWORD_RESET)
        createVerification(user.id, AuthVerificationType.PASSWORD_RESET, RESET_CODE_TTL)

        return StubResponse(
            data = mapOf("message" to "비밀번호 재설정 인증 메일이 발송되었습니다."),
        )
    }

    fun passwordResetVerify(request: AuthPasswordResetVerifyRequest): StubResponse {
        val user = findUserByEmailOrThrow(request.email)
        val verification =
            validateVerification(
                user = user,
                type = AuthVerificationType.PASSWORD_RESET,
                code = request.code,
            )

        val expiresAt = Instant.now().plus(RESET_TOKEN_TTL)
        val resetToken =
            AuthTokenCodec.createResetToken(
                userId = user.id,
                email = user.email,
                verificationId = verification.id,
                code = verification.code,
                expiresAt = expiresAt,
            )

        return StubResponse(data = mapOf("resetToken" to resetToken))
    }

    fun passwordResetConfirm(request: AuthPasswordResetConfirmRequest): StubResponse {
        validatePassword(request.newPassword)

        val claims =
            AuthTokenCodec.decodeResetToken(request.resetToken.trim())
                ?: throw PbShopException(HttpStatusCode.Unauthorized, "INVALID_RESET_TOKEN", "유효하지 않거나 만료된 재설정 토큰입니다.")

        val user = repository.findUserById(claims.userId)
            ?: throw PbShopException(HttpStatusCode.Unauthorized, "INVALID_RESET_TOKEN", "유효하지 않거나 만료된 재설정 토큰입니다.")

        val verification = repository.findVerificationById(claims.verificationId)
            ?: throw PbShopException(HttpStatusCode.Unauthorized, "INVALID_RESET_TOKEN", "유효하지 않거나 만료된 재설정 토큰입니다.")

        if (verification.isUsed || verification.code != claims.code || verification.expiresAt.isBefore(Instant.now())) {
            throw PbShopException(HttpStatusCode.Unauthorized, "INVALID_RESET_TOKEN", "유효하지 않거나 만료된 재설정 토큰입니다.")
        }
        if (BCrypt.checkpw(request.newPassword, user.passwordHash)) {
            throw PbShopException(HttpStatusCode.BadRequest, "SAME_PASSWORD", "기존 비밀번호와 동일한 비밀번호입니다.")
        }

        repository.updatePassword(user.id, BCrypt.hashpw(request.newPassword, BCrypt.gensalt()))
        repository.markVerificationUsed(verification.id)

        return StubResponse(data = mapOf("message" to "비밀번호가 성공적으로 변경되었습니다."))
    }

    fun socialLoginRedirect(provider: String): StubResponse {
        val normalizedProvider = validateProvider(provider)
        return StubResponse(
            status = HttpStatusCode.Found,
            data =
                mapOf(
                    "provider" to normalizedProvider,
                    "redirectUrl" to "https://auth.pbshop.dev/oauth/$normalizedProvider/start",
                ),
            meta = mapOf("redirect" to true),
        )
    }

    fun socialCallback(
        provider: String,
        code: String?,
        state: String?,
    ): StubResponse {
        val normalizedProvider = validateProvider(provider)
        if (state.isNullOrBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "INVALID_STATE", "유효하지 않은 state 토큰입니다.")
        }
        if (code.isNullOrBlank()) {
            throw PbShopException(HttpStatusCode.Unauthorized, "SOCIAL_AUTH_FAILED", "소셜 인증에 실패했습니다.")
        }

        val accessToken =
            AuthTokenCodec.createAccessToken(
                userId = 0,
                email = "$normalizedProvider@social.pbshop.dev",
                role = PbRole.USER,
                expiresAt = Instant.now().plus(ACCESS_TOKEN_TTL),
            )
        val refreshToken =
            AuthTokenCodec.createRefreshToken(
                userId = 0,
                email = "$normalizedProvider@social.pbshop.dev",
                role = PbRole.USER,
                expiresAt = Instant.now().plus(REFRESH_TOKEN_TTL),
            )

        return StubResponse(
            data =
                mapOf(
                    "accessToken" to accessToken,
                    "refreshToken" to refreshToken,
                    "expiresIn" to ACCESS_TOKEN_TTL.seconds,
                    "isNewUser" to false,
                ),
        )
    }

    fun socialComplete(request: AuthSocialCompleteRequest): StubResponse {
        if (normalizePhone(request.phone).length < 10) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "전화번호 형식이 올바르지 않습니다.")
        }
        if (request.nickname.trim().length !in 2..30) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "닉네임 형식이 올바르지 않습니다.")
        }

        val accessToken =
            AuthTokenCodec.createAccessToken(
                userId = 0,
                email = "social-new@pbshop.dev",
                role = PbRole.USER,
                expiresAt = Instant.now().plus(ACCESS_TOKEN_TTL),
            )
        val refreshToken =
            AuthTokenCodec.createRefreshToken(
                userId = 0,
                email = "social-new@pbshop.dev",
                role = PbRole.USER,
                expiresAt = Instant.now().plus(REFRESH_TOKEN_TTL),
            )

        return StubResponse(
            data =
                mapOf(
                    "accessToken" to accessToken,
                    "refreshToken" to refreshToken,
                    "expiresIn" to ACCESS_TOKEN_TTL.seconds,
                ),
        )
    }

    fun socialLink(request: AuthSocialLinkRequest): StubResponse {
        val provider = validateProvider(request.provider)
        if (request.socialToken.isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "소셜 토큰이 필요합니다.")
        }

        return StubResponse(
            data =
                mapOf(
                    "message" to "${provider.uppercase()} 계정이 연동되었습니다.",
                    "linkedProvider" to provider,
                ),
        )
    }

    fun socialUnlink(provider: String): StubResponse {
        validateProvider(provider)
        return StubResponse(data = mapOf("message" to "소셜 계정 연동이 해제되었습니다."))
    }

    private fun findUserByEmailOrThrow(email: String): AuthUserRecord =
        repository.findUserByEmail(normalizeEmail(email))
            ?: throw PbShopException(HttpStatusCode.NotFound, "USER_NOT_FOUND", "해당 이메일로 등록된 계정이 없습니다.")

    private fun validateSignupRequest(
        email: String,
        password: String,
        name: String,
        phone: String,
    ) {
        if (!EMAIL_REGEX.matches(email)) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "이메일 형식이 올바르지 않습니다.")
        }
        validatePassword(password)
        if (name.trim().length !in 2..20) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "이름은 2자 이상 20자 이하로 입력해주세요.")
        }
        if (!PHONE_REGEX.matches(phone)) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "전화번호 형식이 올바르지 않습니다.")
        }
    }

    private fun validatePassword(password: String) {
        if (!PASSWORD_REGEX.matches(password)) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "비밀번호는 8자 이상이며 영문, 숫자, 특수문자를 포함해야 합니다.")
        }
    }

    private fun issueTokens(user: AuthUserRecord): Map<String, Any> {
        val accessExpiresAt = Instant.now().plus(ACCESS_TOKEN_TTL)
        val refreshExpiresAt = Instant.now().plus(REFRESH_TOKEN_TTL)
        val accessToken =
            AuthTokenCodec.createAccessToken(
                userId = user.id,
                email = user.email,
                role = user.role,
                expiresAt = accessExpiresAt,
            )
        val refreshToken =
            AuthTokenCodec.createRefreshToken(
                userId = user.id,
                email = user.email,
                role = user.role,
                expiresAt = refreshExpiresAt,
            )
        repository.saveRefreshToken(user.id, refreshToken)

        return mapOf(
            "accessToken" to accessToken,
            "refreshToken" to refreshToken,
            "expiresIn" to ACCESS_TOKEN_TTL.seconds,
        )
    }

    private fun createVerification(
        userId: Int,
        type: AuthVerificationType,
        ttl: Duration,
    ): AuthVerificationRecord =
        repository.createVerification(
            userId = userId,
            type = type,
            code = generateVerificationCode(),
            expiresAt = Instant.now().plus(ttl),
        )

    private fun validateVerification(
        user: AuthUserRecord,
        type: AuthVerificationType,
        code: String,
    ): AuthVerificationRecord {
        val verification =
            repository.findLatestActiveVerification(user.id, type)
                ?: throw PbShopException(HttpStatusCode.BadRequest, "INVALID_VERIFICATION_CODE", "인증코드가 올바르지 않습니다.")

        if (verification.expiresAt.isBefore(Instant.now())) {
            throw PbShopException(HttpStatusCode.Gone, "VERIFICATION_CODE_EXPIRED", "인증코드가 만료되었습니다. 재발송해주세요.")
        }
        if (verification.attemptCount >= MAX_VERIFICATION_ATTEMPTS) {
            throw PbShopException(HttpStatusCode.TooManyRequests, "VERIFICATION_ATTEMPT_EXCEEDED", "인증 시도 횟수를 초과했습니다.")
        }
        if (verification.code != code.trim()) {
            val nextAttemptCount = verification.attemptCount + 1
            repository.updateVerificationAttemptCount(verification.id, nextAttemptCount)
            if (nextAttemptCount >= MAX_VERIFICATION_ATTEMPTS) {
                throw PbShopException(HttpStatusCode.TooManyRequests, "VERIFICATION_ATTEMPT_EXCEEDED", "인증 시도 횟수를 초과했습니다.")
            }
            throw PbShopException(HttpStatusCode.BadRequest, "INVALID_VERIFICATION_CODE", "인증코드가 올바르지 않습니다.")
        }

        repository.markVerificationUsed(verification.id)
        return verification
    }

    private fun ensureResendWindow(
        userId: Int,
        type: AuthVerificationType,
    ) {
        val latest = repository.findLatestActiveVerification(userId, type) ?: return
        if (latest.createdAt.plusSeconds(RESEND_WINDOW_SECONDS).isAfter(Instant.now())) {
            throw PbShopException(HttpStatusCode.TooManyRequests, "RESEND_RATE_LIMITED", "1분 후 다시 시도해주세요.")
        }
    }

    private fun validateProvider(provider: String): String {
        val normalized = provider.trim().lowercase()
        if (normalized !in SUPPORTED_PROVIDERS) {
            throw PbShopException(HttpStatusCode.BadRequest, "INVALID_PROVIDER", "지원하지 않는 소셜 로그인 공급자입니다.")
        }
        return normalized
    }

    private fun normalizeEmail(email: String): String = email.trim().lowercase()

    private fun normalizePhone(phone: String): String = phone.filter(Char::isDigit)

    private fun generateVerificationCode(): String = Random.nextInt(100_000, 1_000_000).toString()

    private fun createSignupNickname(email: String): String {
        val localPart = email.substringBefore('@').filter { it.isLetterOrDigit() }.take(12).ifBlank { "pbuser" }
        val suffix = Random.nextInt(1000, 9999)
        return "$localPart$suffix".take(30)
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        private val PHONE_REGEX = Regex("^\\d{10,11}$")
        private val PASSWORD_REGEX = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$")
        private val SUPPORTED_PROVIDERS = setOf("google", "naver", "kakao", "facebook", "instagram")
        private const val MAX_VERIFICATION_ATTEMPTS = 5
        private const val RESEND_WINDOW_SECONDS = 60L
        private val SIGNUP_CODE_TTL = Duration.ofMinutes(10)
        private val RESET_CODE_TTL = Duration.ofMinutes(5)
        private val RESET_TOKEN_TTL = Duration.ofMinutes(5)
        private val ACCESS_TOKEN_TTL = Duration.ofMinutes(30)
        private val REFRESH_TOKEN_TTL = Duration.ofDays(7)
    }
}
