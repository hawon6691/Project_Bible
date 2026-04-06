import json

from django.contrib.auth import get_user_model
from django.test import Client, TestCase
from django.utils import timezone

from apps.catalog.models import (
    Category,
    Product,
    ProductSpec,
    SpecDataType,
    SpecDefinition,
    SpecDefinitionType,
)
from apps.users.models import UserRole
from tests.api_test_helpers import auth_header_for, json_request


class SpecApiTests(TestCase):
    def setUp(self):
        self.client = Client()
        self.user_model = get_user_model()
        self.admin = self.user_model.objects.create_user(
            email="spec-admin@example.com",
            password="Password1!",
            name="스펙관리자",
            phone="01088889999",
            nickname="spec-admin",
            role=UserRole.ADMIN,
        )
        self.admin.email_verified = True
        self.admin.save(update_fields=["email_verified", "updated_at"])

        self.user = self.user_model.objects.create_user(
            email="spec-user@example.com",
            password="Password1!",
            name="일반회원",
            phone="01077778888",
            nickname="spec-user",
        )
        self.user.email_verified = True
        self.user.save(update_fields=["email_verified", "updated_at"])

        self.laptop_category = Category.objects.create(name="노트북", sort_order=1)
        self.monitor_category = Category.objects.create(name="모니터", sort_order=2)

        self.cpu_definition = SpecDefinition.objects.create(
            category=self.laptop_category,
            name="CPU",
            type=SpecDefinitionType.SELECT,
            options=["i5", "i7"],
            data_type=SpecDataType.STRING,
            sort_order=2,
        )
        self.ram_definition = SpecDefinition.objects.create(
            category=self.laptop_category,
            name="RAM",
            type=SpecDefinitionType.NUMBER,
            unit="GB",
            data_type=SpecDataType.NUMBER,
            sort_order=1,
        )
        self.panel_definition = SpecDefinition.objects.create(
            category=self.monitor_category,
            name="패널",
            type=SpecDefinitionType.TEXT,
            data_type=SpecDataType.STRING,
            sort_order=1,
        )

        self.product_one = Product.objects.create(
            name="Spec Laptop One",
            description="첫 번째 비교 상품",
            price=1500000,
            stock=10,
            category=self.laptop_category,
            lowest_price=1400000,
            thumbnail_url="/media/products/spec-laptop-one.png",
        )
        self.product_two = Product.objects.create(
            name="Spec Laptop Two",
            description="두 번째 비교 상품",
            price=1300000,
            stock=20,
            category=self.laptop_category,
            lowest_price=1200000,
            thumbnail_url="/media/products/spec-laptop-two.png",
        )
        self.deleted_product = Product.objects.create(
            name="Deleted Spec Laptop",
            description="삭제된 상품",
            price=1000000,
            stock=1,
            category=self.laptop_category,
            deleted_at=timezone.now(),
        )

    def test_list_spec_definitions_and_filter_by_category(self):
        response = self.client.get("/api/v1/specs/definitions")
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(response.json()["data"]), 3)

        filtered = self.client.get(f"/api/v1/specs/definitions?categoryId={self.laptop_category.id}")
        self.assertEqual(filtered.status_code, 200)
        self.assertEqual([item["name"] for item in filtered.json()["data"]], ["RAM", "CPU"])

    def test_admin_can_create_update_and_delete_spec_definition_with_validation_and_permissions(self):
        unauthorized = json_request(
            self.client,
            "post",
            "/api/v1/specs/definitions",
            {"categoryId": self.laptop_category.id, "name": "GPU", "type": "TEXT"},
        )
        self.assertEqual(unauthorized.status_code, 401)

        forbidden = json_request(
            self.client,
            "post",
            "/api/v1/specs/definitions",
            {"categoryId": self.laptop_category.id, "name": "GPU", "type": "TEXT"},
            **auth_header_for(self.user),
        )
        self.assertEqual(forbidden.status_code, 403)

        invalid = json_request(
            self.client,
            "post",
            "/api/v1/specs/definitions",
            {"categoryId": self.laptop_category.id, "name": "GPU", "type": "SELECT", "options": []},
            **auth_header_for(self.admin),
        )
        self.assertEqual(invalid.status_code, 400)

        created = json_request(
            self.client,
            "post",
            "/api/v1/specs/definitions",
            {
                "categoryId": self.laptop_category.id,
                "name": "GPU",
                "type": "SELECT",
                "options": ["RTX 4060", "RTX 4070"],
                "sortOrder": 3,
            },
            **auth_header_for(self.admin),
        )
        self.assertEqual(created.status_code, 201)
        definition_id = created.json()["data"]["id"]
        self.assertEqual(created.json()["data"]["options"], ["RTX 4060", "RTX 4070"])

        updated = json_request(
            self.client,
            "patch",
            f"/api/v1/specs/definitions/{definition_id}",
            {"type": "TEXT", "options": ["ignored"], "unit": "세대"},
            **auth_header_for(self.admin),
        )
        self.assertEqual(updated.status_code, 200)
        self.assertIsNone(updated.json()["data"]["options"])
        self.assertEqual(updated.json()["data"]["unit"], "세대")

        deleted = self.client.delete(
            f"/api/v1/specs/definitions/{definition_id}",
            content_type="application/json",
            **auth_header_for(self.admin),
        )
        self.assertEqual(deleted.status_code, 200)

    def test_delete_definition_conflicts_when_definition_is_in_use(self):
        ProductSpec.objects.create(product=self.product_one, spec_definition=self.cpu_definition, value="i7")

        response = self.client.delete(
            f"/api/v1/specs/definitions/{self.cpu_definition.id}",
            content_type="application/json",
            **auth_header_for(self.admin),
        )

        self.assertEqual(response.status_code, 409)
        self.assertEqual(response.json()["error"]["code"], "CONFLICT")

    def test_replace_and_get_product_specs(self):
        unauthorized = json_request(
            self.client,
            "put",
            f"/api/v1/products/{self.product_one.id}/specs",
            [{"specDefinitionId": self.cpu_definition.id, "value": "i7"}],
        )
        self.assertEqual(unauthorized.status_code, 401)

        forbidden = json_request(
            self.client,
            "put",
            f"/api/v1/products/{self.product_one.id}/specs",
            [{"specDefinitionId": self.cpu_definition.id, "value": "i7"}],
            **auth_header_for(self.user),
        )
        self.assertEqual(forbidden.status_code, 403)

        missing_numeric = json_request(
            self.client,
            "put",
            f"/api/v1/products/{self.product_one.id}/specs",
            [{"specDefinitionId": self.ram_definition.id, "value": "16"}],
            **auth_header_for(self.admin),
        )
        self.assertEqual(missing_numeric.status_code, 400)

        invalid_select = json_request(
            self.client,
            "put",
            f"/api/v1/products/{self.product_one.id}/specs",
            [{"specDefinitionId": self.cpu_definition.id, "value": "Ryzen 7"}],
            **auth_header_for(self.admin),
        )
        self.assertEqual(invalid_select.status_code, 400)

        wrong_category = json_request(
            self.client,
            "put",
            f"/api/v1/products/{self.product_one.id}/specs",
            [{"specDefinitionId": self.panel_definition.id, "value": "IPS"}],
            **auth_header_for(self.admin),
        )
        self.assertEqual(wrong_category.status_code, 400)

        replaced = json_request(
            self.client,
            "put",
            f"/api/v1/products/{self.product_one.id}/specs",
            [
                {"specDefinitionId": self.cpu_definition.id, "value": "i7"},
                {"specDefinitionId": self.ram_definition.id, "value": "16", "numericValue": 16},
            ],
            **auth_header_for(self.admin),
        )
        self.assertEqual(replaced.status_code, 200)
        self.assertEqual([item["name"] for item in replaced.json()["data"]], ["RAM", "CPU"])
        self.assertEqual(replaced.json()["data"][0]["numericValue"], 16.0)
        self.assertEqual(replaced.json()["data"][0]["unit"], "GB")

        fetched = self.client.get(f"/api/v1/products/{self.product_one.id}/specs")
        self.assertEqual(fetched.status_code, 200)
        self.assertEqual(fetched.json()["data"][1]["value"], "i7")

    def test_compare_products_and_product_specs_filter(self):
        ProductSpec.objects.create(product=self.product_one, spec_definition=self.cpu_definition, value="i7")
        ProductSpec.objects.create(
            product=self.product_one,
            spec_definition=self.ram_definition,
            value="16",
            numeric_value=16,
        )
        ProductSpec.objects.create(product=self.product_two, spec_definition=self.cpu_definition, value="i5")

        compare = json_request(
            self.client,
            "post",
            "/api/v1/specs/compare",
            {"productIds": [self.product_two.id, self.product_one.id]},
        )
        self.assertEqual(compare.status_code, 200)
        self.assertEqual([item["id"] for item in compare.json()["data"]["products"]], [self.product_two.id, self.product_one.id])
        self.assertEqual(compare.json()["data"]["specs"][0]["name"], "RAM")
        self.assertEqual(compare.json()["data"]["specs"][0]["values"], ["-", "16"])
        self.assertEqual(compare.json()["data"]["specs"][1]["values"], ["i5", "i7"])

        invalid_compare = json_request(self.client, "post", "/api/v1/specs/compare", {"productIds": [self.product_one.id]})
        self.assertEqual(invalid_compare.status_code, 400)

        missing_product = json_request(
            self.client,
            "post",
            "/api/v1/specs/compare",
            {"productIds": [self.product_one.id, 999999]},
        )
        self.assertEqual(missing_product.status_code, 404)

        filtered = self.client.get(f"/api/v1/products?specs={json.dumps({'RAM': 16})}")
        self.assertEqual(filtered.status_code, 200)
        self.assertEqual([item["id"] for item in filtered.json()["data"]], [self.product_one.id])

        invalid_filter = self.client.get("/api/v1/products?specs=not-json")
        self.assertEqual(invalid_filter.status_code, 400)

    def test_deleted_product_is_hidden_from_spec_endpoints(self):
        detail = self.client.get(f"/api/v1/products/{self.deleted_product.id}/specs")
        self.assertEqual(detail.status_code, 404)

        compare = json_request(
            self.client,
            "post",
            "/api/v1/specs/compare",
            {"productIds": [self.product_one.id, self.deleted_product.id]},
        )
        self.assertEqual(compare.status_code, 404)
