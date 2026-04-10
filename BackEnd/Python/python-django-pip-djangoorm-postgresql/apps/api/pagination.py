from __future__ import annotations

from apps.api.errors import ApiError


def parse_pagination(querydict) -> tuple[int, int]:
    try:
        page = int(querydict.get("page", "1"))
        limit = int(querydict.get("limit", "20"))
    except (TypeError, ValueError) as exc:
        raise ApiError("VALIDATION_ERROR", "page와 limit는 숫자여야 합니다.", 400) from exc

    if page < 1:
        raise ApiError("VALIDATION_ERROR", "page는 1 이상이어야 합니다.", 400)
    if limit < 1 or limit > 100:
        raise ApiError("VALIDATION_ERROR", "limit는 1 이상 100 이하여야 합니다.", 400)

    return page, limit


def build_pagination_meta(*, page: int, limit: int, total_count: int) -> dict:
    total_pages = total_count // limit + (1 if total_count % limit else 0)
    return {
        "page": page,
        "limit": limit,
        "totalCount": total_count,
        "totalPages": total_pages,
    }
