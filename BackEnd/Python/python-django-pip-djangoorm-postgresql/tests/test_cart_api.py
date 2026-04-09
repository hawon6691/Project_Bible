from datetime import timedelta

from django.contrib.auth import get_user_model
from django.test import Client, TestCase
from django.utils import timezone

from apps.cart.models import CartItem
from apps.catalog.models import Category, Product, ProductStatus
from apps.pricing.models import Seller
from apps.users.models import UserRole
from tests.api_test_helpers import auth_header_for, json_request


class CartApiTests(TestCase):
    def setUp(self):
        self.client = Client()
        user_model = get_user_model()

        self.user = user_model.objects.create_user(
            email="cart-api-user@example.com",
            password="Password1!",
            name="Cart User",
            phone="01011112222",
            nickname="cart-api-user",
        )
        self.user.email_verified = True
        self.user.save(update_fields=["email_verified", "updated_at"])

        self.other_user = user_model.objects.create_user(
            email="cart-api-other@example.com",
            password="Password1!",
            name="Other User",
            phone="01033334444",
            nickname="cart-api-other",
            role=UserRole.SELLER,
        )
        self.other_user.email_verified = True
        self.other_user.save(update_fields=["email_verified", "updated_at"])

        self.category = Category.objects.create(name="Laptop")
        self.product = Product.objects.create(
            name="Cart Laptop",
            description="Cart API product",
            price=1500000,
            lowest_price=1390000,
            stock=10,
            category=self.category,
        )
        self.hidden_product = Product.objects.create(
            name="Hidden Product",
            description="Hidden",
            price=500000,
            stock=2,
            status=ProductStatus.HIDDEN,
            category=self.category,
        )
        self.sold_out_product = Product.objects.create(
            name="Soldout Product",
            description="Sold out",
            price=800000,
            stock=0,
            status=ProductStatus.SOLD_OUT,
            category=self.category,
        )
        self.seller = Seller.objects.create(
            name="PB Mall",
            url="https://mall.example.com",
            logo_url="/logos/pb.png",
            trust_score=91,
        )
        self.inactive_seller = Seller.objects.create(
            name="Inactive Mall",
            url="https://inactive.example.com",
            is_active=False,
        )

    def test_list_cart_items_orders_by_updated_at_and_hides_other_users(self):
        first = CartItem.objects.create(
            user=self.user,
            product=self.product,
            seller=self.seller,
            selected_options="silver / 512GB",
            quantity=1,
        )
        second = CartItem.objects.create(
            user=self.user,
            product=self.product,
            seller=self.seller,
            selected_options="black / 1TB",
            quantity=2,
        )
        CartItem.objects.filter(id=first.id).update(updated_at=timezone.now() - timedelta(days=1))
        CartItem.objects.filter(id=second.id).update(updated_at=timezone.now())
        CartItem.objects.create(
            user=self.other_user,
            product=self.product,
            seller=self.seller,
            selected_options="other",
            quantity=3,
        )

        response = self.client.get("/api/v1/cart", **auth_header_for(self.user))

        self.assertEqual(response.status_code, 200)
        data = response.json()["data"]
        self.assertEqual([item["id"] for item in data], [second.id, first.id])
        self.assertEqual(data[0]["product"]["lowestPrice"], 1390000)
        self.assertEqual(data[0]["unitPrice"], 1390000)
        self.assertEqual(data[0]["subtotal"], 2780000)
        self.assertEqual(len(data), 2)

    def test_add_cart_item_merges_same_identity_and_splits_different_options(self):
        unauthorized = json_request(
            self.client,
            "post",
            "/api/v1/cart",
            {"productId": self.product.id, "sellerId": self.seller.id, "quantity": 1},
        )
        self.assertEqual(unauthorized.status_code, 401)

        created = json_request(
            self.client,
            "post",
            "/api/v1/cart",
            {
                "productId": self.product.id,
                "sellerId": self.seller.id,
                "quantity": 2,
                "selectedOptions": "silver / 512GB",
            },
            **auth_header_for(self.user),
        )
        self.assertEqual(created.status_code, 201)
        item_id = created.json()["data"]["id"]
        self.assertEqual(created.json()["data"]["quantity"], 2)

        merged = json_request(
            self.client,
            "post",
            "/api/v1/cart",
            {
                "productId": self.product.id,
                "sellerId": self.seller.id,
                "quantity": 3,
                "selectedOptions": "silver / 512GB",
            },
            **auth_header_for(self.user),
        )
        self.assertEqual(merged.status_code, 201)
        self.assertEqual(merged.json()["data"]["id"], item_id)
        self.assertEqual(merged.json()["data"]["quantity"], 5)
        self.assertEqual(CartItem.objects.count(), 1)

        split = json_request(
            self.client,
            "post",
            "/api/v1/cart",
            {
                "productId": self.product.id,
                "sellerId": self.seller.id,
                "quantity": 1,
                "selectedOptions": "black / 1TB",
            },
            **auth_header_for(self.user),
        )
        self.assertEqual(split.status_code, 201)
        self.assertNotEqual(split.json()["data"]["id"], item_id)
        self.assertEqual(CartItem.objects.count(), 2)

    def test_add_cart_item_validates_product_seller_and_quantity(self):
        invalid_quantity = json_request(
            self.client,
            "post",
            "/api/v1/cart",
            {"productId": self.product.id, "sellerId": self.seller.id, "quantity": 0},
            **auth_header_for(self.user),
        )
        self.assertEqual(invalid_quantity.status_code, 400)

        hidden_product = json_request(
            self.client,
            "post",
            "/api/v1/cart",
            {"productId": self.hidden_product.id, "sellerId": self.seller.id, "quantity": 1},
            **auth_header_for(self.user),
        )
        self.assertEqual(hidden_product.status_code, 400)

        sold_out_product = json_request(
            self.client,
            "post",
            "/api/v1/cart",
            {"productId": self.sold_out_product.id, "sellerId": self.seller.id, "quantity": 1},
            **auth_header_for(self.user),
        )
        self.assertEqual(sold_out_product.status_code, 400)

        inactive_seller = json_request(
            self.client,
            "post",
            "/api/v1/cart",
            {"productId": self.product.id, "sellerId": self.inactive_seller.id, "quantity": 1},
            **auth_header_for(self.user),
        )
        self.assertEqual(inactive_seller.status_code, 404)

        missing_product = json_request(
            self.client,
            "post",
            "/api/v1/cart",
            {"productId": 999999, "sellerId": self.seller.id, "quantity": 1},
            **auth_header_for(self.user),
        )
        self.assertEqual(missing_product.status_code, 404)

    def test_update_delete_and_clear_cart_scope_to_current_user(self):
        own_item = CartItem.objects.create(
            user=self.user,
            product=self.product,
            seller=self.seller,
            quantity=2,
        )
        other_item = CartItem.objects.create(
            user=self.other_user,
            product=self.product,
            seller=self.seller,
            quantity=4,
        )

        updated = json_request(
            self.client,
            "patch",
            f"/api/v1/cart/{own_item.id}",
            {"quantity": 5},
            **auth_header_for(self.user),
        )
        self.assertEqual(updated.status_code, 200)
        self.assertEqual(updated.json()["data"]["quantity"], 5)

        invalid_update = json_request(
            self.client,
            "patch",
            f"/api/v1/cart/{own_item.id}",
            {"quantity": -1},
            **auth_header_for(self.user),
        )
        self.assertEqual(invalid_update.status_code, 400)

        foreign_update = json_request(
            self.client,
            "patch",
            f"/api/v1/cart/{other_item.id}",
            {"quantity": 1},
            **auth_header_for(self.user),
        )
        self.assertEqual(foreign_update.status_code, 404)

        foreign_delete = self.client.delete(
            f"/api/v1/cart/{other_item.id}",
            content_type="application/json",
            **auth_header_for(self.user),
        )
        self.assertEqual(foreign_delete.status_code, 404)

        deleted = self.client.delete(
            f"/api/v1/cart/{own_item.id}",
            content_type="application/json",
            **auth_header_for(self.user),
        )
        self.assertEqual(deleted.status_code, 200)
        self.assertFalse(CartItem.objects.filter(id=own_item.id).exists())

        replacement = CartItem.objects.create(
            user=self.user,
            product=self.product,
            seller=self.seller,
            quantity=1,
        )
        cleared = self.client.delete(
            "/api/v1/cart",
            content_type="application/json",
            **auth_header_for(self.user),
        )
        self.assertEqual(cleared.status_code, 200)
        self.assertFalse(CartItem.objects.filter(id=replacement.id).exists())
        self.assertTrue(CartItem.objects.filter(id=other_item.id).exists())
