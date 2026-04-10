import io
import shutil
import tempfile

from PIL import Image
from django.contrib.auth import get_user_model
from django.core.files.uploadedfile import SimpleUploadedFile
from django.test import Client, TestCase, override_settings

from apps.users.models import UserRole
from tests.api_test_helpers import auth_header_for, json_request


def generate_test_image_file() -> SimpleUploadedFile:
    file_obj = io.BytesIO()
    image = Image.new("RGB", (10, 10), color="red")
    image.save(file_obj, format="PNG")
    file_obj.seek(0)
    return SimpleUploadedFile("profile.png", file_obj.read(), content_type="image/png")


class UserApiTests(TestCase):
    @classmethod
    def setUpClass(cls):
        super().setUpClass()
        cls.temp_media_dir = tempfile.mkdtemp(prefix="pbshop-user-api-")

    @classmethod
    def tearDownClass(cls):
        shutil.rmtree(cls.temp_media_dir, ignore_errors=True)
        super().tearDownClass()

    def setUp(self):
        self.client = Client()
        self.user_model = get_user_model()
        self.user = self.user_model.objects.create_user(
            email="user@example.com",
            password="Password1!",
            name="일반회원",
            phone="01012345678",
            nickname="general-user",
        )
        self.user.email_verified = True
        self.user.save(update_fields=["email_verified", "updated_at"])
        self.admin = self.user_model.objects.create_user(
            email="admin@example.com",
            password="Password1!",
            name="관리자",
            phone="01099998888",
            nickname="admin-user",
            role=UserRole.ADMIN,
        )
        self.admin.email_verified = True
        self.admin.save(update_fields=["email_verified", "updated_at"])

    def test_me_requires_auth_and_can_update_and_delete(self):
        unauthorized = self.client.get("/api/v1/users/me")
        self.assertEqual(unauthorized.status_code, 401)

        me = self.client.get("/api/v1/users/me", **auth_header_for(self.user))
        self.assertEqual(me.status_code, 200)
        self.assertEqual(me.json()["data"]["email"], self.user.email)

        update = json_request(
            self.client,
            "patch",
            "/api/v1/users/me",
            {"name": "수정회원", "phone": "010-0000-1111"},
            **auth_header_for(self.user),
        )
        self.assertEqual(update.status_code, 200)
        self.assertEqual(update.json()["data"]["name"], "수정회원")
        self.assertEqual(update.json()["data"]["phone"], "010-0000-1111")

        delete = self.client.delete(
            "/api/v1/users/me",
            content_type="application/json",
            **auth_header_for(self.user),
        )
        self.assertEqual(delete.status_code, 200)
        self.user.refresh_from_db()
        self.assertIsNotNone(self.user.deleted_at)

    def test_public_profile_and_nickname_duplicate_validation(self):
        profile = self.client.get(f"/api/v1/users/{self.user.id}/profile")
        self.assertEqual(profile.status_code, 200)
        self.assertEqual(profile.json()["data"]["nickname"], "general-user")

        other = self.user_model.objects.create_user(
            email="other@example.com",
            password="Password1!",
            name="다른회원",
            phone="01044445555",
            nickname="other-user",
        )
        conflict = json_request(
            self.client,
            "patch",
            "/api/v1/users/me/profile",
            {"nickname": "other-user"},
            **auth_header_for(self.user),
        )
        self.assertEqual(conflict.status_code, 409)
        self.assertEqual(conflict.json()["error"]["code"], "CONFLICT")

        success = json_request(
            self.client,
            "patch",
            "/api/v1/users/me/profile",
            {"nickname": "new-nick", "bio": "소개글"},
            **auth_header_for(self.user),
        )
        self.assertEqual(success.status_code, 200)
        self.assertEqual(success.json()["data"]["nickname"], "new-nick")

    @override_settings(MEDIA_ROOT=tempfile.gettempdir())
    def test_profile_image_upload_and_delete(self):
        upload = self.client.post(
            "/api/v1/users/me/profile-image",
            {"image": generate_test_image_file()},
            **auth_header_for(self.user),
        )
        self.assertEqual(upload.status_code, 200)
        self.assertIn("/media/profiles/", upload.json()["data"]["imageUrl"])

        delete = self.client.delete(
            "/api/v1/users/me/profile-image",
            content_type="application/json",
            **auth_header_for(self.user),
        )
        self.assertEqual(delete.status_code, 200)
        self.assertEqual(delete.json()["data"]["message"], "프로필 이미지가 기본 이미지로 변경되었습니다.")

    def test_admin_list_and_status_boundary(self):
        forbidden = self.client.get("/api/v1/users", **auth_header_for(self.user))
        self.assertEqual(forbidden.status_code, 403)

        listing = self.client.get("/api/v1/users?page=1&limit=10", **auth_header_for(self.admin))
        self.assertEqual(listing.status_code, 200)
        self.assertGreaterEqual(len(listing.json()["data"]), 2)
        self.assertEqual(listing.json()["meta"]["page"], 1)

        status = json_request(
            self.client,
            "patch",
            f"/api/v1/users/{self.user.id}/status",
            {"status": "BLOCKED"},
            **auth_header_for(self.admin),
        )
        self.assertEqual(status.status_code, 200)
        self.assertEqual(status.json()["data"]["status"], "BLOCKED")
