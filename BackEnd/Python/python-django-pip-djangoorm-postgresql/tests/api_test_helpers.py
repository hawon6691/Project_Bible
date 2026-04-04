import json

from apps.api.auth import issue_access_token


def json_request(client, method: str, path: str, payload: dict | None = None, **extra):
    data = json.dumps(payload or {})
    return getattr(client, method.lower())(
        path,
        data=data,
        content_type="application/json",
        **extra,
    )


def auth_header_for(user):
    return {
        "HTTP_AUTHORIZATION": f"Bearer {issue_access_token(user)}",
    }
