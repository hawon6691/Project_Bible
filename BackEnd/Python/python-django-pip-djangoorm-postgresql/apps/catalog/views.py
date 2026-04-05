from apps.api.auth import require_auth
from apps.api.decorators import api_endpoint
from apps.api.responses import success_response
from apps.catalog.services import CategoryService
from apps.users.models import UserRole

category_service = CategoryService()


@api_endpoint(["GET", "POST"])
def categories_collection(request):
    if request.method == "GET":
        return success_response(category_service.list_tree())

    protected_view = require_auth([UserRole.ADMIN])(create_category)
    return protected_view(request)


@api_endpoint(["GET", "PATCH", "DELETE"])
def category_detail(request, category_id: int):
    if request.method == "GET":
        return success_response(category_service.get_category(category_id))

    if request.method == "PATCH":
        protected_view = require_auth([UserRole.ADMIN])(update_category)
        return protected_view(request, category_id=category_id)

    protected_view = require_auth([UserRole.ADMIN])(delete_category)
    return protected_view(request, category_id=category_id)


def create_category(request):
    return success_response(category_service.create_category(request.json_data), status=201)


def update_category(request, category_id: int):
    return success_response(category_service.update_category(category_id, request.json_data))


def delete_category(request, category_id: int):
    return success_response(category_service.delete_category(category_id))
