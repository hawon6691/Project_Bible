from __future__ import annotations

import os
import uuid
from pathlib import Path

from django.conf import settings
from django.core.files.uploadedfile import UploadedFile
from django.db.models import Q
from django.utils import timezone
from PIL import Image, UnidentifiedImageError

from apps.api.errors import ApiError
from apps.api.pagination import build_pagination_meta, parse_pagination
from apps.api.utils import (
    format_phone,
    normalize_phone,
    validate_bio_value,
    validate_name_value,
    validate_nickname_value,
    validate_password_value,
)
from apps.users.models import CustomUser, UserRole, UserStatus


class UserService:
    def get_me(self, user: CustomUser) -> dict:
        refreshed = CustomUser.objects.filter(id=user.id, deleted_at__isnull=True).first()
        if refreshed is None:
            raise ApiError("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", 404)
        return self._serialize_user_response(refreshed)

    def update_me(self, user: CustomUser, data: dict) -> dict:
        target = CustomUser.objects.filter(id=user.id, deleted_at__isnull=True).first()
        if target is None:
            raise ApiError("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", 404)

        updated_fields: list[str] = []
        if "name" in data:
            target.name = validate_name_value(str(data["name"]))
            updated_fields.append("name")
        if "phone" in data:
            target.phone = normalize_phone(str(data["phone"]))
            updated_fields.append("phone")
        if "password" in data:
            new_password = validate_password_value(str(data["password"]))
            if target.check_password(new_password):
                raise ApiError("SAME_PASSWORD", "기존 비밀번호와 동일한 비밀번호입니다.", 400)
            target.set_password(new_password)
            target.refresh_token = None
            updated_fields.extend(["password", "refresh_token"])

        if updated_fields:
            updated_fields.append("updated_at")
            target.save(update_fields=updated_fields)

        return self._serialize_user_response(target)

    def delete_me(self, user: CustomUser) -> dict:
        target = CustomUser.objects.filter(id=user.id, deleted_at__isnull=True).first()
        if target is None:
            raise ApiError("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", 404)
        target.deleted_at = timezone.now()
        target.status = UserStatus.INACTIVE
        target.refresh_token = None
        target.save(update_fields=["deleted_at", "status", "refresh_token", "updated_at"])
        return {"message": "회원 탈퇴가 완료되었습니다."}

    def list_users(self, querydict) -> tuple[list[dict], dict]:
        page, limit = parse_pagination(querydict)
        queryset = CustomUser.objects.filter(deleted_at__isnull=True).order_by("-created_at")

        search = querydict.get("search", "").strip()
        if search:
            queryset = queryset.filter(Q(email__icontains=search) | Q(name__icontains=search))

        status = querydict.get("status", "").strip()
        if status:
            if status not in UserStatus.values:
                raise ApiError("VALIDATION_ERROR", "유효하지 않은 회원 상태입니다.", 400)
            queryset = queryset.filter(status=status)

        role = querydict.get("role", "").strip()
        if role:
            if role not in UserRole.values:
                raise ApiError("VALIDATION_ERROR", "유효하지 않은 회원 역할입니다.", 400)
            queryset = queryset.filter(role=role)

        total_count = queryset.count()
        start = (page - 1) * limit
        items = queryset[start : start + limit]
        return [self._serialize_user_response(item) for item in items], build_pagination_meta(
            page=page,
            limit=limit,
            total_count=total_count,
        )

    def update_status(self, user_id: int, data: dict) -> dict:
        status = str(data.get("status", "")).strip()
        if status not in UserStatus.values:
            raise ApiError("VALIDATION_ERROR", "유효하지 않은 회원 상태입니다.", 400)
        user = CustomUser.objects.filter(id=user_id, deleted_at__isnull=True).first()
        if user is None:
            raise ApiError("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", 404)
        user.status = status
        user.save(update_fields=["status", "updated_at"])
        return self._serialize_user_response(user)

    def get_profile(self, user_id: int) -> dict:
        user = CustomUser.objects.filter(id=user_id, deleted_at__isnull=True).first()
        if user is None:
            raise ApiError("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", 404)
        return {
            "id": user.id,
            "nickname": user.nickname,
            "bio": user.bio,
            "profileImageUrl": self._profile_image_url(user),
        }

    def update_profile(self, user: CustomUser, data: dict) -> dict:
        target = CustomUser.objects.filter(id=user.id, deleted_at__isnull=True).first()
        if target is None:
            raise ApiError("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", 404)

        updated_fields: list[str] = []
        if "nickname" in data:
            nickname = validate_nickname_value(str(data["nickname"]))
            exists = CustomUser.objects.filter(nickname=nickname).exclude(id=target.id).exists()
            if exists:
                raise ApiError("CONFLICT", "이미 사용 중인 닉네임입니다.", 409)
            target.nickname = nickname
            updated_fields.append("nickname")
        if "bio" in data:
            target.bio = validate_bio_value(data["bio"])
            updated_fields.append("bio")

        if updated_fields:
            updated_fields.append("updated_at")
            target.save(update_fields=updated_fields)

        return {
            "id": target.id,
            "nickname": target.nickname,
            "bio": target.bio,
            "profileImageUrl": self._profile_image_url(target),
        }

    def upload_profile_image(self, user: CustomUser, file: UploadedFile | None) -> dict:
        if file is None:
            raise ApiError("VALIDATION_ERROR", "업로드할 이미지 파일이 필요합니다.", 400)
        target = CustomUser.objects.filter(id=user.id, deleted_at__isnull=True).first()
        if target is None:
            raise ApiError("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", 404)

        self._validate_image_file(file)
        upload_dir = Path(settings.MEDIA_ROOT) / settings.PBSHOP_PROFILE_IMAGE_DIR
        upload_dir.mkdir(parents=True, exist_ok=True)
        extension = Path(file.name).suffix.lower() or ".png"
        filename = f"{uuid.uuid4().hex}{extension}"
        file_path = upload_dir / filename
        with file_path.open("wb") as destination:
            for chunk in file.chunks():
                destination.write(chunk)

        target.profile_image_url = f"{settings.MEDIA_URL.rstrip('/')}/{settings.PBSHOP_PROFILE_IMAGE_DIR}/{filename}"
        target.save(update_fields=["profile_image_url", "updated_at"])
        return {"imageUrl": self._profile_image_url(target)}

    def delete_profile_image(self, user: CustomUser) -> dict:
        target = CustomUser.objects.filter(id=user.id, deleted_at__isnull=True).first()
        if target is None:
            raise ApiError("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", 404)
        self._delete_local_profile_image(target.profile_image_url)
        target.profile_image_url = settings.PBSHOP_DEFAULT_PROFILE_IMAGE_URL
        target.save(update_fields=["profile_image_url", "updated_at"])
        return {"message": "프로필 이미지가 기본 이미지로 변경되었습니다."}

    def _serialize_user_response(self, user: CustomUser) -> dict:
        return {
            "id": user.id,
            "email": user.email,
            "name": user.name,
            "phone": format_phone(user.phone),
            "role": user.role,
            "status": user.status,
            "point": user.point,
            "badges": [],
            "createdAt": user.created_at.isoformat().replace("+00:00", "Z") if user.created_at else None,
        }

    def _profile_image_url(self, user: CustomUser) -> str:
        return user.profile_image_url or settings.PBSHOP_DEFAULT_PROFILE_IMAGE_URL

    def _validate_image_file(self, file: UploadedFile) -> None:
        try:
            with Image.open(file) as image:
                image.verify()
        except (UnidentifiedImageError, OSError) as exc:
            raise ApiError("VALIDATION_ERROR", "유효한 이미지 파일만 업로드할 수 있습니다.", 400) from exc
        finally:
            file.seek(0)

    def _delete_local_profile_image(self, image_url: str | None) -> None:
        if not image_url or image_url == settings.PBSHOP_DEFAULT_PROFILE_IMAGE_URL:
            return
        prefix = settings.MEDIA_URL.rstrip("/") + "/"
        if image_url.startswith(prefix):
            relative_path = image_url[len(prefix) :]
            target = Path(settings.MEDIA_ROOT) / relative_path
            if target.exists():
                os.remove(target)
