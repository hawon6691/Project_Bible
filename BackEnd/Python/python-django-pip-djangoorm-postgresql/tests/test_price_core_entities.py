from decimal import Decimal

from django.db import IntegrityError, transaction
from django.test import TestCase

from apps.catalog.models import (
    Category,
    Product,
    ProductSpec,
    SpecDataType,
    SpecDefinition,
    SpecDefinitionType,
)
from apps.pricing.models import PriceEntry, Seller, ShippingType


class PriceCoreEntityTests(TestCase):
    def setUp(self):
        self.category = Category.objects.create(name="Laptop")
        self.product = Product.objects.create(
            name="PBShop Laptop",
            description="Price core entity test product.",
            price=1500000,
            category=self.category,
        )

    def test_spec_definition_belongs_to_category(self):
        spec = SpecDefinition.objects.create(
            category=self.category,
            name="RAM",
            type=SpecDefinitionType.SELECT,
            options=["16GB", "32GB"],
            unit="GB",
            is_comparable=True,
            data_type=SpecDataType.STRING,
            sort_order=1,
        )

        self.assertEqual(spec.category, self.category)
        self.assertEqual(self.category.spec_definitions.count(), 1)

    def test_product_spec_unique_constraint(self):
        spec_definition = SpecDefinition.objects.create(
            category=self.category,
            name="Weight",
            type=SpecDefinitionType.NUMBER,
            unit="kg",
            data_type=SpecDataType.NUMBER,
        )
        ProductSpec.objects.create(
            product=self.product,
            spec_definition=spec_definition,
            value="1.45",
            numeric_value=Decimal("1.45"),
        )

        with self.assertRaises(IntegrityError):
            with transaction.atomic():
                ProductSpec.objects.create(
                    product=self.product,
                    spec_definition=spec_definition,
                    value="1.50",
                    numeric_value=Decimal("1.50"),
                )

    def test_product_spec_stores_value_and_numeric_value(self):
        spec_definition = SpecDefinition.objects.create(
            category=self.category,
            name="Battery",
            type=SpecDefinitionType.NUMBER,
            unit="Wh",
            data_type=SpecDataType.NUMBER,
        )
        product_spec = ProductSpec.objects.create(
            product=self.product,
            spec_definition=spec_definition,
            value="80",
            numeric_value=Decimal("80.00"),
        )

        self.assertEqual(product_spec.value, "80")
        self.assertEqual(product_spec.numeric_value, Decimal("80.00"))

    def test_seller_defaults_to_active(self):
        seller = Seller.objects.create(
            name="PB Mall",
            url="https://mall.example.com",
        )

        self.assertTrue(seller.is_active)
        self.assertEqual(seller.trust_score, 0)

    def test_price_entry_unique_constraint(self):
        seller = Seller.objects.create(
            name="PB Price",
            url="https://price.example.com",
        )
        PriceEntry.objects.create(
            product=self.product,
            seller=seller,
            price=1450000,
            product_url="https://price.example.com/products/1",
        )

        with self.assertRaises(IntegrityError):
            with transaction.atomic():
                PriceEntry.objects.create(
                    product=self.product,
                    seller=seller,
                    price=1440000,
                    product_url="https://price.example.com/products/1-alt",
                )

    def test_price_entry_total_price_is_generated_and_relationships_work(self):
        seller = Seller.objects.create(
            name="PB Compare",
            url="https://compare.example.com",
            is_active=False,
        )
        price_entry = PriceEntry.objects.create(
            product=self.product,
            seller=seller,
            price=1400000,
            shipping_cost=3000,
            shipping_info="CJ Logistics",
            product_url="https://compare.example.com/products/1",
            shipping_fee=2500,
            shipping_type=ShippingType.PAID,
        )

        price_entry.refresh_from_db()

        self.assertEqual(price_entry.product, self.product)
        self.assertEqual(price_entry.seller, seller)
        self.assertEqual(price_entry.total_price, 1402500)
        self.assertEqual(price_entry.shipping_type, ShippingType.PAID)
