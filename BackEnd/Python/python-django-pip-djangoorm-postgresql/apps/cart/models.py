from django.db import models

from apps.catalog.models import Product
from apps.pricing.models import Seller


class CartItem(models.Model):
    class Meta:
        db_table = "cart_items"
        constraints = [
            models.UniqueConstraint(
                fields=["user", "product", "seller", "selected_options"],
                name="uq_cart_items",
            ),
        ]
        indexes = [
            models.Index(fields=["user"], name="idx_cart_items_user"),
        ]

    user = models.ForeignKey(
        "users.CustomUser",
        on_delete=models.CASCADE,
        related_name="cart_items",
        db_index=False,
    )
    product = models.ForeignKey(
        Product,
        on_delete=models.CASCADE,
        related_name="cart_items",
    )
    seller = models.ForeignKey(
        Seller,
        on_delete=models.CASCADE,
        related_name="cart_items",
    )
    selected_options = models.CharField(max_length=200, blank=True, default="")
    quantity = models.IntegerField(default=1)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self) -> str:
        return f"{self.user_id}:{self.product_id}:{self.seller_id}"

