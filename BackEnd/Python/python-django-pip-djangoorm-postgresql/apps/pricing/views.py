from apps.api.auth import require_auth
from apps.api.decorators import api_endpoint
from apps.api.responses import success_response
from apps.pricing.services import PriceEntryService, SellerService
from apps.users.models import UserRole

seller_service = SellerService()
price_entry_service = PriceEntryService()


@api_endpoint(["GET", "POST"])
def sellers_collection(request):
    if request.method == "GET":
        data, meta = seller_service.list_sellers(request.GET)
        return success_response(data, meta=meta)

    protected_view = require_auth([UserRole.ADMIN])(create_seller)
    return protected_view(request)


@api_endpoint(["GET", "PATCH", "DELETE"])
def seller_detail(request, seller_id: int):
    if request.method == "GET":
        return success_response(seller_service.get_seller(seller_id))

    if request.method == "PATCH":
        protected_view = require_auth([UserRole.ADMIN])(update_seller)
        return protected_view(request, seller_id=seller_id)

    protected_view = require_auth([UserRole.ADMIN])(delete_seller)
    return protected_view(request, seller_id=seller_id)


@api_endpoint(["GET", "POST"])
def product_prices_collection(request, product_id: int):
    if request.method == "GET":
        return success_response(price_entry_service.get_product_prices(product_id))

    protected_view = require_auth([UserRole.SELLER, UserRole.ADMIN])(create_price_entry)
    return protected_view(request, product_id=product_id)


@api_endpoint(["PATCH", "DELETE"])
def price_entry_detail(request, price_id: int):
    if request.method == "PATCH":
        protected_view = require_auth([UserRole.SELLER, UserRole.ADMIN])(update_price_entry)
        return protected_view(request, price_id=price_id)

    protected_view = require_auth([UserRole.ADMIN])(delete_price_entry)
    return protected_view(request, price_id=price_id)


def create_seller(request):
    return success_response(seller_service.create_seller(request.json_data), status=201)


def update_seller(request, seller_id: int):
    return success_response(seller_service.update_seller(seller_id, request.json_data))


def delete_seller(request, seller_id: int):
    return success_response(seller_service.delete_seller(seller_id))


def create_price_entry(request, product_id: int):
    return success_response(price_entry_service.create_price_entry(product_id, request.json_data), status=201)


def update_price_entry(request, price_id: int):
    return success_response(price_entry_service.update_price_entry(price_id, request.json_data))


def delete_price_entry(request, price_id: int):
    return success_response(price_entry_service.delete_price_entry(price_id))
