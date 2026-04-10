from django.conf import settings
from django.db import models
from django.db.models import Q


class VerificationType(models.TextChoices):
    SIGNUP = "SIGNUP", "Signup"
    PASSWORD_RESET = "PASSWORD_RESET", "Password reset"


class SocialProvider(models.TextChoices):
    GOOGLE = "google", "Google"
    NAVER = "naver", "Naver"
    KAKAO = "kakao", "Kakao"
    FACEBOOK = "facebook", "Facebook"
    INSTAGRAM = "instagram", "Instagram"


class EmailVerification(models.Model):
    class Meta:
        db_table = "email_verifications"
        indexes = [
            models.Index(fields=["user", "type"], name="idx_email_verif_user_type"),
            models.Index(fields=["user", "code", "type"], name="idx_email_verif_code"),
            models.Index(fields=["expires_at"], name="idx_email_verif_expires"),
        ]

    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name="email_verifications",
        db_index=False,
    )
    type = models.CharField(max_length=20, choices=VerificationType.choices)
    code = models.CharField(max_length=6)
    expires_at = models.DateTimeField()
    attempt_count = models.IntegerField(default=0)
    is_used = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self) -> str:
        return f"{self.user_id}:{self.type}:{self.code}"


class SocialAccount(models.Model):
    class Meta:
        db_table = "social_accounts"
        constraints = [
            models.UniqueConstraint(
                fields=["provider", "provider_user_id"],
                name="uq_social_accounts_provider_user",
                condition=Q(deleted_at__isnull=True),
            ),
        ]
        indexes = [
            models.Index(fields=["user"], name="idx_social_accounts_user"),
            models.Index(fields=["provider"], name="idx_social_accounts_provider"),
        ]

    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name="social_accounts",
        db_index=False,
    )
    provider = models.CharField(max_length=20, choices=SocialProvider.choices)
    provider_user_id = models.CharField(max_length=255)
    provider_email = models.EmailField(max_length=255, null=True, blank=True)
    provider_name = models.CharField(max_length=100, null=True, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    deleted_at = models.DateTimeField(null=True, blank=True)

    def __str__(self) -> str:
        return f"{self.user_id}:{self.provider}:{self.provider_user_id}"
