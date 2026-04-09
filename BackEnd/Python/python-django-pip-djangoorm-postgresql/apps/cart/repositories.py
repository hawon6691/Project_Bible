from __future__ import annotations

from django.db.models import QuerySet

from apps.cart.models import CartItem


class CartRepository:
    def list_by_user(self, user_id: int) -> QuerySet[CartItem]:
        return (
            CartItem.objects.select_related("product", "seller")
            .filter(user_id=user_id)
            .order_by("-updated_at", "-id")
        )

    def find_by_user_and_identity(
        self,
        *,
        user_id: int,
        product_id: int,
        seller_id: int,
        selected_options: str,
    ) -> CartItem | None:
        return (
            CartItem.objects.select_related("product", "seller")
            .filter(
                user_id=user_id,
                product_id=product_id,
                seller_id=seller_id,
                selected_options=selected_options,
            )
            .first()
        )

    def find_by_user_and_id(self, *, user_id: int, item_id: int) -> CartItem | None:
        return (
            CartItem.objects.select_related("product", "seller")
            .filter(user_id=user_id, id=item_id)
            .first()
        )

    def create(self, **kwargs) -> CartItem:
        return CartItem.objects.create(**kwargs)

    def save(self, cart_item: CartItem, *, update_fields: list[str] | None = None) -> CartItem:
        cart_item.save(update_fields=update_fields)
        return cart_item

    def delete(self, cart_item: CartItem) -> None:
        cart_item.delete()

    def delete_all_by_user(self, user_id: int) -> int:
        deleted_count, _ = CartItem.objects.filter(user_id=user_id).delete()
        return deleted_count
