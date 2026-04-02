from django.db import models


class Address(models.Model):
    class Meta:
        db_table = "addresses"
        indexes = [
            models.Index(fields=["user"], name="idx_addresses_user"),
        ]

    user = models.ForeignKey(
        "users.CustomUser",
        on_delete=models.CASCADE,
        related_name="addresses",
        db_index=False,
    )
    label = models.CharField(max_length=50)
    recipient_name = models.CharField(max_length=50)
    phone = models.CharField(max_length=20)
    zip_code = models.CharField(max_length=10)
    address = models.CharField(max_length=200)
    address_detail = models.CharField(max_length=100, null=True, blank=True)
    is_default = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self) -> str:
        return f"{self.user_id}:{self.label}"

