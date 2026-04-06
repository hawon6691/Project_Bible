from django.db import models


class ProductStatus(models.TextChoices):
    ON_SALE = "ON_SALE", "On sale"
    SOLD_OUT = "SOLD_OUT", "Sold out"
    HIDDEN = "HIDDEN", "Hidden"


class SpecDefinitionType(models.TextChoices):
    TEXT = "TEXT", "Text"
    NUMBER = "NUMBER", "Number"
    SELECT = "SELECT", "Select"


class SpecDataType(models.TextChoices):
    NUMBER = "NUMBER", "Number"
    STRING = "STRING", "String"
    BOOLEAN = "BOOLEAN", "Boolean"


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


class SpecDefinition(models.Model):
    class Meta:
        db_table = "spec_definitions"
        indexes = [
            models.Index(fields=["category"], name="idx_spec_definitions_category"),
        ]

    category = models.ForeignKey(
        Category,
        on_delete=models.PROTECT,
        related_name="spec_definitions",
        db_index=False,
    )
    name = models.CharField(max_length=50)
    type = models.CharField(
        max_length=10,
        choices=SpecDefinitionType.choices,
    )
    options = models.JSONField(null=True, blank=True)
    unit = models.CharField(max_length=20, null=True, blank=True)
    is_comparable = models.BooleanField(default=True)
    data_type = models.CharField(
        max_length=10,
        choices=SpecDataType.choices,
        default=SpecDataType.STRING,
    )
    sort_order = models.IntegerField(default=0)
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self) -> str:
        return self.name


class ProductSpec(models.Model):
    class Meta:
        db_table = "product_specs"
        constraints = [
            models.UniqueConstraint(
                fields=["product", "spec_definition"],
                name="uq_product_specs",
            ),
        ]
        indexes = [
            models.Index(
                fields=["spec_definition", "value"],
                name="idx_product_specs_value",
            ),
        ]

    product = models.ForeignKey(
        Product,
        on_delete=models.CASCADE,
        related_name="product_specs",
    )
    spec_definition = models.ForeignKey(
        SpecDefinition,
        on_delete=models.CASCADE,
        related_name="product_specs",
    )
    value = models.CharField(max_length=200)
    numeric_value = models.DecimalField(
        max_digits=10,
        decimal_places=2,
        null=True,
        blank=True,
    )
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self) -> str:
        return f"{self.product_id}:{self.spec_definition_id}"


class ProductOption(models.Model):
    class Meta:
        db_table = "product_options"
        indexes = [
            models.Index(fields=["product"], name="idx_product_options_product"),
        ]

    product = models.ForeignKey(
        Product,
        on_delete=models.CASCADE,
        related_name="options",
        db_index=False,
    )
    name = models.CharField(max_length=50)
    values = models.JSONField()
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self) -> str:
        return f"{self.product_id}:{self.name}"


class ProductImage(models.Model):
    class Meta:
        db_table = "product_images"
        indexes = [
            models.Index(fields=["product"], name="idx_product_images_product"),
        ]

    product = models.ForeignKey(
        Product,
        on_delete=models.CASCADE,
        related_name="images",
        db_index=False,
    )
    url = models.CharField(max_length=500)
    is_main = models.BooleanField(default=False)
    sort_order = models.IntegerField(default=0)
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self) -> str:
        return f"{self.product_id}:{self.url}"

