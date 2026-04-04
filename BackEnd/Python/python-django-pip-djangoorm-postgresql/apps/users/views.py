from apps.api.auth import require_auth
from apps.api.decorators import api_endpoint
from apps.api.responses import success_response
from apps.users.models import UserRole
from apps.users.services import UserService

user_service = UserService()


@api_endpoint(["GET", "PATCH", "DELETE"])
@require_auth()
def users_me(request):
    if request.method == "GET":
        return success_response(user_service.get_me(request.auth_user))
    if request.method == "PATCH":
        return success_response(user_service.update_me(request.auth_user, request.json_data))
    return success_response(user_service.delete_me(request.auth_user))


@api_endpoint(["PATCH"])
@require_auth()
def users_me_profile(request):
    return success_response(user_service.update_profile(request.auth_user, request.json_data))


@api_endpoint(["POST", "DELETE"])
@require_auth()
def users_me_profile_image(request):
    if request.method == "POST":
        return success_response(user_service.upload_profile_image(request.auth_user, request.FILES.get("image")))
    return success_response(user_service.delete_profile_image(request.auth_user))


@api_endpoint(["GET"])
def users_profile(request, user_id: int):
    return success_response(user_service.get_profile(user_id))


@api_endpoint(["GET"])
@require_auth([UserRole.ADMIN])
def users_collection(request):
    data, meta = user_service.list_users(request.GET)
    return success_response(data, meta=meta)


@api_endpoint(["PATCH"])
@require_auth([UserRole.ADMIN])
def users_status(request, user_id: int):
    return success_response(user_service.update_status(user_id, request.json_data))
