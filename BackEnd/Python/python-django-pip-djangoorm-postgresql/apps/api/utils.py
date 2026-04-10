from __future__ import annotations

import re
from urllib.parse import parse_qs

from django.core.validators import validate_email
from django.utils.translation import gettext_lazy as _

from apps.api.errors import ApiError

PASSWORD_PATTERN = re.compile(r"^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z\d]).{8,}$")
PHONE_DIGIT_PATTERN = re.compile(r"\D")
SOCIAL_PROVIDER_PATTERN = re.compile(r"^(google|naver|kakao|facebook|instagram)$")


def require_fields(data: dict, fields: list[str]) -> None:
    missing = [field for field in fields if data.get(field) in (None, "")]
    if missing:
        raise ApiError("VALIDATION_ERROR", f"필수 값이 누락되었습니다: {', '.join(missing)}", 400)


def validate_email_value(value: str) -> str:
    try:
        validate_email(value)
    except Exception as exc:
        raise ApiError("VALIDATION_ERROR", "올바른 이메일 형식이 아닙니다.", 400) from exc
    return value.strip().lower()


def validate_password_value(value: str) -> str:
    if not PASSWORD_PATTERN.match(value or ""):
        raise ApiError("VALIDATION_ERROR", "비밀번호는 8자 이상이며 영문, 숫자, 특수문자를 포함해야 합니다.", 400)
    return value


def validate_name_value(value: str) -> str:
    normalized = (value or "").strip()
    if len(normalized) < 2 or len(normalized) > 20:
        raise ApiError("VALIDATION_ERROR", "이름은 2자 이상 20자 이하여야 합니다.", 400)
    return normalized


def normalize_phone(value: str) -> str:
    digits = PHONE_DIGIT_PATTERN.sub("", value or "")
    if len(digits) not in {10, 11}:
        raise ApiError("VALIDATION_ERROR", "올바른 전화번호 형식이 아닙니다.", 400)
    return digits


def format_phone(value: str | None) -> str | None:
    if not value:
        return value
    digits = PHONE_DIGIT_PATTERN.sub("", value)
    if len(digits) == 11:
        return f"{digits[:3]}-{digits[3:7]}-{digits[7:]}"
    if len(digits) == 10:
        return f"{digits[:3]}-{digits[3:6]}-{digits[6:]}"
    return value


def validate_nickname_value(value: str) -> str:
    normalized = (value or "").strip()
    if not normalized:
        raise ApiError("VALIDATION_ERROR", "닉네임은 비워둘 수 없습니다.", 400)
    if len(normalized) > 30:
        raise ApiError("VALIDATION_ERROR", "닉네임은 30자 이하여야 합니다.", 400)
    return normalized


def validate_bio_value(value: str | None) -> str | None:
    if value is None:
        return None
    normalized = value.strip()
    if len(normalized) > 200:
        raise ApiError("VALIDATION_ERROR", "소개글은 200자 이하여야 합니다.", 400)
    return normalized or None


def validate_provider(provider: str) -> str:
    normalized = (provider or "").strip().lower()
    if not SOCIAL_PROVIDER_PATTERN.match(normalized):
        raise ApiError("INVALID_PROVIDER", "지원하지 않는 소셜 로그인 공급자입니다.", 400)
    return normalized


def parse_mock_social_token(value: str) -> dict[str, str]:
    token = (value or "").strip()
    if not token:
        raise ApiError("SOCIAL_AUTH_FAILED", "소셜 인증에 실패했습니다.", 401)
    if token.startswith("mock:"):
        return {key: values[-1] for key, values in parse_qs(token[5:]).items()}
    return {}
