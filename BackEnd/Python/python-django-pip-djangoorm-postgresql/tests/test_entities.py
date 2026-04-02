from decimal import Decimal

from django.contrib.auth import get_user_model
from django.db import IntegrityError, transaction
from django.test import TestCase

from apps.catalog.models import Category, Product, ProductStatus
from apps.users.models import UserRole, UserStatus


class CustomUserModelTests(TestCase):
    def test_create_user_uses_email_as_login_identifier(self):
        user_model = get_user_model()
        user = user_model.objects.create_user(
            email="user@example.com",
            password="pbshop-secret",
            name="PB User",
            phone="010-1234-5678",
            nickname="pb-user",
        )

        self.assertEqual(user_model.USERNAME_FIELD, "email")
        self.assertEqual(user.get_username(), "user@example.com")
        self.assertEqual(user.role, UserRole.USER)
        self.assertEqual(user.status, UserStatus.ACTIVE)
        self.assertTrue(user.check_password("pbshop-secret"))

    def test_email_and_nickname_must_be_unique(self):
        user_model = get_user_model()
        user_model.objects.create_user(
            email="first@example.com",
            password="pbshop-secret",
            name="First User",
            phone="010-0000-0001",
            nickname="dup-nickname",
        )

        with self.assertRaises(IntegrityError):
            with transaction.atomic():
                user_model.objects.create_user(
                    email="first@example.com",
                    password="pbshop-secret",
                    name="Second User",
                    phone="010-0000-0002",
                    nickname="second-nickname",
                )

        with self.assertRaises(IntegrityError):
            with transaction.atomic():
                user_model.objects.create_user(
                    email="second@example.com",
                    password="pbshop-secret",
                    name="Third User",
                    phone="010-0000-0003",
                    nickname="dup-nickname",
                )


class CatalogModelTests(TestCase):
    def test_category_can_reference_parent_category(self):
        root = Category.objects.create(name="Electronics")
        child = Category.objects.create(name="Phone", parent=root, sort_order=1)

        self.assertEqual(child.parent, root)
        self.assertEqual(root.children.count(), 1)

    def test_product_belongs_to_category_and_uses_default_metrics(self):
        category = Category.objects.create(name="Books")
        product = Product.objects.create(
            name="PBShop Django Guide",
            description="Bootstrap product for entity tests.",
            price=35000,
            category=category,
        )

        self.assertEqual(product.category, category)
        self.assertEqual(product.status, ProductStatus.ON_SALE)
        self.assertEqual(product.stock, 0)
        self.assertEqual(product.seller_count, 0)
        self.assertEqual(product.view_count, 0)
        self.assertEqual(product.review_count, 0)
        self.assertEqual(product.sales_count, 0)
        self.assertEqual(product.average_rating, Decimal("0.0"))
        self.assertEqual(product.popularity_score, Decimal("0"))
