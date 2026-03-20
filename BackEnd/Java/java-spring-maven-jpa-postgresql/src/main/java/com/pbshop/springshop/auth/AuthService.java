package com.pbshop.springshop.auth;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import com.pbshop.springshop.auth.dto.AuthDtos.LoginRequest;
import com.pbshop.springshop.auth.dto.AuthDtos.PasswordResetConfirmRequest;
import com.pbshop.springshop.auth.dto.AuthDtos.PasswordResetRequest;
import com.pbshop.springshop.auth.dto.AuthDtos.PasswordResetVerifyRequest;
import com.pbshop.springshop.auth.dto.AuthDtos.RefreshRequest;
import com.pbshop.springshop.auth.dto.AuthDtos.ResendVerificationRequest;
import com.pbshop.springshop.auth.dto.AuthDtos.SignupRequest;
import com.pbshop.springshop.auth.dto.AuthDtos.SocialCompleteRequest;
import com.pbshop.springshop.auth.dto.AuthDtos.SocialLinkRequest;
import com.pbshop.springshop.auth.dto.AuthDtos.VerifyEmailRequest;
import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.exception.BusinessException;
import com.pbshop.springshop.common.exception.ErrorCode;
import com.pbshop.springshop.config.PbshopProperties;
import com.pbshop.springshop.user.User;
import com.pbshop.springshop.user.UserRepository;

@Service
@Transactional
public class AuthService {

    private static final Set<String> SUPPORTED_PROVIDERS = Set.of("google", "naver", "kakao", "facebook", "instagram");
    private static final long ACCESS_TOKEN_SECONDS = 1800L;
    private static final long REFRESH_TOKEN_SECONDS = 60L * 60 * 24 * 14;

    private final UserRepository userRepository;
    private final AuthVerificationCodeRepository verificationCodeRepository;
    private final AuthAccessTokenRepository accessTokenRepository;
    private final AuthRefreshTokenRepository refreshTokenRepository;
    private final AuthPasswordResetRequestRepository passwordResetRequestRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final PbshopProperties pbshopProperties;

    public AuthService(
            UserRepository userRepository,
            AuthVerificationCodeRepository verificationCodeRepository,
            AuthAccessTokenRepository accessTokenRepository,
            AuthRefreshTokenRepository refreshTokenRepository,
            AuthPasswordResetRequestRepository passwordResetRequestRepository,
            SocialAccountRepository socialAccountRepository,
            PasswordEncoder passwordEncoder,
            PbshopProperties pbshopProperties
    ) {
        this.userRepository = userRepository;
        this.verificationCodeRepository = verificationCodeRepository;
        this.accessTokenRepository = accessTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetRequestRepository = passwordResetRequestRepository;
        this.socialAccountRepository = socialAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.pbshopProperties = pbshopProperties;
    }

    public Map<String, Object> signup(SignupRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "이미 가입된 이메일입니다.");
        });

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setName(request.name());
        user.setPhone(request.phone());
        user.setRole("USER");
        user.setStatus("ACTIVE");
        user.setEmailVerified(false);
        user = userRepository.save(user);

        issueVerificationCode(user);

        return Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "name", user.getName(),
                "message", "회원가입이 완료되었습니다. 인증 코드를 확인하세요."
        );
    }

    public Map<String, Object> verifyEmail(VerifyEmailRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        AuthVerificationCode code = verificationCodeRepository.findTopByEmailAndPurposeAndConsumedAtIsNullOrderByIdDesc(
                        request.email(), "EMAIL_VERIFY")
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "인증 코드를 찾을 수 없습니다."));

        if (!code.getCode().equals(request.code()) || code.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "유효하지 않은 인증 코드입니다.");
        }

        code.setConsumedAt(OffsetDateTime.now());
        verificationCodeRepository.save(code);
        user.setEmailVerified(true);
        userRepository.save(user);

        return Map.of("message", "이메일 인증이 완료되었습니다.", "verified", true);
    }

    public Map<String, Object> resendVerification(ResendVerificationRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (user.isEmailVerified()) {
            return Map.of("message", "이미 인증이 완료된 계정입니다.");
        }

        issueVerificationCode(user);
        return Map.of("message", "인증 코드가 재발송되었습니다.");
    }

    public Map<String, Object> login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!user.isEmailVerified()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "이메일 인증이 필요합니다.");
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "활성 상태의 계정만 로그인할 수 있습니다.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return issueTokenPair(user, false);
    }

    public Map<String, Object> logout(AuthenticatedUserPrincipal principal) {
        accessTokenRepository.findByTokenAndRevokedAtIsNullAndExpiresAtAfter(principal.accessToken(), OffsetDateTime.now())
                .ifPresent(token -> {
                    token.setRevokedAt(OffsetDateTime.now());
                    accessTokenRepository.save(token);
                });

        return Map.of("message", "로그아웃 되었습니다.");
    }

    public Map<String, Object> refresh(RefreshRequest request) {
        AuthRefreshToken refreshToken = refreshTokenRepository.findByTokenAndRevokedAtIsNullAndExpiresAtAfter(
                        request.refreshToken(), OffsetDateTime.now())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "유효하지 않은 refresh token 입니다."));

        refreshToken.setRevokedAt(OffsetDateTime.now());
        refreshTokenRepository.save(refreshToken);

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "활성 상태의 계정만 토큰을 재발급할 수 있습니다.");
        }

        return issueTokenPair(user, false);
    }

    public Map<String, Object> requestPasswordReset(PasswordResetRequest request) {
        User user = userRepository.findByEmail(request.email())
                .filter(found -> request.phone().equals(found.getPhone()))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "일치하는 사용자 정보를 찾을 수 없습니다."));

        AuthPasswordResetRequest resetRequest = new AuthPasswordResetRequest();
        resetRequest.setUserId(user.getId());
        resetRequest.setEmail(user.getEmail());
        resetRequest.setPhone(user.getPhone());
        resetRequest.setCode(issueCode());
        resetRequest.setExpiresAt(OffsetDateTime.now().plusMinutes(10));
        passwordResetRequestRepository.save(resetRequest);

        return Map.of("message", "비밀번호 재설정 인증 코드가 발급되었습니다.");
    }

    public Map<String, Object> verifyPasswordReset(PasswordResetVerifyRequest request) {
        AuthPasswordResetRequest resetRequest = passwordResetRequestRepository.findTopByEmailAndConsumedAtIsNullOrderByIdDesc(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "재설정 요청을 찾을 수 없습니다."));

        if (!resetRequest.getCode().equals(request.code()) || resetRequest.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "유효하지 않은 재설정 코드입니다.");
        }

        resetRequest.setVerifiedAt(OffsetDateTime.now());
        resetRequest.setResetToken("rst_" + UUID.randomUUID());
        passwordResetRequestRepository.save(resetRequest);

        return Map.of("resetToken", resetRequest.getResetToken());
    }

    public Map<String, Object> confirmPasswordReset(PasswordResetConfirmRequest request) {
        AuthPasswordResetRequest resetRequest = passwordResetRequestRepository.findByResetTokenAndConsumedAtIsNull(request.resetToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "유효하지 않은 재설정 토큰입니다."));

        if (resetRequest.getVerifiedAt() == null || resetRequest.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "유효하지 않은 재설정 토큰입니다.");
        }

        User user = userRepository.findById(resetRequest.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "기존 비밀번호와 동일한 비밀번호입니다.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        resetRequest.setConsumedAt(OffsetDateTime.now());
        passwordResetRequestRepository.save(resetRequest);

        for (AuthRefreshToken token : refreshTokenRepository.findByUserIdAndRevokedAtIsNull(user.getId())) {
            token.setRevokedAt(OffsetDateTime.now());
        }

        return Map.of("message", "비밀번호가 변경되었습니다.");
    }

    public URI socialLoginRedirect(String provider) {
        validateProvider(provider);
        return UriComponentsBuilder.fromUriString(pbshopProperties.frontendUrl())
                .path("/public/login")
                .queryParam("provider", provider)
                .build()
                .toUri();
    }

    public Map<String, Object> socialCallback(String provider, String code) {
        validateProvider(provider);

        String providerUserId = provider + ":" + (code == null || code.isBlank() ? "mock-user" : code);
        SocialAccount account = socialAccountRepository.findByProviderAndProviderUserId(provider, providerUserId).orElse(null);
        boolean isNewUser = account == null;
        User user;

        if (account == null) {
            user = new User();
            user.setEmail(provider + "+" + providerUserId.replace(':', '_') + "@pbshop.local");
            user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setName(provider + "_user");
            user.setPhone("010-0000-0000");
            user.setRole("USER");
            user.setStatus("ACTIVE");
            user.setEmailVerified(true);
            user = userRepository.save(user);

            account = new SocialAccount();
            account.setUserId(user.getId());
            account.setProvider(provider);
            account.setProviderUserId(providerUserId);
            socialAccountRepository.save(account);
        } else {
            user = userRepository.findById(account.getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        }

        return issueTokenPair(user, isNewUser);
    }

    public Map<String, Object> socialComplete(AuthenticatedUserPrincipal principal, SocialCompleteRequest request) {
        User user = userRepository.findById(principal.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        user.setPhone(request.phone());
        user.setName(request.nickname());
        userRepository.save(user);
        return issueTokenPair(user, false);
    }

    public Map<String, Object> socialLink(AuthenticatedUserPrincipal principal, SocialLinkRequest request) {
        validateProvider(request.provider());
        String providerUserId = request.provider() + ":" + request.socialToken();

        socialAccountRepository.findByProviderAndProviderUserId(request.provider(), providerUserId).ifPresent(existing -> {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "이미 연동된 소셜 계정입니다.");
        });

        SocialAccount account = socialAccountRepository.findByUserIdAndProvider(principal.userId(), request.provider())
                .orElse(new SocialAccount());
        account.setUserId(principal.userId());
        account.setProvider(request.provider());
        account.setProviderUserId(providerUserId);
        socialAccountRepository.save(account);

        return Map.of("message", "소셜 계정이 연동되었습니다.", "linkedProvider", request.provider());
    }

    public Map<String, Object> socialUnlink(AuthenticatedUserPrincipal principal, String provider) {
        validateProvider(provider);
        SocialAccount account = socialAccountRepository.findByUserIdAndProvider(principal.userId(), provider)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "연동된 소셜 계정을 찾을 수 없습니다."));
        socialAccountRepository.delete(account);

        return Map.of("message", "소셜 계정 연동이 해제되었습니다.");
    }

    private void issueVerificationCode(User user) {
        AuthVerificationCode code = new AuthVerificationCode();
        code.setUserId(user.getId());
        code.setEmail(user.getEmail());
        code.setCode(issueCode());
        code.setPurpose("EMAIL_VERIFY");
        code.setExpiresAt(OffsetDateTime.now().plusMinutes(10));
        verificationCodeRepository.save(code);
    }

    private String issueCode() {
        return String.valueOf((int) (100000 + Math.random() * 900000));
    }

    private Map<String, Object> issueTokenPair(User user, boolean isNewUser) {
        AuthAccessToken accessToken = new AuthAccessToken();
        accessToken.setUserId(user.getId());
        accessToken.setToken("atk_" + UUID.randomUUID());
        accessToken.setExpiresAt(OffsetDateTime.now().plusSeconds(ACCESS_TOKEN_SECONDS));
        accessTokenRepository.save(accessToken);

        AuthRefreshToken refreshToken = new AuthRefreshToken();
        refreshToken.setUserId(user.getId());
        refreshToken.setToken("rtk_" + UUID.randomUUID());
        refreshToken.setExpiresAt(OffsetDateTime.now().plusSeconds(REFRESH_TOKEN_SECONDS));
        refreshTokenRepository.save(refreshToken);

        return Map.of(
                "accessToken", accessToken.getToken(),
                "refreshToken", refreshToken.getToken(),
                "expiresIn", ACCESS_TOKEN_SECONDS,
                "isNewUser", isNewUser
        );
    }

    private void validateProvider(String provider) {
        if (!SUPPORTED_PROVIDERS.contains(provider)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "지원하지 않는 소셜 provider 입니다.");
        }
    }
}
