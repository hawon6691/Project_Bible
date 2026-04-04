from __future__ import annotations

from datetime import datetime, timedelta
from functools import wraps
from typing import Iterable

import jwt
from django.conf import settings
from django.utils import timezone

from apps.api.errors import ApiError
from apps.users.models import CustomUser


def issue_token(
    payload: dict,
    *,
    secret: str,
    expires_in: int,
) -> str:
    now = timezone.now()
    data = {
        **payload,
        "iat": int(now.timestamp()),
        "exp": int((now + timedelta(seconds=expires_in)).timestamp()),
    }
    return jwt.encode(data, secret, algorithm="HS256")


def decode_token(token: str, *, secret: str) -> dict:
    try:
        return jwt.decode(token, secret, algorithms=["HS256"])
    except jwt.ExpiredSignatureError as exc:
        raise ApiError("TOKEN_EXPIRED", "토큰이 만료되었습니다.", 401) from exc
    except jwt.InvalidTokenError as exc:
        raise ApiError("UNAUTHORIZED", "유효하지 않은 토큰입니다.", 401) from exc


def issue_access_token(user: CustomUser) -> str:
    return issue_token(
        {
            "sub": str(user.id),
            "email": user.email,
            "role": user.role,
            "type": "access",
        },
        secret=settings.JWT_ACCESS_SECRET,
        expires_in=settings.JWT_ACCESS_EXPIRES_IN_SECONDS,
    )


def issue_refresh_token(user: CustomUser) -> str:
    return issue_token(
        {
            "sub": str(user.id),
            "email": user.email,
            "role": user.role,
            "type": "refresh",
        },
        secret=settings.JWT_REFRESH_SECRET,
        expires_in=settings.JWT_REFRESH_EXPIRES_IN_SECONDS,
    )


def issue_password_reset_token(user: CustomUser) -> str:
    return issue_token(
        {
            "sub": str(user.id),
            "email": user.email,
            "type": "password_reset",
        },
        secret=settings.JWT_ACCESS_SECRET,
        expires_in=settings.PASSWORD_RESET_TOKEN_EXPIRES_IN_SECONDS,
    )


def issue_social_signup_token(payload: dict) -> str:
    return issue_token(
        {**payload, "type": "social_signup"},
        secret=settings.JWT_ACCESS_SECRET,
        expires_in=settings.JWT_SIGNUP_TOKEN_EXPIRES_IN_SECONDS,
    )


def issue_social_state_token(provider: str) -> str:
    return issue_token(
        {"provider": provider, "type": "social_state"},
        secret=settings.JWT_ACCESS_SECRET,
        expires_in=settings.PBSHOP_SOCIAL_STATE_EXPIRES_IN_SECONDS,
    )


def decode_access_token(token: str) -> dict:
    payload = decode_token(token, secret=settings.JWT_ACCESS_SECRET)
    if payload.get("type") != "access":
        raise ApiError("UNAUTHORIZED", "유효하지 않은 액세스 토큰입니다.", 401)
    return payload


def decode_refresh_token(token: str) -> dict:
    payload = decode_token(token, secret=settings.JWT_REFRESH_SECRET)
    if payload.get("type") != "refresh":
        raise ApiError("INVALID_REFRESH_TOKEN", "유효하지 않거나 만료된 리프레시 토큰입니다.", 401)
    return payload


def decode_password_reset_token(token: str) -> dict:
    payload = decode_token(token, secret=settings.JWT_ACCESS_SECRET)
    if payload.get("type") != "password_reset":
        raise ApiError("INVALID_RESET_TOKEN", "유효하지 않거나 만료된 재설정 토큰입니다.", 401)
    return payload


def decode_social_signup_token(token: str) -> dict:
    payload = decode_token(token, secret=settings.JWT_ACCESS_SECRET)
    if payload.get("type") != "social_signup":
        raise ApiError("INVALID_SOCIAL_SIGNUP_TOKEN", "유효하지 않은 소셜 가입 토큰입니다.", 401)
    return payload


def decode_social_state_token(token: str, provider: str) -> dict:
    payload = decode_token(token, secret=settings.JWT_ACCESS_SECRET)
    if payload.get("type") != "social_state" or payload.get("provider") != provider:
        raise ApiError("INVALID_STATE", "유효하지 않은 state 토큰입니다.", 400)
    return payload


def build_token_response(user: CustomUser) -> dict:
    access_token = issue_access_token(user)
    refresh_token = issue_refresh_token(user)
    return {
        "accessToken": access_token,
        "refreshToken": refresh_token,
        "expiresIn": settings.JWT_ACCESS_EXPIRES_IN_SECONDS,
    }


def extract_bearer_token(authorization_header: str | None) -> str:
    if not authorization_header:
        raise ApiError("UNAUTHORIZED", "인증이 필요합니다.", 401)

    prefix = "Bearer "
    if not authorization_header.startswith(prefix):
        raise ApiError("UNAUTHORIZED", "Bearer 토큰이 필요합니다.", 401)

    token = authorization_header[len(prefix) :].strip()
    if not token:
        raise ApiError("UNAUTHORIZED", "Bearer 토큰이 필요합니다.", 401)

    return token


def require_auth(roles: Iterable[str] | None = None):
    role_set = set(roles or [])

    def decorator(view_func):
        @wraps(view_func)
        def wrapped(request, *args, **kwargs):
            token = extract_bearer_token(request.headers.get("Authorization"))
            payload = decode_access_token(token)
            user = (
                CustomUser.objects.filter(id=payload.get("sub"), deleted_at__isnull=True)
                .only(
                    "id",
                    "email",
                    "role",
                    "status",
                    "email_verified",
                    "nickname",
                    "phone",
                    "name",
                    "profile_image_url",
                    "created_at",
                    "point",
                )
                .first()
            )
            if user is None:
                raise ApiError("UNAUTHORIZED", "유효한 사용자 토큰이 아닙니다.", 401)
            if role_set and user.role not in role_set:
                raise ApiError("FORBIDDEN", "권한이 없습니다.", 403)

            request.auth_user = user
            request.auth_payload = payload
            return view_func(request, *args, **kwargs)

        return wrapped

    return decorator
