from __future__ import annotations

import json
from functools import wraps

from django.conf import settings
from django.core.exceptions import ValidationError
from django.views.decorators.csrf import csrf_exempt

from apps.api.errors import ApiError
from apps.api.responses import error_response


def api_endpoint(methods: list[str] | tuple[str, ...]):
    method_set = {method.upper() for method in methods}

    def decorator(view_func):
        @csrf_exempt
        @wraps(view_func)
        def wrapped(request, *args, **kwargs):
            if request.method.upper() not in method_set:
                return error_response("METHOD_NOT_ALLOWED", "허용되지 않은 메서드입니다.", 405)

            request.json_data = {}
            if request.content_type and request.content_type.startswith("application/json"):
                raw_body = request.body.decode("utf-8") if request.body else ""
                if raw_body:
                    try:
                        request.json_data = json.loads(raw_body)
                    except json.JSONDecodeError as exc:
                        return error_response("VALIDATION_ERROR", "JSON 형식이 올바르지 않습니다.", 400)

            try:
                return view_func(request, *args, **kwargs)
            except ApiError as exc:
                return error_response(exc.code, exc.message, exc.status, details=exc.details)
            except ValidationError as exc:
                return error_response("VALIDATION_ERROR", exc.message or "입력값 검증 실패", 400)
            except Exception:
                if settings.DEBUG:
                    raise
                return error_response("INTERNAL_ERROR", "서버 내부 오류가 발생했습니다.", 500)

        return wrapped

    return decorator
