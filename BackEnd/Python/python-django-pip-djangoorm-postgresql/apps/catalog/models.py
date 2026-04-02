from django.db import models


class ProductStatus(models.TextChoices):
    ON_SALE = "ON_SALE", "On sale"
    SOLD_OUT = "SOLD_OUT", "Sold out"
    HIDDEN = "HIDDEN", "Hidden"


class Category(models.Model):
    class Meta:
        db_table = "categories"
        indexes = [
            models.Index(fields=["parent"], name="idx_categories_parent"),
        ]

    name = models.CharField(max_length=50)
    parent = models.ForeignKey(
        "self",
        null=True,
        blank=True,
        on_delete=models.SET_NULL,
        related_name="children",
        db_index=False,
    )
    sort_order = models.IntegerField(default=0)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self) -> str:
        return self.name


class Product(models.Model):
    class Meta:
        db_table = "products"
        indexes = [
            models.Index(fields=["category"], name="idx_products_category"),
            models.Index(fields=["status"], name="idx_products_status"),
            models.Index(fields=["lowest_price"], name="idx_products_lowest_price"),
            models.Index(fields=["-created_at"], name="idx_products_created"),
            models.Index(fields=["-view_count"], name="idx_products_view_count"),
            models.Index(fields=["popularity_score"], name="idx_products_popularity"),
            models.Index(fields=["name"], name="idx_products_name"),
        ]

    name = models.CharField(max_length=200)
    description = models.TextField()
    price = models.IntegerField()
    discount_price = models.IntegerField(null=True, blank=True)
    stock = models.IntegerField(default=0)
    status = models.CharField(
        max_length=10,
        choices=ProductStatus.choices,
        default=ProductStatus.ON_SALE,
    )
    category = models.ForeignKey(
        Category,
        on_delete=models.PROTECT,
        related_name="products",
        db_index=False,
    )
    thumbnail_url = models.CharField(max_length=500, null=True, blank=True)
    lowest_price = models.IntegerField(null=True, blank=True)
    seller_count = models.IntegerField(default=0)
    view_count = models.IntegerField(default=0)
    review_count = models.IntegerField(default=0)
    average_rating = models.DecimalField(max_digits=2, decimal_places=1, default=0.0)
    sales_count = models.IntegerField(default=0)
    popularity_score = models.DecimalField(max_digits=10, decimal_places=2, default=0)
    version = models.IntegerField(default=1)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    deleted_at = models.DateTimeField(null=True, blank=True)

    def __str__(self) -> str:
        return self.name

