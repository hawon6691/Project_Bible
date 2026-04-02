from django.db import models
from django.db.models import F

from apps.catalog.models import Product


class ShippingType(models.TextChoices):
    FREE = "FREE", "Free"
    PAID = "PAID", "Paid"
    CONDITIONAL = "CONDITIONAL", "Conditional"


class PriceTrend(models.TextChoices):
    RISING = "RISING", "Rising"
    FALLING = "FALLING", "Falling"
    STABLE = "STABLE", "Stable"


class PriceRecommendation(models.TextChoices):
    BUY_NOW = "BUY_NOW", "Buy now"
    BUY_SOON = "BUY_SOON", "Buy soon"
    WAIT = "WAIT", "Wait"
    HOLD = "HOLD", "Hold"


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


class PriceHistory(models.Model):
    class Meta:
        db_table = "price_history"
        constraints = [
            models.UniqueConstraint(
                fields=["product", "date"],
                name="uq_price_history",
            ),
        ]
        indexes = [
            models.Index(fields=["-date"], name="idx_price_hist_date"),
        ]

    product = models.ForeignKey(
        Product,
        on_delete=models.CASCADE,
        related_name="price_history_entries",
    )
    date = models.DateField()
    lowest_price = models.IntegerField()
    average_price = models.IntegerField()
    highest_price = models.IntegerField()
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self) -> str:
        return f"{self.product_id}:{self.date}"


class PriceAlert(models.Model):
    class Meta:
        db_table = "price_alerts"
        constraints = [
            models.UniqueConstraint(
                fields=["user", "product"],
                name="uq_price_alerts",
            ),
        ]
        indexes = [
            models.Index(
                fields=["is_active", "is_triggered"],
                name="idx_price_alert_state",
            ),
        ]

    user = models.ForeignKey(
        "users.CustomUser",
        on_delete=models.CASCADE,
        related_name="price_alerts",
    )
    product = models.ForeignKey(
        Product,
        on_delete=models.CASCADE,
        related_name="price_alerts",
    )
    target_price = models.IntegerField()
    is_triggered = models.BooleanField(default=False)
    triggered_at = models.DateTimeField(null=True, blank=True)
    is_active = models.BooleanField(default=True)
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self) -> str:
        return f"{self.user_id}:{self.product_id}"


class PricePrediction(models.Model):
    class Meta:
        db_table = "price_predictions"
        constraints = [
            models.UniqueConstraint(
                fields=["product", "prediction_date"],
                name="uq_price_predictions",
            ),
        ]
        indexes = [
            models.Index(fields=["prediction_date"], name="idx_price_pred_date"),
        ]

    product = models.ForeignKey(
        Product,
        on_delete=models.CASCADE,
        related_name="price_predictions",
    )
    prediction_date = models.DateField()
    predicted_price = models.IntegerField()
    confidence = models.DecimalField(max_digits=3, decimal_places=2)
    trend = models.CharField(
        max_length=10,
        choices=PriceTrend.choices,
    )
    trend_strength = models.DecimalField(max_digits=3, decimal_places=2)
    moving_avg_7d = models.IntegerField(null=True, blank=True)
    moving_avg_30d = models.IntegerField(null=True, blank=True)
    recommendation = models.CharField(
        max_length=10,
        choices=PriceRecommendation.choices,
    )
    seasonality_note = models.CharField(max_length=200, null=True, blank=True)
    calculated_at = models.DateTimeField()
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self) -> str:
        return f"{self.product_id}:{self.prediction_date}"
