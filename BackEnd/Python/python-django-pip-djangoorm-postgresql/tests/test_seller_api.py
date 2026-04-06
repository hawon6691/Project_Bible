from django.contrib.auth import get_user_model
from django.test import Client, TestCase

from apps.catalog.models import Category, Product
from apps.pricing.models import PriceEntry, Seller, ShippingType
from apps.users.models import UserRole
from tests.api_test_helpers import auth_header_for, json_request


class SellerApiTests(TestCase):
    def setUp(self):
        self.client = Client()
        self.user_model = get_user_model()
        self.admin = self.user_model.objects.create_user(
            email="seller-admin@example.com",
            password="Password1!",
            name="판매처관리자",
            phone="01010001000",
            nickname="seller-admin",
            role=UserRole.ADMIN,
        )
        self.admin.email_verified = True
        self.admin.save(update_fields=["email_verified", "updated_at"])

        self.seller_user = self.user_model.objects.create_user(
            email="seller-user@example.com",
            password="Password1!",
            name="판매회원",
            phone="01020002000",
            nickname="seller-user",
            role=UserRole.SELLER,
        )
        self.seller_user.email_verified = True
        self.seller_user.save(update_fields=["email_verified", "updated_at"])

        self.user = self.user_model.objects.create_user(
            email="plain-user@example.com",
            password="Password1!",
            name="일반회원",
            phone="01030003000",
            nickname="plain-user",
        )
        self.user.email_verified = True
        self.user.save(update_fields=["email_verified", "updated_at"])

        self.category = Category.objects.create(name="노트북", sort_order=1)
        self.product = Product.objects.create(
            name="Seller Product",
            description="판매처 API 테스트 상품",
            price=1500000,
            stock=5,
            category=self.category,
        )

        self.seller_one = Seller.objects.create(
            name="쿠팡",
            url="https://coupang.example.com",
            logo_url="/uploads/sellers/coupang.png",
            trust_score=95,
            trust_grade="A+",
            description="로켓배송",
        )
        self.seller_two = Seller.objects.create(
            name="11번가",
            url="https://11st.example.com",
            trust_score=80,
            trust_grade="B",
        )
        self.inactive_seller = Seller.objects.create(
            name="숨김 판매처",
            url="https://hidden.example.com",
            is_active=False,
        )

    def test_seller_list_and_detail_hide_inactive(self):
        response = self.client.get("/api/v1/sellers")

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json()["meta"]["totalCount"], 2)
        self.assertEqual([item["id"] for item in response.json()["data"]], [self.seller_one.id, self.seller_two.id])

        detail = self.client.get(f"/api/v1/sellers/{self.seller_one.id}")
        self.assertEqual(detail.status_code, 200)
        self.assertEqual(detail.json()["data"]["name"], "쿠팡")

        hidden = self.client.get(f"/api/v1/sellers/{self.inactive_seller.id}")
        self.assertEqual(hidden.status_code, 404)

    def test_admin_can_create_update_and_deactivate_seller(self):
        unauthorized = json_request(
            self.client,
            "post",
            "/api/v1/sellers",
            {"name": "신규 판매처", "url": "https://new.example.com"},
        )
        self.assertEqual(unauthorized.status_code, 401)

        forbidden = json_request(
            self.client,
            "post",
            "/api/v1/sellers",
            {"name": "신규 판매처", "url": "https://new.example.com"},
            **auth_header_for(self.user),
        )
        self.assertEqual(forbidden.status_code, 403)

        invalid = json_request(
            self.client,
            "post",
            "/api/v1/sellers",
            {"name": "", "url": "not-a-url", "trustScore": 101},
            **auth_header_for(self.admin),
        )
        self.assertEqual(invalid.status_code, 400)

        created = json_request(
            self.client,
            "post",
            "/api/v1/sellers",
            {
                "name": "신규 판매처",
                "url": "https://new.example.com",
                "logoUrl": "/uploads/sellers/new.png",
                "description": "빠른배송",
                "trustScore": 88,
                "trustGrade": "B+",
            },
            **auth_header_for(self.admin),
        )
        self.assertEqual(created.status_code, 201)
        seller_id = created.json()["data"]["id"]
        self.assertEqual(created.json()["data"]["trustScore"], 88)

        updated = json_request(
            self.client,
            "patch",
            f"/api/v1/sellers/{seller_id}",
            {"trustScore": 90, "isActive": True, "description": "당일배송"},
            **auth_header_for(self.admin),
        )
        self.assertEqual(updated.status_code, 200)
        self.assertEqual(updated.json()["data"]["trustScore"], 90)
        self.assertEqual(updated.json()["data"]["description"], "당일배송")

        deleted = self.client.delete(
            f"/api/v1/sellers/{seller_id}",
            content_type="application/json",
            **auth_header_for(self.admin),
        )
        self.assertEqual(deleted.status_code, 200)
        self.assertFalse(Seller.objects.get(id=seller_id).is_active)

    def test_get_product_prices_returns_sorted_summary(self):
        PriceEntry.objects.create(
            product=self.product,
            seller=self.seller_one,
            price=1500000,
            shipping_cost=0,
            product_url="https://coupang.example.com/products/1",
            shipping_fee=0,
            shipping_type=ShippingType.FREE,
        )
        PriceEntry.objects.create(
            product=self.product,
            seller=self.seller_two,
            price=1600000,
            shipping_cost=5000,
            shipping_info="일반배송",
            product_url="https://11st.example.com/products/1",
            shipping_fee=5000,
            shipping_type=ShippingType.PAID,
        )
        PriceEntry.objects.create(
            product=self.product,
            seller=self.inactive_seller,
            price=1000000,
            shipping_cost=0,
            product_url="https://hidden.example.com/products/1",
            shipping_fee=0,
            shipping_type=ShippingType.FREE,
        )

        response = self.client.get(f"/api/v1/products/{self.product.id}/prices")

        self.assertEqual(response.status_code, 200)
        data = response.json()["data"]
        self.assertEqual(data["lowestPrice"], 1500000)
        self.assertEqual(data["averagePrice"], 1550000)
        self.assertEqual(data["highestPrice"], 1600000)
        self.assertEqual([item["seller"]["id"] for item in data["entries"]], [self.seller_one.id, self.seller_two.id])
        self.assertEqual(data["entries"][0]["shippingInfo"], "무료배송")
        self.assertEqual(data["entries"][1]["shippingInfo"], "일반배송")

    def test_seller_and_admin_can_manage_price_entries_and_refresh_product_stats(self):
        unauthorized = json_request(
            self.client,
            "post",
            f"/api/v1/products/{self.product.id}/prices",
            {"sellerId": self.seller_one.id, "price": 1500000, "productUrl": "https://coupang.example.com/products/1"},
        )
        self.assertEqual(unauthorized.status_code, 401)

        forbidden = json_request(
            self.client,
            "post",
            f"/api/v1/products/{self.product.id}/prices",
            {"sellerId": self.seller_one.id, "price": 1500000, "productUrl": "https://coupang.example.com/products/1"},
            **auth_header_for(self.user),
        )
        self.assertEqual(forbidden.status_code, 403)

        inactive_seller = json_request(
            self.client,
            "post",
            f"/api/v1/products/{self.product.id}/prices",
            {"sellerId": self.inactive_seller.id, "price": 1500000, "productUrl": "https://hidden.example.com/products/1"},
            **auth_header_for(self.seller_user),
        )
        self.assertEqual(inactive_seller.status_code, 404)

        created = json_request(
            self.client,
            "post",
            f"/api/v1/products/{self.product.id}/prices",
            {
                "sellerId": self.seller_one.id,
                "price": 1500000,
                "shippingCost": 0,
                "productUrl": "https://coupang.example.com/products/1",
                "shippingType": "FREE",
            },
            **auth_header_for(self.seller_user),
        )
        self.assertEqual(created.status_code, 201)
        price_id = created.json()["data"]["id"]
        self.product.refresh_from_db()
        self.assertEqual(self.product.lowest_price, 1500000)
        self.assertEqual(self.product.seller_count, 1)

        invalid = json_request(
            self.client,
            "patch",
            f"/api/v1/prices/{price_id}",
            {"price": -1, "shippingType": "BAD"},
            **auth_header_for(self.seller_user),
        )
        self.assertEqual(invalid.status_code, 400)

        updated = json_request(
            self.client,
            "patch",
            f"/api/v1/prices/{price_id}",
            {"price": 1400000, "shippingCost": 10000, "shippingType": "PAID", "isAvailable": True},
            **auth_header_for(self.admin),
        )
        self.assertEqual(updated.status_code, 200)
        self.assertEqual(updated.json()["data"]["price"], 1400000)
        self.product.refresh_from_db()
        self.assertEqual(self.product.lowest_price, 1400000)
        self.assertEqual(self.product.seller_count, 1)

        deleted = self.client.delete(
            f"/api/v1/prices/{price_id}",
            content_type="application/json",
            **auth_header_for(self.admin),
        )
        self.assertEqual(deleted.status_code, 200)
        self.product.refresh_from_db()
        self.assertIsNone(self.product.lowest_price)
        self.assertEqual(self.product.seller_count, 0)

    def test_deactivating_seller_updates_product_stats(self):
        PriceEntry.objects.create(
            product=self.product,
            seller=self.seller_one,
            price=1300000,
            shipping_cost=0,
            product_url="https://coupang.example.com/products/1",
            shipping_fee=0,
            shipping_type=ShippingType.FREE,
        )
        PriceEntry.objects.create(
            product=self.product,
            seller=self.seller_two,
            price=1500000,
            shipping_cost=0,
            product_url="https://11st.example.com/products/1",
            shipping_fee=0,
            shipping_type=ShippingType.FREE,
        )

        self.client.delete(
            f"/api/v1/sellers/{self.seller_one.id}",
            content_type="application/json",
            **auth_header_for(self.admin),
        )

        self.product.refresh_from_db()
        self.assertEqual(self.product.lowest_price, 1500000)
        self.assertEqual(self.product.seller_count, 1)
