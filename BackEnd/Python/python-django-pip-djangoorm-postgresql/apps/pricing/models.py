from django.db import models
from django.db.models import F

from apps.catalog.models import Product


class ShippingType(models.TextChoices):
    FREE = "FREE", "Free"
    PAID = "PAID", "Paid"
    CONDITIONAL = "CONDITIONAL", "Conditional"


class Seller(models.Model):
    class Meta:
        db_table = "sellers"
        indexes = [
            models.Index(fields=["is_active"], name="idx_sellers_active"),
        ]

    name = models.CharField(max_length=100)
    url = models.CharField(max_length=500)
    logo_url = models.CharField(max_length=500, null=True, blank=True)
    trust_score = models.IntegerField(default=0)
    trust_grade = models.CharField(max_length=2, null=True, blank=True)
    description = models.CharField(max_length=200, null=True, blank=True)
    is_active = models.BooleanField(default=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self) -> str:
        return self.name


class PriceEntry(models.Model):
    class Meta:
        db_table = "price_entries"
        constraints = [
            models.UniqueConstraint(
                fields=["product", "seller"],
                name="uq_price_entries",
            ),
        ]
        indexes = [
            models.Index(
                fields=["product", "price"],
                name="idx_price_entry_prod_price",
            ),
        ]

    product = models.ForeignKey(
        Product,
        on_delete=models.CASCADE,
        related_name="price_entries",
    )
    seller = models.ForeignKey(
        Seller,
        on_delete=models.CASCADE,
        related_name="price_entries",
    )
    price = models.IntegerField()
    shipping_cost = models.IntegerField(default=0)
    shipping_info = models.CharField(max_length=100, null=True, blank=True)
    product_url = models.CharField(max_length=1000)
    shipping_fee = models.IntegerField(default=0)
    shipping_type = models.CharField(
        max_length=12,
        choices=ShippingType.choices,
        default=ShippingType.PAID,
    )
    total_price = models.GeneratedField(
        expression=F("price") + F("shipping_fee"),
        output_field=models.IntegerField(),
        db_persist=True,
    )
    click_count = models.IntegerField(default=0)
    is_available = models.BooleanField(default=True)
    crawled_at = models.DateTimeField(null=True, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self) -> str:
        return f"{self.product_id}:{self.seller_id}"
