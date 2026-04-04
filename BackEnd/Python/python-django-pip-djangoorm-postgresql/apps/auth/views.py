from django.conf import settings
from django.http import HttpResponseRedirect

from apps.api.auth import require_auth
from apps.api.decorators import api_endpoint
from apps.api.errors import ApiError
from apps.api.responses import success_response
from apps.auth.services import AuthService

auth_service = AuthService()


@api_endpoint(["POST"])
def auth_signup(request):
    return success_response(auth_service.signup(request.json_data), status=201)


@api_endpoint(["POST"])
def auth_verify_email(request):
    return success_response(auth_service.verify_email(request.json_data))


@api_endpoint(["POST"])
def auth_resend_verification(request):
    return success_response(auth_service.resend_verification(request.json_data))


@api_endpoint(["POST"])
def auth_login(request):
    data, _user = auth_service.login(request.json_data)
    return success_response(data)


@api_endpoint(["POST"])
@require_auth()
def auth_logout(request):
    return success_response(auth_service.logout(request.auth_user))


@api_endpoint(["POST"])
def auth_refresh(request):
    data, _user = auth_service.refresh(request.json_data)
    return success_response(data)


@api_endpoint(["POST"])
def auth_password_reset_request(request):
    return success_response(auth_service.request_password_reset(request.json_data))


@api_endpoint(["POST"])
def auth_password_reset_verify(request):
    return success_response(auth_service.verify_reset_code(request.json_data))


@api_endpoint(["POST"])
def auth_password_reset_confirm(request):
    return success_response(auth_service.reset_password(request.json_data))


@api_endpoint(["GET"])
def auth_login_provider(request, provider: str):
    _state_token, redirect_url = auth_service.get_social_auth_redirect(provider)
    return HttpResponseRedirect(redirect_url)


@api_endpoint(["GET"])
def auth_callback(request, provider: str):
    code = request.GET.get("code", "")
    state = request.GET.get("state", "")
    data, signup_token = auth_service.social_callback(provider, code, state)
    response = success_response(data)
    if signup_token:
        response.set_cookie(
            settings.PBSHOP_SOCIAL_SIGNUP_COOKIE_NAME,
            signup_token,
            httponly=True,
            samesite="Lax",
        )
    return response


@api_endpoint(["POST"])
def auth_social_complete(request):
    signup_token = request.COOKIES.get(settings.PBSHOP_SOCIAL_SIGNUP_COOKIE_NAME)
    if not signup_token:
        raise ApiError("INVALID_SOCIAL_SIGNUP_TOKEN", "소셜 가입 세션이 없습니다.", 401)
    data, _user = auth_service.complete_social_signup(signup_token, request.json_data)
    response = success_response(data)
    response.delete_cookie(settings.PBSHOP_SOCIAL_SIGNUP_COOKIE_NAME)
    return response


@api_endpoint(["POST"])
@require_auth()
def auth_social_link(request):
    return success_response(auth_service.link_social_account(request.auth_user, request.json_data))


@api_endpoint(["DELETE"])
@require_auth()
def auth_social_unlink(request, provider: str):
    return success_response(auth_service.unlink_social_account(request.auth_user, provider))
