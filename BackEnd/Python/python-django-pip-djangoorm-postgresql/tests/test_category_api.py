from django.contrib.auth import get_user_model
from django.test import Client, TestCase

from apps.catalog.models import Category
from apps.users.models import UserRole
from tests.api_test_helpers import auth_header_for, json_request


class CategoryApiTests(TestCase):
    def setUp(self):
        self.client = Client()
        self.user_model = get_user_model()
        self.admin = self.user_model.objects.create_user(
            email="category-admin@example.com",
            password="Password1!",
            name="관리자",
            phone="01012341234",
            nickname="category-admin",
            role=UserRole.ADMIN,
        )
        self.admin.email_verified = True
        self.admin.save(update_fields=["email_verified", "updated_at"])

        self.user = self.user_model.objects.create_user(
            email="category-user@example.com",
            password="Password1!",
            name="일반회원",
            phone="01056785678",
            nickname="category-user",
        )
        self.user.email_verified = True
        self.user.save(update_fields=["email_verified", "updated_at"])

    def test_list_tree_and_get_category_detail(self):
        laptop = Category.objects.create(name="노트북", sort_order=2)
        monitor = Category.objects.create(name="모니터", sort_order=1)
        Category.objects.create(name="게이밍 노트북", parent=laptop, sort_order=3)
        Category.objects.create(name="사무용 노트북", parent=laptop, sort_order=1)

        tree = self.client.get("/api/v1/categories")
        self.assertEqual(tree.status_code, 200)
        payload = tree.json()["data"]
        self.assertEqual([item["name"] for item in payload], ["모니터", "노트북"])
        self.assertEqual([item["name"] for item in payload[1]["children"]], ["사무용 노트북", "게이밍 노트북"])

        detail = self.client.get(f"/api/v1/categories/{laptop.id}")
        self.assertEqual(detail.status_code, 200)
        self.assertEqual(detail.json()["data"]["name"], "노트북")
        self.assertEqual(len(detail.json()["data"]["children"]), 2)

    def test_admin_can_create_and_update_category(self):
        parent = Category.objects.create(name="컴퓨터", sort_order=1)

        create = json_request(
            self.client,
            "post",
            "/api/v1/categories",
            {"name": "그래픽카드", "parentId": parent.id, "sortOrder": 4},
            **auth_header_for(self.admin),
        )
        self.assertEqual(create.status_code, 201)
        self.assertEqual(create.json()["data"]["parentId"], parent.id)
        self.assertEqual(create.json()["data"]["sortOrder"], 4)

        category_id = create.json()["data"]["id"]
        update = json_request(
            self.client,
            "patch",
            f"/api/v1/categories/{category_id}",
            {"name": "GPU", "sortOrder": 2},
            **auth_header_for(self.admin),
        )
        self.assertEqual(update.status_code, 200)
        self.assertEqual(update.json()["data"]["name"], "GPU")
        self.assertEqual(update.json()["data"]["sortOrder"], 2)

    def test_create_requires_admin_and_valid_parent(self):
        unauthorized = json_request(
            self.client,
            "post",
            "/api/v1/categories",
            {"name": "주변기기"},
        )
        self.assertEqual(unauthorized.status_code, 401)

        forbidden = json_request(
            self.client,
            "post",
            "/api/v1/categories",
            {"name": "주변기기"},
            **auth_header_for(self.user),
        )
        self.assertEqual(forbidden.status_code, 403)

        missing_parent = json_request(
            self.client,
            "post",
            "/api/v1/categories",
            {"name": "주변기기", "parentId": 9999},
            **auth_header_for(self.admin),
        )
        self.assertEqual(missing_parent.status_code, 404)
        self.assertEqual(missing_parent.json()["error"]["code"], "CATEGORY_NOT_FOUND")

    def test_delete_blocks_category_with_children_and_allows_leaf_delete(self):
        parent = Category.objects.create(name="데스크탑")
        child = Category.objects.create(name="미니 PC", parent=parent)

        blocked = self.client.delete(
            f"/api/v1/categories/{parent.id}",
            content_type="application/json",
            **auth_header_for(self.admin),
        )
        self.assertEqual(blocked.status_code, 400)
        self.assertEqual(blocked.json()["error"]["code"], "CATEGORY_HAS_CHILDREN")

        deleted = self.client.delete(
            f"/api/v1/categories/{child.id}",
            content_type="application/json",
            **auth_header_for(self.admin),
        )
        self.assertEqual(deleted.status_code, 200)
        self.assertFalse(Category.objects.filter(id=child.id).exists())
