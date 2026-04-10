from __future__ import annotations

from django.http import JsonResponse


def success_response(
    data: dict | list,
    *,
    status: int = 200,
    meta: dict | None = None,
) -> JsonResponse:
    payload = {
        "success": True,
        "data": data,
    }
    if meta is not None:
        payload["meta"] = meta
    return JsonResponse(payload, status=status)


def error_response(
    code: str,
    message: str,
    status: int,
    *,
    details: dict | None = None,
) -> JsonResponse:
    payload = {
        "success": False,
        "error": {
            "code": code,
            "message": message,
        },
    }
    if details:
        payload["error"]["details"] = details
    return JsonResponse(payload, status=status)
