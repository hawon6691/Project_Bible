from datetime import timedelta
from urllib.parse import parse_qs, urlparse

from django.contrib.auth import get_user_model
from django.core import mail
from django.test import Client, TestCase
from django.utils import timezone

from apps.auth.models import EmailVerification, SocialAccount
from apps.users.models import UserStatus
from tests.api_test_helpers import auth_header_for, json_request


class AuthApiTests(TestCase):
    def setUp(self):
        self.client = Client()
        self.user_model = get_user_model()

    def create_user(self, **overrides):
        payload = {
            "email": "tester@example.com",
            "password": "Password1!",
            "name": "테스터",
            "phone": "01012345678",
            "nickname": "tester",
        }
        payload.update(overrides)
        user = self.user_model.objects.create_user(**payload)
        return user

    def test_signup_success_and_duplicate_email(self):
        response = json_request(
            self.client,
            "post",
            "/api/v1/auth/signup",
            {
                "email": "signup@example.com",
                "password": "Password1!",
                "name": "가입자",
                "phone": "010-1111-2222",
            },
        )

        self.assertEqual(response.status_code, 201)
        self.assertTrue(response.json()["success"])
        self.assertEqual(response.json()["data"]["email"], "signup@example.com")
        self.assertEqual(len(mail.outbox), 1)
        self.assertEqual(EmailVerification.objects.count(), 1)

        duplicate = json_request(
            self.client,
            "post",
            "/api/v1/auth/signup",
            {
                "email": "signup@example.com",
                "password": "Password1!",
                "name": "중복자",
                "phone": "010-1111-3333",
            },
        )
        self.assertEqual(duplicate.status_code, 409)
        self.assertEqual(duplicate.json()["error"]["code"], "DUPLICATE_EMAIL")

    def test_verify_email_success_invalid_and_attempt_exceeded(self):
        user = self.create_user(email="verify@example.com", nickname="verify-user")
        user.email_verified = False
        user.save(update_fields=["email_verified", "updated_at"])
        verification = EmailVerification.objects.create(
            user=user,
            type="SIGNUP",
            code="123456",
            expires_at=timezone.now() - timedelta(seconds=1),
        )

        expired = json_request(
            self.client,
            "post",
            "/api/v1/auth/verify-email",
            {"email": user.email, "code": "123456"},
        )
        self.assertEqual(expired.status_code, 410)
        self.assertEqual(expired.json()["error"]["code"], "VERIFICATION_CODE_EXPIRED")

        verification.expires_at = timezone.now() + timedelta(minutes=10)
        verification.save(update_fields=["expires_at", "updated_at"])
        invalid = json_request(
            self.client,
            "post",
            "/api/v1/auth/verify-email",
            {"email": user.email, "code": "999999"},
        )
        self.assertEqual(invalid.status_code, 400)
        self.assertEqual(invalid.json()["error"]["code"], "INVALID_VERIFICATION_CODE")

        verification.refresh_from_db()
        verification.attempt_count = 5
        verification.save(update_fields=["attempt_count", "updated_at"])
        exceeded = json_request(
            self.client,
            "post",
            "/api/v1/auth/verify-email",
            {"email": user.email, "code": "123456"},
        )
        self.assertEqual(exceeded.status_code, 429)
        self.assertEqual(exceeded.json()["error"]["code"], "VERIFICATION_ATTEMPT_EXCEEDED")

        verification.attempt_count = 0
        verification.expires_at = timezone.now() + timedelta(minutes=10)
        verification.save(update_fields=["attempt_count", "expires_at", "updated_at"])
        success = json_request(
            self.client,
            "post",
            "/api/v1/auth/verify-email",
            {"email": user.email, "code": "123456"},
        )
        self.assertEqual(success.status_code, 200)
        self.assertTrue(success.json()["data"]["verified"])

    def test_login_refresh_and_logout_flow(self):
        user = self.create_user()
        user.email_verified = True
        user.save(update_fields=["email_verified", "updated_at"])

        login = json_request(
            self.client,
            "post",
            "/api/v1/auth/login",
            {"email": user.email, "password": "Password1!"},
        )
        self.assertEqual(login.status_code, 200)
        tokens = login.json()["data"]

        refresh = json_request(
            self.client,
            "post",
            "/api/v1/auth/refresh",
            {"refreshToken": tokens["refreshToken"]},
        )
        self.assertEqual(refresh.status_code, 200)
        self.assertIn("accessToken", refresh.json()["data"])

        unauthorized_logout = json_request(self.client, "post", "/api/v1/auth/logout")
        self.assertEqual(unauthorized_logout.status_code, 401)

        logout = json_request(
            self.client,
            "post",
            "/api/v1/auth/logout",
            {},
            **auth_header_for(user),
        )
        self.assertEqual(logout.status_code, 200)
        self.assertEqual(logout.json()["data"]["message"], "로그아웃 되었습니다.")

        invalid_refresh = json_request(
            self.client,
            "post",
            "/api/v1/auth/refresh",
            {"refreshToken": tokens["refreshToken"]},
        )
        self.assertEqual(invalid_refresh.status_code, 401)
        self.assertEqual(invalid_refresh.json()["error"]["code"], "INVALID_REFRESH_TOKEN")

    def test_login_requires_verified_and_unblocked_user(self):
        user = self.create_user(email="pending@example.com", nickname="pending-user")
        pending = json_request(
            self.client,
            "post",
            "/api/v1/auth/login",
            {"email": user.email, "password": "Password1!"},
        )
        self.assertEqual(pending.status_code, 403)
        self.assertEqual(pending.json()["error"]["code"], "EMAIL_NOT_VERIFIED")

        user.email_verified = True
        user.status = UserStatus.BLOCKED
        user.save(update_fields=["email_verified", "status", "updated_at"])
        blocked = json_request(
            self.client,
            "post",
            "/api/v1/auth/login",
            {"email": user.email, "password": "Password1!"},
        )
        self.assertEqual(blocked.status_code, 403)
        self.assertEqual(blocked.json()["error"]["code"], "USER_BLOCKED")

    def test_password_reset_request_verify_and_confirm(self):
        user = self.create_user(email="reset@example.com", nickname="reset-user")
        user.email_verified = True
        user.save(update_fields=["email_verified", "updated_at"])

        request_reset = json_request(
            self.client,
            "post",
            "/api/v1/auth/password-reset/request",
            {"email": user.email, "phone": "010-1234-5678"},
        )
        self.assertEqual(request_reset.status_code, 200)
        verification = EmailVerification.objects.filter(user=user, type="PASSWORD_RESET").latest("id")

        invalid = json_request(
            self.client,
            "post",
            "/api/v1/auth/password-reset/verify",
            {"email": user.email, "code": "000000"},
        )
        self.assertEqual(invalid.status_code, 400)

        verify = json_request(
            self.client,
            "post",
            "/api/v1/auth/password-reset/verify",
            {"email": user.email, "code": verification.code},
        )
        self.assertEqual(verify.status_code, 200)
        reset_token = verify.json()["data"]["resetToken"]

        same_password = json_request(
            self.client,
            "post",
            "/api/v1/auth/password-reset/confirm",
            {"resetToken": reset_token, "newPassword": "Password1!"},
        )
        self.assertEqual(same_password.status_code, 400)

        confirm = json_request(
            self.client,
            "post",
            "/api/v1/auth/password-reset/confirm",
            {"resetToken": reset_token, "newPassword": "Changed1!"},
        )
        self.assertEqual(confirm.status_code, 200)
        user.refresh_from_db()
        self.assertTrue(user.check_password("Changed1!"))
        self.assertIsNone(user.refresh_token)

    def test_social_callback_complete_link_and_unlink(self):
        redirect = self.client.get("/api/v1/auth/login/google")
        self.assertEqual(redirect.status_code, 302)
        state = parse_qs(urlparse(redirect["Location"]).query)["state"][0]

        new_user_callback = self.client.get(
            "/api/v1/auth/callback/google",
            {
                "code": "mock:email=new-social@example.com&name=소셜유저&id=google-new-user",
                "state": state,
            },
        )
        self.assertEqual(new_user_callback.status_code, 200)
        self.assertTrue(new_user_callback.json()["data"]["isNewUser"])

        complete = json_request(
            self.client,
            "post",
            "/api/v1/auth/social/complete",
            {"phone": "010-5555-6666", "nickname": "social-user"},
        )
        self.assertEqual(complete.status_code, 200)
        self.assertIn("accessToken", complete.json()["data"])

        existing = self.create_user(email="linked@example.com", nickname="linked-user")
        existing.email_verified = True
        existing.save(update_fields=["email_verified", "updated_at"])

        redirect2 = self.client.get("/api/v1/auth/login/kakao")
        state2 = parse_qs(urlparse(redirect2["Location"]).query)["state"][0]
        callback = self.client.get(
            "/api/v1/auth/callback/kakao",
            {
                "code": "mock:email=linked@example.com&name=기존유저&id=kakao-existing",
                "state": state2,
            },
        )
        self.assertEqual(callback.status_code, 200)
        self.assertFalse(callback.json()["data"]["isNewUser"])
        self.assertTrue(
            SocialAccount.objects.filter(user=existing, provider="kakao", deleted_at__isnull=True).exists()
        )

        link = json_request(
            self.client,
            "post",
            "/api/v1/auth/social/link",
            {"provider": "google", "socialToken": "mock:email=linked@example.com&id=google-link-1"},
            **auth_header_for(existing),
        )
        self.assertEqual(link.status_code, 200)
        self.assertEqual(link.json()["data"]["linkedProvider"], "google")

        unlink = self.client.delete(
            "/api/v1/auth/social/unlink/google",
            content_type="application/json",
            **auth_header_for(existing),
        )
        self.assertEqual(unlink.status_code, 200)

        passwordless = self.user_model.objects.create_user(
            email="passwordless@example.com",
            password=None,
            name="소셜만",
            phone="01077778888",
            nickname="passwordless",
        )
        passwordless.email_verified = True
        passwordless.save(update_fields=["email_verified", "updated_at"])
        SocialAccount.objects.create(
            user=passwordless,
            provider="naver",
            provider_user_id="naver-only",
        )
        cannot_unlink = self.client.delete(
            "/api/v1/auth/social/unlink/naver",
            content_type="application/json",
            **auth_header_for(passwordless),
        )
        self.assertEqual(cannot_unlink.status_code, 400)
        self.assertEqual(cannot_unlink.json()["error"]["code"], "CANNOT_UNLINK")
