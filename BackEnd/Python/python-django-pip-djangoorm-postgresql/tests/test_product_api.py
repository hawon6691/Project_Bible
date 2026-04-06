import io
import json
import shutil
import tempfile
from datetime import timedelta

from PIL import Image
from django.contrib.auth import get_user_model
from django.core.files.uploadedfile import SimpleUploadedFile
from django.test import Client, TestCase, override_settings
from django.utils import timezone

from apps.catalog.models import (
    Category,
    Product,
    ProductImage,
    ProductOption,
    ProductSpec,
    ProductStatus,
    SpecDataType,
    SpecDefinition,
    SpecDefinitionType,
)
from apps.pricing.models import PriceEntry, Seller
from apps.users.models import UserRole
from tests.api_test_helpers import auth_header_for, json_request


def generate_test_image_file(name: str = "product.png") -> SimpleUploadedFile:
    file_obj = io.BytesIO()
    image = Image.new("RGB", (10, 10), color="blue")
    image.save(file_obj, format="PNG")
    file_obj.seek(0)
    return SimpleUploadedFile(name, file_obj.read(), content_type="image/png")


class ProductApiTests(TestCase):
    @classmethod
    def setUpClass(cls):
        super().setUpClass()
        cls.temp_media_dir = tempfile.mkdtemp(prefix="pbshop-product-api-")

    @classmethod
    def tearDownClass(cls):
        shutil.rmtree(cls.temp_media_dir, ignore_errors=True)
        super().tearDownClass()

    def setUp(self):
        self.client = Client()
        self.user_model = get_user_model()
        self.admin = self.user_model.objects.create_user(
            email="product-admin@example.com",
            password="Password1!",
            name="상품관리자",
            phone="01011112222",
            nickname="product-admin",
            role=UserRole.ADMIN,
        )
        self.admin.email_verified = True
        self.admin.save(update_fields=["email_verified", "updated_at"])

        self.user = self.user_model.objects.create_user(
            email="product-user@example.com",
            password="Password1!",
            name="일반회원",
            phone="01033334444",
            nickname="product-user",
        )
        self.user.email_verified = True
        self.user.save(update_fields=["email_verified", "updated_at"])

        self.laptop_category = Category.objects.create(name="노트북", sort_order=1)
        self.monitor_category = Category.objects.create(name="모니터", sort_order=2)

        self.cpu_spec = SpecDefinition.objects.create(
            category=self.laptop_category,
            name="CPU",
            type=SpecDefinitionType.SELECT,
            data_type=SpecDataType.STRING,
            sort_order=1,
        )

        self.product_one = Product.objects.create(
            name="PBShop Gaming Laptop",
            description="고성능 게이밍 노트북",
            price=1500000,
            discount_price=1400000,
            stock=15,
            category=self.laptop_category,
            lowest_price=1300000,
            seller_count=2,
            review_count=11,
            average_rating=4.8,
            popularity_score=95,
            thumbnail_url="/media/products/gaming-thumb.png",
        )
        self.product_two = Product.objects.create(
            name="PBShop Office Laptop",
            description="사무용 노트북",
            price=1100000,
            discount_price=1000000,
            stock=30,
            category=self.laptop_category,
            lowest_price=1000000,
            seller_count=1,
            review_count=3,
            average_rating=4.1,
            popularity_score=40,
            thumbnail_url="/media/products/office-thumb.png",
        )
        self.product_three = Product.objects.create(
            name="PBShop Wide Monitor",
            description="울트라와이드 모니터",
            price=700000,
            stock=12,
            category=self.monitor_category,
            lowest_price=650000,
            seller_count=0,
            review_count=5,
            average_rating=4.5,
            popularity_score=50,
        )
        self.deleted_product = Product.objects.create(
            name="Deleted Product",
            description="삭제된 상품",
            price=500000,
            stock=1,
            category=self.monitor_category,
            deleted_at=timezone.now(),
        )

        ProductSpec.objects.create(product=self.product_one, spec_definition=self.cpu_spec, value="i7")
        ProductSpec.objects.create(product=self.product_two, spec_definition=self.cpu_spec, value="i5")

        seller_one = Seller.objects.create(name="쿠팡", url="https://coupang.example.com", trust_score=95)
        seller_two = Seller.objects.create(name="11번가", url="https://11st.example.com", trust_score=90)
        PriceEntry.objects.create(
            product=self.product_one,
            seller=seller_one,
            price=1290000,
            product_url="https://coupang.example.com/products/1",
            shipping_info="무료배송",
            shipping_fee=0,
        )
        PriceEntry.objects.create(
            product=self.product_one,
            seller=seller_two,
            price=1350000,
            product_url="https://11st.example.com/products/1",
            shipping_info="무료배송",
            shipping_fee=0,
        )

        ProductOption.objects.create(product=self.product_one, name="색상", values=["실버", "그레이"])
        ProductImage.objects.create(product=self.product_one, url="/media/products/detail-1.png", is_main=True, sort_order=1)

        older = timezone.now() - timedelta(days=1)
        newer = timezone.now()
        Product.objects.filter(id=self.product_one.id).update(created_at=newer)
        Product.objects.filter(id=self.product_two.id).update(created_at=older)
        Product.objects.filter(id=self.product_three.id).update(created_at=older - timedelta(days=1))
        self.product_one.refresh_from_db()
        self.product_two.refresh_from_db()
        self.product_three.refresh_from_db()

    def test_list_products_filters_sort_and_excludes_soft_deleted(self):
        response = self.client.get("/api/v1/products")
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json()["meta"]["totalCount"], 3)
        self.assertNotIn(self.deleted_product.id, [item["id"] for item in response.json()["data"]])

        category_filtered = self.client.get(f"/api/v1/products?categoryId={self.monitor_category.id}")
        self.assertEqual(len(category_filtered.json()["data"]), 1)
        self.assertEqual(category_filtered.json()["data"][0]["id"], self.product_three.id)

        search_filtered = self.client.get("/api/v1/products?search=Gaming")
        self.assertEqual(len(search_filtered.json()["data"]), 1)
        self.assertEqual(search_filtered.json()["data"][0]["id"], self.product_one.id)

        spec_filtered = self.client.get(f"/api/v1/products?specs={json.dumps({'CPU': 'i7'})}")
        self.assertEqual(len(spec_filtered.json()["data"]), 1)
        self.assertEqual(spec_filtered.json()["data"][0]["id"], self.product_one.id)

        price_sorted = self.client.get("/api/v1/products?sort=price_asc")
        self.assertEqual(
            [item["id"] for item in price_sorted.json()["data"]],
            [self.product_three.id, self.product_two.id, self.product_one.id],
        )

        rating_sorted = self.client.get("/api/v1/products?sort=rating_desc")
        self.assertEqual(rating_sorted.json()["data"][0]["id"], self.product_one.id)
        self.assertEqual(rating_sorted.json()["data"][0]["priceDiff"], -100000)
        self.assertAlmostEqual(rating_sorted.json()["data"][0]["priceDiffPercent"], -7.14, places=2)

    def test_get_product_detail_contains_related_data(self):
        response = self.client.get(f"/api/v1/products/{self.product_one.id}")

        self.assertEqual(response.status_code, 200)
        data = response.json()["data"]
        self.assertEqual(data["category"]["name"], "노트북")
        self.assertEqual(data["options"][0]["name"], "색상")
        self.assertEqual(data["images"][0]["isMain"], True)
        self.assertEqual(data["specs"][0]["name"], "CPU")
        self.assertEqual(len(data["priceEntries"]), 2)
        self.assertEqual(data["lowestPrice"], 1290000)
        self.assertEqual(data["highestPrice"], 1350000)
        self.assertEqual(data["averagePrice"], 1320000)

        missing = self.client.get(f"/api/v1/products/{self.deleted_product.id}")
        self.assertEqual(missing.status_code, 404)

    def test_admin_product_crud_and_validation(self):
        unauthorized = json_request(
            self.client,
            "post",
            "/api/v1/products",
            {"name": "신규 상품", "description": "desc", "price": 1000, "stock": 3, "categoryId": self.laptop_category.id},
        )
        self.assertEqual(unauthorized.status_code, 401)

        forbidden = json_request(
            self.client,
            "post",
            "/api/v1/products",
            {"name": "신규 상품", "description": "desc", "price": 1000, "stock": 3, "categoryId": self.laptop_category.id},
            **auth_header_for(self.user),
        )
        self.assertEqual(forbidden.status_code, 403)

        invalid = json_request(
            self.client,
            "post",
            "/api/v1/products",
            {"name": "잘못된 상품", "description": "desc", "price": -1, "stock": 3, "categoryId": self.laptop_category.id},
            **auth_header_for(self.admin),
        )
        self.assertEqual(invalid.status_code, 400)

        created = json_request(
            self.client,
            "post",
            "/api/v1/products",
            {
                "name": "신규 상품",
                "description": "설명",
                "price": 990000,
                "discountPrice": 950000,
                "stock": 8,
                "categoryId": self.laptop_category.id,
                "status": ProductStatus.ON_SALE,
                "options": [{"name": "용량", "values": ["256GB", "512GB"]}],
                "images": [{"url": "/media/products/new-image.png", "isMain": True, "sortOrder": 0}],
            },
            **auth_header_for(self.admin),
        )
        self.assertEqual(created.status_code, 201)
        created_id = created.json()["data"]["id"]
        self.assertEqual(created.json()["data"]["images"][0]["url"], "/media/products/new-image.png")

        updated = json_request(
            self.client,
            "patch",
            f"/api/v1/products/{created_id}",
            {"name": "수정 상품", "stock": 5, "thumbnailUrl": "/media/products/manual-thumb.png"},
            **auth_header_for(self.admin),
        )
        self.assertEqual(updated.status_code, 200)
        self.assertEqual(updated.json()["data"]["name"], "수정 상품")

        deleted = self.client.delete(
            f"/api/v1/products/{created_id}",
            content_type="application/json",
            **auth_header_for(self.admin),
        )
        self.assertEqual(deleted.status_code, 200)
        hidden_from_list = self.client.get("/api/v1/products?search=수정 상품")
        self.assertEqual(hidden_from_list.json()["meta"]["totalCount"], 0)

    def test_option_management_requires_matching_product(self):
        created = json_request(
            self.client,
            "post",
            f"/api/v1/products/{self.product_one.id}/options",
            {"name": "저장공간", "values": ["512GB", "1TB"]},
            **auth_header_for(self.admin),
        )
        self.assertEqual(created.status_code, 201)
        option_id = created.json()["data"]["id"]

        updated = json_request(
            self.client,
            "patch",
            f"/api/v1/products/{self.product_one.id}/options/{option_id}",
            {"name": "저장공간", "values": ["2TB"]},
            **auth_header_for(self.admin),
        )
        self.assertEqual(updated.status_code, 200)
        self.assertEqual(updated.json()["data"]["values"], ["2TB"])

        mismatch = json_request(
            self.client,
            "patch",
            f"/api/v1/products/{self.product_two.id}/options/{option_id}",
            {"name": "저장공간", "values": ["4TB"]},
            **auth_header_for(self.admin),
        )
        self.assertEqual(mismatch.status_code, 404)

        deleted = self.client.delete(
            f"/api/v1/products/{self.product_one.id}/options/{option_id}",
            content_type="application/json",
            **auth_header_for(self.admin),
        )
        self.assertEqual(deleted.status_code, 200)

    @override_settings(MEDIA_ROOT=tempfile.gettempdir())
    def test_image_upload_and_delete_reselects_thumbnail(self):
        first = self.client.post(
            f"/api/v1/products/{self.product_two.id}/images",
            {"image": generate_test_image_file("first.png"), "sortOrder": "2"},
            **auth_header_for(self.admin),
        )
        self.assertEqual(first.status_code, 201)
        first_id = first.json()["data"]["id"]
        self.assertTrue(first.json()["data"]["url"].startswith("/media/products/"))
        self.product_two.refresh_from_db()
        self.assertEqual(self.product_two.thumbnail_url, first.json()["data"]["url"])

        second = self.client.post(
            f"/api/v1/products/{self.product_two.id}/images",
            {"image": generate_test_image_file("second.png"), "isMain": "true", "sortOrder": "1"},
            **auth_header_for(self.admin),
        )
        self.assertEqual(second.status_code, 201)
        second_id = second.json()["data"]["id"]
        self.product_two.refresh_from_db()
        self.assertEqual(self.product_two.thumbnail_url, second.json()["data"]["url"])

        deleted = self.client.delete(
            f"/api/v1/products/{self.product_two.id}/images/{second_id}",
            content_type="application/json",
            **auth_header_for(self.admin),
        )
        self.assertEqual(deleted.status_code, 200)
        self.product_two.refresh_from_db()
        self.assertTrue(ProductImage.objects.filter(id=first_id, is_main=True).exists())
        self.assertEqual(self.product_two.thumbnail_url, ProductImage.objects.get(id=first_id).url)

        invalid = self.client.post(
            f"/api/v1/products/{self.product_two.id}/images",
            {"image": SimpleUploadedFile("bad.txt", b"not-image", content_type="text/plain")},
            **auth_header_for(self.admin),
        )
        self.assertEqual(invalid.status_code, 400)
