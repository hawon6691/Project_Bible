from datetime import timedelta

from django.contrib.auth import get_user_model
from django.test import Client, TestCase
from django.utils import timezone

from apps.catalog.models import Category, Product
from apps.pricing.models import PriceAlert, PriceEntry, PriceHistory, Seller, ShippingType
from apps.users.models import UserRole
from tests.api_test_helpers import auth_header_for, json_request


class PriceApiTests(TestCase):
    def setUp(self):
        self.client = Client()
        self.user_model = get_user_model()
        self.user = self.user_model.objects.create_user(
            email="price-user@example.com",
            password="Password1!",
            name="가격회원",
            phone="01040004000",
            nickname="price-user",
        )
        self.user.email_verified = True
        self.user.save(update_fields=["email_verified", "updated_at"])

        self.other_user = self.user_model.objects.create_user(
            email="price-other@example.com",
            password="Password1!",
            name="다른회원",
            phone="01050005000",
            nickname="price-other",
        )
        self.other_user.email_verified = True
        self.other_user.save(update_fields=["email_verified", "updated_at"])

        self.admin = self.user_model.objects.create_user(
            email="price-admin@example.com",
            password="Password1!",
            name="관리자",
            phone="01060006000",
            nickname="price-admin",
            role=UserRole.ADMIN,
        )
        self.admin.email_verified = True
        self.admin.save(update_fields=["email_verified", "updated_at"])

        self.seller_user = self.user_model.objects.create_user(
            email="price-seller@example.com",
            password="Password1!",
            name="판매회원",
            phone="01070007000",
            nickname="price-seller",
            role=UserRole.SELLER,
        )
        self.seller_user.email_verified = True
        self.seller_user.save(update_fields=["email_verified", "updated_at"])

        self.category = Category.objects.create(name="그래픽카드", sort_order=1)
        self.product = Product.objects.create(
            name="PBShop GPU",
            description="가격 API 테스트 상품",
            price=2000000,
            stock=7,
            category=self.category,
        )

    def test_price_history_supports_period_aggregation_and_validation(self):
        today = timezone.localdate()
        monthly_date = today - timedelta(days=35)
        weekly_date_one = today - timedelta(days=5)
        weekly_date_two = today - timedelta(days=4)
        old_date = today - timedelta(days=200)

        PriceHistory.objects.create(
            product=self.product,
            date=monthly_date,
            lowest_price=1800000,
            average_price=1900000,
            highest_price=2000000,
        )
        PriceHistory.objects.create(
            product=self.product,
            date=weekly_date_one,
            lowest_price=1500000,
            average_price=1600000,
            highest_price=1700000,
        )
        PriceHistory.objects.create(
            product=self.product,
            date=weekly_date_two,
            lowest_price=1400000,
            average_price=1500000,
            highest_price=1650000,
        )
        PriceHistory.objects.create(
            product=self.product,
            date=old_date,
            lowest_price=2100000,
            average_price=2150000,
            highest_price=2200000,
        )

        default_response = self.client.get(f"/api/v1/products/{self.product.id}/price-history")
        self.assertEqual(default_response.status_code, 200)
        default_data = default_response.json()["data"]
        self.assertEqual(default_data["productId"], self.product.id)
        self.assertEqual(default_data["productName"], self.product.name)
        self.assertEqual(default_data["allTimeLowest"], 1400000)
        self.assertEqual(default_data["allTimeHighest"], 2200000)
        self.assertEqual(len(default_data["history"]), 3)
        self.assertEqual(default_data["history"][0]["date"], monthly_date.isoformat())
        self.assertEqual(default_data["history"][-1]["date"], weekly_date_two.isoformat())

        weekly_response = self.client.get(f"/api/v1/products/{self.product.id}/price-history?period=3m&type=weekly")
        self.assertEqual(weekly_response.status_code, 200)
        weekly_data = weekly_response.json()["data"]["history"]
        self.assertEqual(len(weekly_data), 2)
        self.assertEqual(weekly_data[-1]["lowestPrice"], 1400000)
        self.assertEqual(weekly_data[-1]["averagePrice"], 1550000)

        monthly_response = self.client.get(f"/api/v1/products/{self.product.id}/price-history?period=1y&type=monthly")
        self.assertEqual(monthly_response.status_code, 200)
        monthly_data = monthly_response.json()["data"]["history"]
        self.assertGreaterEqual(len(monthly_data), 3)
        self.assertIn(monthly_date.replace(day=1).isoformat(), [item["date"] for item in monthly_data])
        self.assertIn(old_date.replace(day=1).isoformat(), [item["date"] for item in monthly_data])

        invalid_period = self.client.get(f"/api/v1/products/{self.product.id}/price-history?period=10y")
        self.assertEqual(invalid_period.status_code, 400)

        invalid_type = self.client.get(f"/api/v1/products/{self.product.id}/price-history?type=hourly")
        self.assertEqual(invalid_type.status_code, 400)

        missing_product = self.client.get("/api/v1/products/999999/price-history")
        self.assertEqual(missing_product.status_code, 404)

    def test_user_can_create_list_delete_and_reactivate_price_alert(self):
        unauthorized_list = self.client.get("/api/v1/price-alerts")
        self.assertEqual(unauthorized_list.status_code, 401)

        self.product.lowest_price = 1500000
        self.product.save(update_fields=["lowest_price", "updated_at"])

        invalid_payload = json_request(
            self.client,
            "post",
            "/api/v1/price-alerts",
            {"productId": self.product.id, "targetPrice": 0},
            **auth_header_for(self.user),
        )
        self.assertEqual(invalid_payload.status_code, 400)

        created = json_request(
            self.client,
            "post",
            "/api/v1/price-alerts",
            {"productId": self.product.id, "targetPrice": 1400000},
            **auth_header_for(self.user),
        )
        self.assertEqual(created.status_code, 201)
        alert_id = created.json()["data"]["id"]
        self.assertFalse(created.json()["data"]["isTriggered"])

        listed = self.client.get("/api/v1/price-alerts", **auth_header_for(self.user))
        self.assertEqual(listed.status_code, 200)
        self.assertEqual(len(listed.json()["data"]), 1)
        self.assertEqual(listed.json()["data"][0]["id"], alert_id)

        duplicate = json_request(
            self.client,
            "post",
            "/api/v1/price-alerts",
            {"productId": self.product.id, "targetPrice": 1300000},
            **auth_header_for(self.user),
        )
        self.assertEqual(duplicate.status_code, 409)
        self.assertEqual(duplicate.json()["error"]["code"], "ALERT_EXISTS")

        deleted = self.client.delete(
            f"/api/v1/price-alerts/{alert_id}",
            content_type="application/json",
            **auth_header_for(self.user),
        )
        self.assertEqual(deleted.status_code, 200)
        self.assertFalse(PriceAlert.objects.get(id=alert_id).is_active)

        relisted = self.client.get("/api/v1/price-alerts", **auth_header_for(self.user))
        self.assertEqual(relisted.status_code, 200)
        self.assertEqual(relisted.json()["data"], [])

        recreated = json_request(
            self.client,
            "post",
            "/api/v1/price-alerts",
            {"productId": self.product.id, "targetPrice": 1600000},
            **auth_header_for(self.user),
        )
        self.assertEqual(recreated.status_code, 201)
        self.assertEqual(recreated.json()["data"]["id"], alert_id)
        self.assertTrue(recreated.json()["data"]["isTriggered"])
        self.assertEqual(recreated.json()["data"]["currentLowestPrice"], 1500000)

        forbidden_delete = self.client.delete(
            f"/api/v1/price-alerts/{alert_id}",
            content_type="application/json",
            **auth_header_for(self.other_user),
        )
        self.assertEqual(forbidden_delete.status_code, 404)

    def test_price_alerts_trigger_on_seller_activation_and_price_update(self):
        active_seller = Seller.objects.create(
            name="활성 판매처",
            url="https://active.example.com",
            trust_score=80,
            trust_grade="B",
        )
        inactive_seller = Seller.objects.create(
            name="비활성 판매처",
            url="https://inactive.example.com",
            is_active=False,
        )
        PriceEntry.objects.create(
            product=self.product,
            seller=active_seller,
            price=1800000,
            shipping_cost=0,
            product_url="https://active.example.com/products/1",
            shipping_fee=0,
            shipping_type=ShippingType.FREE,
        )
        hidden_entry = PriceEntry.objects.create(
            product=self.product,
            seller=inactive_seller,
            price=1300000,
            shipping_cost=0,
            product_url="https://inactive.example.com/products/1",
            shipping_fee=0,
            shipping_type=ShippingType.FREE,
        )
        self.product.lowest_price = 1800000
        self.product.seller_count = 1
        self.product.save(update_fields=["lowest_price", "seller_count", "updated_at"])

        first_alert = json_request(
            self.client,
            "post",
            "/api/v1/price-alerts",
            {"productId": self.product.id, "targetPrice": 1500000},
            **auth_header_for(self.user),
        )
        self.assertEqual(first_alert.status_code, 201)
        first_alert_id = first_alert.json()["data"]["id"]
        self.assertFalse(first_alert.json()["data"]["isTriggered"])

        activated = json_request(
            self.client,
            "patch",
            f"/api/v1/sellers/{inactive_seller.id}",
            {"isActive": True},
            **auth_header_for(self.admin),
        )
        self.assertEqual(activated.status_code, 200)
        self.product.refresh_from_db()
        self.assertEqual(self.product.lowest_price, 1300000)
        self.assertEqual(self.product.seller_count, 2)
        self.assertTrue(PriceAlert.objects.get(id=first_alert_id).is_triggered)

        second_alert = json_request(
            self.client,
            "post",
            "/api/v1/price-alerts",
            {"productId": self.product.id, "targetPrice": 1200000},
            **auth_header_for(self.other_user),
        )
        self.assertEqual(second_alert.status_code, 201)
        second_alert_id = second_alert.json()["data"]["id"]
        self.assertFalse(second_alert.json()["data"]["isTriggered"])

        updated_entry = json_request(
            self.client,
            "patch",
            f"/api/v1/prices/{hidden_entry.id}",
            {"price": 1100000, "shippingCost": 0, "shippingType": "FREE", "isAvailable": True},
            **auth_header_for(self.seller_user),
        )
        self.assertEqual(updated_entry.status_code, 200)
        self.product.refresh_from_db()
        self.assertEqual(self.product.lowest_price, 1100000)
        self.assertTrue(PriceAlert.objects.get(id=second_alert_id).is_triggered)

        deleted_entry = self.client.delete(
            f"/api/v1/prices/{hidden_entry.id}",
            content_type="application/json",
            **auth_header_for(self.admin),
        )
        self.assertEqual(deleted_entry.status_code, 200)
        self.assertTrue(PriceAlert.objects.get(id=second_alert_id).is_triggered)
