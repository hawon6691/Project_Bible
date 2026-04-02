from django.db import models

from apps.order.models import Order


class PaymentMethod(models.TextChoices):
    CARD = "CARD", "Card"
    BANK_TRANSFER = "BANK_TRANSFER", "Bank transfer"
    VIRTUAL_ACCOUNT = "VIRTUAL_ACCOUNT", "Virtual account"


class PaymentStatus(models.TextChoices):
    PENDING = "PENDING", "Pending"
    COMPLETED = "COMPLETED", "Completed"
    FAILED = "FAILED", "Failed"
    REFUNDED = "REFUNDED", "Refunded"


class Payment(models.Model):
    class Meta:
        db_table = "payments"
        indexes = [
            models.Index(fields=["order"], name="idx_payments_order"),
        ]

    order = models.ForeignKey(
        Order,
        on_delete=models.CASCADE,
        related_name="payments",
        db_index=False,
    )
    method = models.CharField(max_length=20, choices=PaymentMethod.choices)
    amount = models.IntegerField()
    status = models.CharField(
        max_length=10,
        choices=PaymentStatus.choices,
        default=PaymentStatus.PENDING,
    )
    paid_at = models.DateTimeField(null=True, blank=True)
    refunded_at = models.DateTimeField(null=True, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self) -> str:
        return f"{self.order_id}:{self.method}"

