from __future__ import annotations

import hashlib
from datetime import timedelta
from urllib.parse import urlencode

from django.conf import settings
from django.contrib.auth.hashers import check_password, make_password
from django.core.mail import send_mail
from django.db import transaction
from django.utils import timezone
from django.utils.crypto import get_random_string

from apps.api.auth import (
    build_token_response,
    decode_password_reset_token,
    decode_refresh_token,
    decode_social_signup_token,
    decode_social_state_token,
    issue_password_reset_token,
    issue_social_signup_token,
    issue_social_state_token,
)
from apps.api.errors import ApiError
from apps.api.utils import (
    normalize_phone,
    parse_mock_social_token,
    require_fields,
    validate_email_value,
    validate_name_value,
    validate_nickname_value,
    validate_password_value,
    validate_provider,
)
from apps.auth.models import EmailVerification, SocialAccount, VerificationType
from apps.users.models import CustomUser, UserStatus


class AuthService:
    def signup(self, data: dict) -> dict:
        require_fields(data, ["email", "password", "name", "phone"])
        email = validate_email_value(data["email"])
        password = validate_password_value(data["password"])
        name = validate_name_value(data["name"])
        phone = normalize_phone(data["phone"])

        if CustomUser.objects.filter(email=email).exists():
            raise ApiError("DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다.", 409)

        nickname = self._generate_unique_nickname(email.split("@")[0])

        with transaction.atomic():
            user = CustomUser.objects.create_user(
                email=email,
                password=password,
                name=name,
                phone=phone,
                nickname=nickname,
                status=UserStatus.ACTIVE,
            )
            user.email_verified = False
            user.save(update_fields=["email_verified", "updated_at"])
            self._create_and_send_verification(user, VerificationType.SIGNUP)

        return {
            "id": user.id,
            "email": user.email,
            "name": user.name,
            "message": "인증 메일이 발송되었습니다. 이메일을 확인해주세요.",
        }

    def verify_email(self, data: dict) -> dict:
        require_fields(data, ["email", "code"])
        email = validate_email_value(data["email"])
        code = str(data["code"]).strip()
        user = CustomUser.objects.filter(email=email, deleted_at__isnull=True).first()
        if user is None:
            raise ApiError("USER_NOT_FOUND", "해당 이메일로 등록된 계정이 없습니다.", 404)
        if user.email_verified:
            raise ApiError("ALREADY_VERIFIED", "이미 인증된 이메일입니다.", 409)

        verification = self._get_latest_verification(user, VerificationType.SIGNUP)
        if verification is None or verification.is_used:
            raise ApiError("INVALID_VERIFICATION_CODE", "인증코드가 올바르지 않습니다.", 400)
        self._assert_verification_available(verification)

        if verification.code != code:
            verification.attempt_count += 1
            verification.save(update_fields=["attempt_count", "updated_at"])
            if verification.attempt_count >= settings.EMAIL_VERIFICATION_MAX_ATTEMPTS:
                raise ApiError("VERIFICATION_ATTEMPT_EXCEEDED", "인증 시도 횟수를 초과했습니다.", 429)
            raise ApiError("INVALID_VERIFICATION_CODE", "인증코드가 올바르지 않습니다.", 400)

        verification.is_used = True
        verification.save(update_fields=["is_used", "updated_at"])
        user.email_verified = True
        user.email_verified_at = timezone.now()
        user.save(update_fields=["email_verified", "email_verified_at", "updated_at"])
        return {"message": "이메일 인증이 완료되었습니다.", "verified": True}

    def resend_verification(self, data: dict) -> dict:
        require_fields(data, ["email"])
        email = validate_email_value(data["email"])
        user = CustomUser.objects.filter(email=email, deleted_at__isnull=True).first()
        if user is None:
            raise ApiError("USER_NOT_FOUND", "해당 이메일로 등록된 계정이 없습니다.", 404)
        if user.email_verified:
            raise ApiError("ALREADY_VERIFIED", "이미 인증된 이메일입니다.", 409)
        self._ensure_resend_allowed(user, VerificationType.SIGNUP)
        self._create_and_send_verification(user, VerificationType.SIGNUP)
        return {"message": "인증 메일이 재발송되었습니다."}

    def login(self, data: dict) -> tuple[dict, CustomUser]:
        require_fields(data, ["email", "password"])
        email = validate_email_value(data["email"])
        password = str(data["password"])
        user = CustomUser.objects.filter(email=email, deleted_at__isnull=True).first()
        if user is None or not user.check_password(password):
            raise ApiError("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다.", 401)
        if not user.email_verified:
            raise ApiError("EMAIL_NOT_VERIFIED", "이메일 인증이 완료되지 않았습니다. 인증 메일을 확인해주세요.", 403)
        if user.status == UserStatus.BLOCKED:
            raise ApiError("USER_BLOCKED", "차단된 계정입니다.", 403)

        tokens = build_token_response(user)
        user.refresh_token = make_password(tokens["refreshToken"])
        user.last_login_at = timezone.now()
        user.save(update_fields=["refresh_token", "last_login", "updated_at"])
        return tokens, user

    def logout(self, user: CustomUser) -> dict:
        user.refresh_token = None
        user.save(update_fields=["refresh_token", "updated_at"])
        return {"message": "로그아웃 되었습니다."}

    def refresh(self, data: dict) -> tuple[dict, CustomUser]:
        require_fields(data, ["refreshToken"])
        payload = decode_refresh_token(str(data["refreshToken"]))
        user = CustomUser.objects.filter(id=payload["sub"], deleted_at__isnull=True).first()
        if user is None or not user.refresh_token:
            raise ApiError("INVALID_REFRESH_TOKEN", "유효하지 않거나 만료된 리프레시 토큰입니다.", 401)
        if not check_password(str(data["refreshToken"]), user.refresh_token):
            raise ApiError("INVALID_REFRESH_TOKEN", "유효하지 않거나 만료된 리프레시 토큰입니다.", 401)

        tokens = build_token_response(user)
        user.refresh_token = make_password(tokens["refreshToken"])
        user.save(update_fields=["refresh_token", "updated_at"])
        return tokens, user

    def request_password_reset(self, data: dict) -> dict:
        require_fields(data, ["email", "phone"])
        email = validate_email_value(data["email"])
        normalized_phone = normalize_phone(data["phone"])
        user = CustomUser.objects.filter(email=email, deleted_at__isnull=True).first()
        if user is None or user.phone != normalized_phone:
            self._equalize_failure_timing()
            return {"message": "비밀번호 재설정 인증 메일이 발송되었습니다."}

        self._ensure_resend_allowed(user, VerificationType.PASSWORD_RESET)
        self._create_and_send_verification(user, VerificationType.PASSWORD_RESET)
        return {"message": "비밀번호 재설정 인증 메일이 발송되었습니다."}

    def verify_reset_code(self, data: dict) -> dict:
        require_fields(data, ["email", "code"])
        email = validate_email_value(data["email"])
        code = str(data["code"]).strip()
        user = CustomUser.objects.filter(email=email, deleted_at__isnull=True).first()
        if user is None:
            raise ApiError("USER_NOT_FOUND", "해당 이메일로 등록된 계정이 없습니다.", 404)

        verification = self._get_latest_verification(user, VerificationType.PASSWORD_RESET)
        if verification is None or verification.is_used:
            raise ApiError("INVALID_VERIFICATION_CODE", "인증코드가 올바르지 않습니다.", 400)
        self._assert_verification_available(verification)

        if verification.code != code:
            verification.attempt_count += 1
            verification.save(update_fields=["attempt_count", "updated_at"])
            if verification.attempt_count >= settings.EMAIL_VERIFICATION_MAX_ATTEMPTS:
                raise ApiError("VERIFICATION_ATTEMPT_EXCEEDED", "인증 시도 횟수를 초과했습니다.", 429)
            raise ApiError("INVALID_VERIFICATION_CODE", "인증코드가 올바르지 않습니다.", 400)

        verification.is_used = True
        verification.save(update_fields=["is_used", "updated_at"])
        return {"resetToken": issue_password_reset_token(user)}

    def reset_password(self, data: dict) -> dict:
        require_fields(data, ["resetToken", "newPassword"])
        payload = decode_password_reset_token(str(data["resetToken"]))
        new_password = validate_password_value(str(data["newPassword"]))
        user = CustomUser.objects.filter(id=payload["sub"], deleted_at__isnull=True).first()
        if user is None:
            raise ApiError("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", 404)
        if user.check_password(new_password):
            raise ApiError("SAME_PASSWORD", "기존 비밀번호와 동일한 비밀번호입니다.", 400)

        user.set_password(new_password)
        user.refresh_token = None
        user.save(update_fields=["password", "refresh_token", "updated_at"])
        return {"message": "비밀번호가 성공적으로 변경되었습니다."}

    def get_social_auth_redirect(self, provider: str) -> tuple[str, str]:
        provider = validate_provider(provider)
        state_token = issue_social_state_token(provider)
        redirect_uri = f"http://localhost:8000/api/v1/auth/callback/{provider}"
        params = urlencode(
            {
                "client_id": f"mock-{provider}-client",
                "redirect_uri": redirect_uri,
                "response_type": "code",
                "state": state_token,
            }
        )
        return state_token, f"https://auth.{provider}.example/authorize?{params}"

    def social_callback(self, provider: str, code: str, state: str) -> tuple[dict, str | None]:
        provider = validate_provider(provider)
        if not code:
            raise ApiError("SOCIAL_AUTH_FAILED", "소셜 인증에 실패했습니다.", 401)
        decode_social_state_token(state, provider)
        profile = self._resolve_social_profile(provider, code)

        social = SocialAccount.objects.filter(
            provider=provider,
            provider_user_id=profile["provider_user_id"],
            deleted_at__isnull=True,
        ).select_related("user").first()
        if social is not None:
            return {**self._issue_tokens_for_user(social.user), "isNewUser": False}, None

        if profile["email"]:
            existing_user = CustomUser.objects.filter(
                email=profile["email"],
                deleted_at__isnull=True,
            ).first()
            if existing_user is not None:
                SocialAccount.objects.create(
                    user=existing_user,
                    provider=provider,
                    provider_user_id=profile["provider_user_id"],
                    provider_email=profile["email"],
                    provider_name=profile["name"],
                )
                return {**self._issue_tokens_for_user(existing_user), "isNewUser": False}, None

        signup_token = issue_social_signup_token(
            {
                "provider": provider,
                "provider_user_id": profile["provider_user_id"],
                "email": profile["email"],
                "name": profile["name"],
            }
        )
        return {"isNewUser": True}, signup_token

    def complete_social_signup(self, signup_token: str, data: dict) -> tuple[dict, CustomUser]:
        require_fields(data, ["phone", "nickname"])
        payload = decode_social_signup_token(signup_token)
        phone = normalize_phone(data["phone"])
        nickname = validate_nickname_value(data["nickname"])
        provider = validate_provider(payload["provider"])

        if SocialAccount.objects.filter(
            provider=provider,
            provider_user_id=payload["provider_user_id"],
            deleted_at__isnull=True,
        ).exists():
            raise ApiError("SOCIAL_ALREADY_LINKED", "이미 가입된 소셜 계정입니다.", 409)
        if CustomUser.objects.filter(nickname=nickname).exists():
            raise ApiError("CONFLICT", "이미 사용 중인 닉네임입니다.", 409)

        email = payload.get("email") or f"{provider}_{payload['provider_user_id']}@social.local"
        if CustomUser.objects.filter(email=email).exists():
            raise ApiError("DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다.", 409)

        source_name = (payload.get("name") or nickname).strip()
        if len(source_name) < 2:
            source_name = f"{source_name}01"
        name = validate_name_value(source_name)

        with transaction.atomic():
            user = CustomUser.objects.create_user(
                email=email,
                password=None,
                name=name,
                phone=phone,
                nickname=nickname,
                status=UserStatus.ACTIVE,
            )
            user.email_verified = True
            user.email_verified_at = timezone.now()
            user.save(update_fields=["email_verified", "email_verified_at", "updated_at"])
            SocialAccount.objects.create(
                user=user,
                provider=provider,
                provider_user_id=payload["provider_user_id"],
                provider_email=payload.get("email"),
                provider_name=payload.get("name"),
            )

        return self._issue_tokens_for_user(user), user

    def link_social_account(self, user: CustomUser, data: dict) -> dict:
        require_fields(data, ["provider", "socialToken"])
        provider = validate_provider(data["provider"])
        profile = self._resolve_social_profile(provider, str(data["socialToken"]))

        taken = SocialAccount.objects.filter(
            provider=provider,
            provider_user_id=profile["provider_user_id"],
            deleted_at__isnull=True,
        ).select_related("user").first()
        if taken is not None and taken.user_id != user.id:
            raise ApiError("SOCIAL_ALREADY_LINKED", "이미 다른 계정에 연동된 소셜 계정입니다.", 409)

        if SocialAccount.objects.filter(
            user=user,
            provider=provider,
            deleted_at__isnull=True,
        ).exists():
            raise ApiError("PROVIDER_ALREADY_LINKED", "이미 해당 소셜 서비스가 연동되어 있습니다.", 409)

        SocialAccount.objects.create(
            user=user,
            provider=provider,
            provider_user_id=profile["provider_user_id"],
            provider_email=profile["email"],
            provider_name=profile["name"],
        )
        return {"message": f"{provider} 계정이 연동되었습니다.", "linkedProvider": provider}

    def unlink_social_account(self, user: CustomUser, provider: str) -> dict:
        provider = validate_provider(provider)
        social = SocialAccount.objects.filter(
            user=user,
            provider=provider,
            deleted_at__isnull=True,
        ).first()
        if social is None:
            raise ApiError("SOCIAL_NOT_LINKED", "해당 소셜 서비스가 연동되어 있지 않습니다.", 404)

        active_social_count = SocialAccount.objects.filter(
            user=user,
            deleted_at__isnull=True,
        ).count()
        if not user.has_usable_password() and active_social_count <= 1:
            raise ApiError("CANNOT_UNLINK", "일반 로그인 수단이 없어 소셜 연동을 해제할 수 없습니다.", 400)

        social.deleted_at = timezone.now()
        social.save(update_fields=["deleted_at", "updated_at"])
        return {"message": "소셜 계정 연동이 해제되었습니다."}

    def _issue_tokens_for_user(self, user: CustomUser) -> dict:
        tokens = build_token_response(user)
        user.refresh_token = make_password(tokens["refreshToken"])
        user.last_login_at = timezone.now()
        user.save(update_fields=["refresh_token", "last_login", "updated_at"])
        return tokens

    def _create_and_send_verification(self, user: CustomUser, verification_type: str) -> EmailVerification:
        code = get_random_string(length=6, allowed_chars="0123456789")
        verification = EmailVerification.objects.create(
            user=user,
            type=verification_type,
            code=code,
            expires_at=timezone.now() + timedelta(seconds=settings.EMAIL_VERIFICATION_CODE_EXPIRES_IN_SECONDS),
        )
        self._send_verification_email(user, verification_type, code)
        return verification

    def _send_verification_email(self, user: CustomUser, verification_type: str, code: str) -> None:
        if verification_type == VerificationType.SIGNUP:
            subject = "[PBShop] 이메일 인증 코드"
            message = f"{user.name}님, 이메일 인증코드는 {code} 입니다."
        else:
            subject = "[PBShop] 비밀번호 재설정 인증 코드"
            message = f"{user.name}님, 비밀번호 재설정 인증코드는 {code} 입니다."
        send_mail(subject, message, settings.DEFAULT_FROM_EMAIL, [user.email], fail_silently=False)

    def _get_latest_verification(self, user: CustomUser, verification_type: str) -> EmailVerification | None:
        return EmailVerification.objects.filter(user=user, type=verification_type).order_by("-created_at").first()

    def _assert_verification_available(self, verification: EmailVerification) -> None:
        if verification.attempt_count >= settings.EMAIL_VERIFICATION_MAX_ATTEMPTS:
            raise ApiError("VERIFICATION_ATTEMPT_EXCEEDED", "인증 시도 횟수를 초과했습니다.", 429)
        if verification.expires_at <= timezone.now():
            raise ApiError("VERIFICATION_CODE_EXPIRED", "인증코드가 만료되었습니다. 재발송해주세요.", 410)

    def _ensure_resend_allowed(self, user: CustomUser, verification_type: str) -> None:
        recent = self._get_latest_verification(user, verification_type)
        if recent is None:
            return
        cutoff = timezone.now() - timedelta(seconds=settings.EMAIL_VERIFICATION_RESEND_INTERVAL_SECONDS)
        if recent.created_at > cutoff:
            raise ApiError("RESEND_RATE_LIMITED", "1분 후 다시 시도해주세요.", 429)

    def _resolve_social_profile(self, provider: str, social_token: str) -> dict[str, str | None]:
        mock_values = parse_mock_social_token(social_token)
        raw_token = str(social_token).strip()
        provider_user_id = mock_values.get("id")
        if not provider_user_id:
            digest = hashlib.sha256(raw_token.encode("utf-8")).hexdigest()[:24]
            provider_user_id = f"{provider}_{digest}"

        email = mock_values.get("email")
        if email:
            email = validate_email_value(email)
        else:
            email = f"{provider}_{provider_user_id[-12:]}@social.local"

        name = mock_values.get("name") or f"{provider}_user"
        return {
            "provider_user_id": provider_user_id,
            "email": email,
            "name": name[:100],
        }

    def _generate_unique_nickname(self, base: str) -> str:
        base_text = "".join(ch for ch in base if ch.isalnum())[:20] or "user"
        for _ in range(50):
            candidate = f"{base_text}{get_random_string(length=5, allowed_chars='0123456789')}"[:30]
            if not CustomUser.objects.filter(nickname=candidate).exists():
                return candidate
        raise ApiError("CONFLICT", "닉네임 생성에 실패했습니다.", 409)

    def _equalize_failure_timing(self) -> None:
        timezone.now()
