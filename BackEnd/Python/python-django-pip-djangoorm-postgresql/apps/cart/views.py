from apps.api.auth import require_auth
from apps.api.decorators import api_endpoint
from apps.api.responses import success_response
from apps.cart.services import CartService
from apps.users.models import UserRole


cart_service = CartService()
CART_ROLES = [UserRole.USER, UserRole.SELLER, UserRole.ADMIN]


@api_endpoint(["GET", "POST", "DELETE"])
def cart_collection(request):
    protected_view = require_auth(CART_ROLES)(_cart_collection)
    return protected_view(request)


@api_endpoint(["PATCH", "DELETE"])
def cart_item_detail(request, item_id: int):
    protected_view = require_auth(CART_ROLES)(_cart_item_detail)
    return protected_view(request, item_id=item_id)


def _cart_collection(request):
    if request.method == "GET":
        return success_response(cart_service.list_items(request.auth_user))
    if request.method == "POST":
        return success_response(cart_service.add_item(request.auth_user, request.json_data), status=201)
    return success_response(cart_service.clear_items(request.auth_user))


def _cart_item_detail(request, item_id: int):
    if request.method == "PATCH":
        return success_response(cart_service.update_quantity(request.auth_user, item_id, request.json_data))
    return success_response(cart_service.delete_item(request.auth_user, item_id))
