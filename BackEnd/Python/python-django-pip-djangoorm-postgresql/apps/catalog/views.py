from apps.api.auth import require_auth
from apps.api.decorators import api_endpoint
from apps.api.responses import success_response
from apps.catalog.services import CategoryService, ProductService, SpecService
from apps.users.models import UserRole

category_service = CategoryService()
product_service = ProductService()
spec_service = SpecService()


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


@api_endpoint(["GET", "POST"])
def spec_definitions_collection(request):
    if request.method == "GET":
        return success_response(spec_service.list_definitions(request.GET))

    protected_view = require_auth([UserRole.ADMIN])(create_spec_definition)
    return protected_view(request)


@api_endpoint(["PATCH", "DELETE"])
def spec_definition_detail(request, definition_id: int):
    if request.method == "PATCH":
        protected_view = require_auth([UserRole.ADMIN])(update_spec_definition)
        return protected_view(request, definition_id=definition_id)

    protected_view = require_auth([UserRole.ADMIN])(delete_spec_definition)
    return protected_view(request, definition_id=definition_id)


@api_endpoint(["POST"])
def spec_compare(request):
    return success_response(spec_service.compare_products(request.json_data))


@api_endpoint(["GET", "PUT"])
def product_spec_collection(request, product_id: int):
    if request.method == "GET":
        return success_response(spec_service.get_product_specs(product_id))

    protected_view = require_auth([UserRole.ADMIN])(replace_product_specs)
    return protected_view(request, product_id=product_id)


@api_endpoint(["GET", "POST"])
def products_collection(request):
    if request.method == "GET":
        data, meta = product_service.list_products(request.GET)
        return success_response(data, meta=meta)

    protected_view = require_auth([UserRole.ADMIN])(create_product)
    return protected_view(request)


@api_endpoint(["GET", "PATCH", "DELETE"])
def product_detail(request, product_id: int):
    if request.method == "GET":
        return success_response(product_service.get_product(product_id))

    if request.method == "PATCH":
        protected_view = require_auth([UserRole.ADMIN])(update_product)
        return protected_view(request, product_id=product_id)

    protected_view = require_auth([UserRole.ADMIN])(delete_product)
    return protected_view(request, product_id=product_id)


@api_endpoint(["POST"])
def product_option_collection(request, product_id: int):
    protected_view = require_auth([UserRole.ADMIN])(create_product_option)
    return protected_view(request, product_id=product_id)


@api_endpoint(["PATCH", "DELETE"])
def product_option_detail(request, product_id: int, option_id: int):
    if request.method == "PATCH":
        protected_view = require_auth([UserRole.ADMIN])(update_product_option)
        return protected_view(request, product_id=product_id, option_id=option_id)

    protected_view = require_auth([UserRole.ADMIN])(delete_product_option)
    return protected_view(request, product_id=product_id, option_id=option_id)


@api_endpoint(["POST"])
def product_image_collection(request, product_id: int):
    protected_view = require_auth([UserRole.ADMIN])(upload_product_image)
    return protected_view(request, product_id=product_id)


@api_endpoint(["DELETE"])
def product_image_detail(request, product_id: int, image_id: int):
    protected_view = require_auth([UserRole.ADMIN])(delete_product_image)
    return protected_view(request, product_id=product_id, image_id=image_id)


def create_product(request):
    return success_response(product_service.create_product(request.json_data), status=201)


def update_product(request, product_id: int):
    return success_response(product_service.update_product(product_id, request.json_data))


def delete_product(request, product_id: int):
    return success_response(product_service.delete_product(product_id))


def create_product_option(request, product_id: int):
    return success_response(product_service.add_option(product_id, request.json_data), status=201)


def update_product_option(request, product_id: int, option_id: int):
    return success_response(product_service.update_option(product_id, option_id, request.json_data))


def delete_product_option(request, product_id: int, option_id: int):
    return success_response(product_service.delete_option(product_id, option_id))


def upload_product_image(request, product_id: int):
    return success_response(product_service.upload_image(product_id, request.FILES.get("image"), request.POST), status=201)


def delete_product_image(request, product_id: int, image_id: int):
    return success_response(product_service.delete_image(product_id, image_id))


def create_spec_definition(request):
    return success_response(spec_service.create_definition(request.json_data), status=201)


def update_spec_definition(request, definition_id: int):
    return success_response(spec_service.update_definition(definition_id, request.json_data))


def delete_spec_definition(request, definition_id: int):
    return success_response(spec_service.delete_definition(definition_id))


def replace_product_specs(request, product_id: int):
    return success_response(spec_service.replace_product_specs(product_id, request.json_data))
