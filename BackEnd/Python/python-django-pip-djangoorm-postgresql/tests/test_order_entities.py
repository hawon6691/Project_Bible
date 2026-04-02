from django.contrib.auth import get_user_model
from django.db import IntegrityError, transaction
from django.test import TestCase

from apps.catalog.models import Category, Product
from apps.order.models import Order, OrderItem, OrderStatus
from apps.pricing.models import Seller


class OrderEntityTests(TestCase):
    def setUp(self):
        user_model = get_user_model()
        self.user = user_model.objects.create_user(
            email="order-user@example.com",
            password="pbshop-secret",
            name="Order User",
            phone="010-2222-3333",
            nickname="order-user",
        )
        self.category = Category.objects.create(name="Tablet")
        self.product = Product.objects.create(
            name="PBShop Tablet",
            description="Order entity test product.",
            price=550000,
            category=self.category,
        )
        self.seller = Seller.objects.create(
            name="PB Commerce Mall",
            url="https://commerce.example.com",
        )

    def test_order_number_must_be_unique(self):
        Order.objects.create(
            user=self.user,
            order_number="ORD-20260402-000001",
            total_amount=550000,
            final_amount=550000,
            recipient_name="PB User",
            recipient_phone="010-2222-3333",
            zip_code="12345",
            address="Seoul Street 1",
        )

        with self.assertRaises(IntegrityError):
            with transaction.atomic():
                Order.objects.create(
                    user=self.user,
                    order_number="ORD-20260402-000001",
                    total_amount=540000,
                    final_amount=540000,
                    recipient_name="PB User",
                    recipient_phone="010-2222-3333",
                    zip_code="12345",
                    address="Seoul Street 1",
                )

    def test_order_defaults_and_amounts_are_stored(self):
        order = Order.objects.create(
            user=self.user,
            order_number="ORD-20260402-000002",
            total_amount=560000,
            point_used=10000,
            final_amount=550000,
            recipient_name="PB User",
            recipient_phone="010-2222-3333",
            zip_code="12345",
            address="Seoul Street 1",
            memo="Leave at the door",
        )

        self.assertEqual(order.status, OrderStatus.ORDER_PLACED)
        self.assertEqual(order.point_used, 10000)
        self.assertEqual(order.final_amount, 550000)
        self.assertEqual(order.version, 1)

    def test_order_item_relationships_work(self):
        order = Order.objects.create(
            user=self.user,
            order_number="ORD-20260402-000003",
            total_amount=550000,
            final_amount=550000,
            recipient_name="PB User",
            recipient_phone="010-2222-3333",
            zip_code="12345",
            address="Seoul Street 1",
        )
        order_item = OrderItem.objects.create(
            order=order,
            product=self.product,
            seller=self.seller,
            product_name=self.product.name,
            seller_name=self.seller.name,
            selected_options="storage:256",
            quantity=2,
            unit_price=275000,
            total_price=550000,
        )

        self.assertEqual(order_item.order, order)
        self.assertEqual(order_item.product, self.product)
        self.assertEqual(order_item.seller, self.seller)
        self.assertFalse(order_item.is_reviewed)

