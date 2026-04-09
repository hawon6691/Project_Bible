from __future__ import annotations

from django.db import transaction

from apps.api.errors import ApiError
from apps.api.utils import require_fields
from apps.cart.models import CartItem
from apps.cart.repositories import CartRepository
from apps.catalog.models import ProductStatus
from apps.catalog.repositories import ProductRepository
from apps.pricing.repositories import SellerRepository


class CartService:
    def __init__(
        self,
        cart_repository: CartRepository | None = None,
        product_repository: ProductRepository | None = None,
        seller_repository: SellerRepository | None = None,
    ) -> None:
        self.cart_repository = cart_repository or CartRepository()
        self.product_repository = product_repository or ProductRepository()
        self.seller_repository = seller_repository or SellerRepository()

    def list_items(self, user) -> list[dict]:
        return [self._serialize_item(item) for item in self.cart_repository.list_by_user(user.id)]

    def add_item(self, user, data: dict) -> dict:
        if not isinstance(data, dict):
            raise ApiError("VALIDATION_ERROR", "요청 본문은 객체여야 합니다.", 400)

        require_fields(data, ["productId", "sellerId", "quantity"])
        product = self._resolve_product(data.get("productId"))
        seller = self._resolve_seller(data.get("sellerId"))
        quantity = self._parse_positive_int(data.get("quantity"), "quantity")
        selected_options = self._normalize_selected_options(data.get("selectedOptions"))

        existing = self.cart_repository.find_by_user_and_identity(
            user_id=user.id,
            product_id=product.id,
            seller_id=seller.id,
            selected_options=selected_options,
        )

        with transaction.atomic():
            if existing is not None:
                existing.quantity += quantity
                self.cart_repository.save(existing, update_fields=["quantity", "updated_at"])
                return self._serialize_item(existing)

            item = self.cart_repository.create(
                user=user,
                product=product,
                seller=seller,
                selected_options=selected_options,
                quantity=quantity,
            )
            return self._serialize_item(item)

    def update_quantity(self, user, item_id: int, data: dict) -> dict:
        if not isinstance(data, dict):
            raise ApiError("VALIDATION_ERROR", "요청 본문은 객체여야 합니다.", 400)
        require_fields(data, ["quantity"])

        item = self._get_owned_item(user.id, item_id)
        item.quantity = self._parse_positive_int(data.get("quantity"), "quantity")
        self.cart_repository.save(item, update_fields=["quantity", "updated_at"])
        return self._serialize_item(item)

    def delete_item(self, user, item_id: int) -> dict:
        item = self._get_owned_item(user.id, item_id)
        self.cart_repository.delete(item)
        return {"message": "장바구니 항목이 삭제되었습니다."}

    def clear_items(self, user) -> dict:
        self.cart_repository.delete_all_by_user(user.id)
        return {"message": "장바구니가 비워졌습니다."}

    def _get_owned_item(self, user_id: int, item_id: int) -> CartItem:
        item = self.cart_repository.find_by_user_and_id(user_id=user_id, item_id=item_id)
        if item is None:
            raise ApiError("CART_ITEM_NOT_FOUND", "장바구니 항목을 찾을 수 없습니다.", 404)
        return item

    def _resolve_product(self, raw_product_id):
        product_id = self._parse_positive_int(raw_product_id, "productId")
        product = self.product_repository.find_by_id(product_id)
        if product is None:
            raise ApiError("PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다.", 404)
        if product.status != ProductStatus.ON_SALE:
            raise ApiError("PRODUCT_NOT_AVAILABLE", "현재 장바구니에 담을 수 없는 상품입니다.", 400)
        return product

    def _resolve_seller(self, raw_seller_id):
        seller_id = self._parse_positive_int(raw_seller_id, "sellerId")
        seller = self.seller_repository.find_active_by_id(seller_id)
        if seller is None:
            raise ApiError("SELLER_NOT_FOUND", "활성 판매처를 찾을 수 없습니다.", 404)
        return seller

    def _parse_positive_int(self, value, field_name: str) -> int:
        try:
            parsed = int(value)
        except (TypeError, ValueError) as exc:
            raise ApiError("VALIDATION_ERROR", f"{field_name}는 정수여야 합니다.", 400) from exc
        if parsed < 1:
            raise ApiError("VALIDATION_ERROR", f"{field_name}는 1 이상이어야 합니다.", 400)
        return parsed

    def _normalize_selected_options(self, value) -> str:
        if value in (None, ""):
            return ""
        normalized = str(value).strip()
        if len(normalized) > 200:
            raise ApiError("VALIDATION_ERROR", "selectedOptions는 200자 이하여야 합니다.", 400)
        return normalized

    def _display_price(self, item: CartItem) -> int:
        return item.product.lowest_price if item.product.lowest_price is not None else item.product.price

    def _serialize_item(self, item: CartItem) -> dict:
        unit_price = self._display_price(item)
        return {
            "id": item.id,
            "quantity": item.quantity,
            "selectedOptions": item.selected_options,
            "createdAt": item.created_at.isoformat().replace("+00:00", "Z"),
            "updatedAt": item.updated_at.isoformat().replace("+00:00", "Z"),
            "product": {
                "id": item.product.id,
                "name": item.product.name,
                "thumbnailUrl": item.product.thumbnail_url,
                "status": item.product.status,
                "stock": item.product.stock,
                "price": item.product.price,
                "lowestPrice": unit_price,
            },
            "seller": {
                "id": item.seller.id,
                "name": item.seller.name,
                "logoUrl": item.seller.logo_url,
                "trustScore": item.seller.trust_score,
            },
            "unitPrice": unit_price,
            "subtotal": unit_price * item.quantity,
        }
