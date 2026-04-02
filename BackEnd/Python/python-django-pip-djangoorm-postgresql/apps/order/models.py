from django.db import models

from apps.catalog.models import Product
from apps.pricing.models import Seller


class OrderStatus(models.TextChoices):
    ORDER_PLACED = "ORDER_PLACED", "Order placed"
    PAYMENT_PENDING = "PAYMENT_PENDING", "Payment pending"
    PAYMENT_CONFIRMED = "PAYMENT_CONFIRMED", "Payment confirmed"
    PREPARING = "PREPARING", "Preparing"
    SHIPPING = "SHIPPING", "Shipping"
    DELIVERED = "DELIVERED", "Delivered"
    CONFIRMED = "CONFIRMED", "Confirmed"
    CANCELLED = "CANCELLED", "Cancelled"
    RETURN_REQUESTED = "RETURN_REQUESTED", "Return requested"
    RETURNED = "RETURNED", "Returned"


class Order(models.Model):
    class Meta:
        db_table = "orders"
        constraints = [
            models.UniqueConstraint(fields=["order_number"], name="uq_orders_number"),
        ]
        indexes = [
            models.Index(fields=["user"], name="idx_orders_user"),
            models.Index(fields=["status"], name="idx_orders_status"),
            models.Index(fields=["-created_at"], name="idx_orders_created"),
        ]

    user = models.ForeignKey(
        "users.CustomUser",
        on_delete=models.CASCADE,
        related_name="orders",
        db_index=False,
    )
    order_number = models.CharField(max_length=30)
    status = models.CharField(
        max_length=20,
        choices=OrderStatus.choices,
        default=OrderStatus.ORDER_PLACED,
    )
    total_amount = models.IntegerField()
    point_used = models.IntegerField(default=0)
    final_amount = models.IntegerField()
    recipient_name = models.CharField(max_length=50)
    recipient_phone = models.CharField(max_length=20)
    zip_code = models.CharField(max_length=10)
    address = models.CharField(max_length=200)
    address_detail = models.CharField(max_length=100, null=True, blank=True)
    memo = models.CharField(max_length=200, null=True, blank=True)
    version = models.IntegerField(default=1)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self) -> str:
        return self.order_number


class OrderItem(models.Model):
    class Meta:
        db_table = "order_items"
        indexes = [
            models.Index(fields=["order"], name="idx_order_items_order"),
            models.Index(fields=["product"], name="idx_order_items_prod"),
        ]

    order = models.ForeignKey(
        Order,
        on_delete=models.CASCADE,
        related_name="order_items",
        db_index=False,
    )
    product = models.ForeignKey(
        Product,
        on_delete=models.CASCADE,
        related_name="order_items",
        db_index=False,
    )
    seller = models.ForeignKey(
        Seller,
        on_delete=models.CASCADE,
        related_name="order_items",
    )
    product_name = models.CharField(max_length=200)
    seller_name = models.CharField(max_length=100)
    selected_options = models.CharField(max_length=200, null=True, blank=True)
    quantity = models.IntegerField()
    unit_price = models.IntegerField()
    total_price = models.IntegerField()
    is_reviewed = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self) -> str:
        return f"{self.order_id}:{self.product_id}:{self.seller_id}"

