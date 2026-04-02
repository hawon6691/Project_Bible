from django.contrib.auth import get_user_model
from django.db import IntegrityError, transaction
from django.test import TestCase

from apps.cart.models import CartItem
from apps.catalog.models import Category, Product
from apps.pricing.models import Seller


class CartEntityTests(TestCase):
    def setUp(self):
        user_model = get_user_model()
        self.user = user_model.objects.create_user(
            email="cart-user@example.com",
            password="pbshop-secret",
            name="Cart User",
            phone="010-2222-3333",
            nickname="cart-user",
        )
        self.category = Category.objects.create(name="Tablet")
        self.product = Product.objects.create(
            name="PBShop Tablet",
            description="Cart entity test product.",
            price=550000,
            category=self.category,
        )
        self.seller = Seller.objects.create(
            name="PB Commerce Mall",
            url="https://commerce.example.com",
        )

    def test_cart_item_unique_constraint(self):
        CartItem.objects.create(
            user=self.user,
            product=self.product,
            seller=self.seller,
            selected_options="color:black",
        )

        with self.assertRaises(IntegrityError):
            with transaction.atomic():
                CartItem.objects.create(
                    user=self.user,
                    product=self.product,
                    seller=self.seller,
                    selected_options="color:black",
                )

    def test_cart_item_quantity_defaults_to_one(self):
        cart_item = CartItem.objects.create(
            user=self.user,
            product=self.product,
            seller=self.seller,
        )

        self.assertEqual(cart_item.quantity, 1)

