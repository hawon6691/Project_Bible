from django.contrib.auth.base_user import AbstractBaseUser, BaseUserManager
from django.contrib.auth.models import PermissionsMixin
from django.db import models
from django.utils import timezone


class UserRole(models.TextChoices):
    USER = "USER", "User"
    SELLER = "SELLER", "Seller"
    ADMIN = "ADMIN", "Admin"


class UserStatus(models.TextChoices):
    ACTIVE = "ACTIVE", "Active"
    INACTIVE = "INACTIVE", "Inactive"
    BLOCKED = "BLOCKED", "Blocked"


class UserManager(BaseUserManager):
    use_in_migrations = True

    def _create_user(self, email: str, password: str | None, **extra_fields):
        if not email:
            raise ValueError("The email field must be set.")

        email = self.normalize_email(email)
        user = self.model(email=email, **extra_fields)

        if password:
            user.set_password(password)
        else:
            user.set_unusable_password()

        user.save(using=self._db)
        return user

    def create_user(self, email: str, password: str | None = None, **extra_fields):
        extra_fields.setdefault("role", UserRole.USER)
        extra_fields.setdefault("status", UserStatus.ACTIVE)
        return self._create_user(email, password, **extra_fields)

    def create_superuser(self, email: str, password: str, **extra_fields):
        extra_fields.setdefault("role", UserRole.ADMIN)
        extra_fields.setdefault("status", UserStatus.ACTIVE)
        extra_fields.setdefault("email_verified", True)
        extra_fields.setdefault("email_verified_at", timezone.now())
        extra_fields.setdefault("is_superuser", True)

        if extra_fields.get("role") != UserRole.ADMIN:
            raise ValueError("Superuser must have role=ADMIN.")
        if extra_fields.get("is_superuser") is not True:
            raise ValueError("Superuser must have is_superuser=True.")

        return self._create_user(email, password, **extra_fields)


class CustomUser(AbstractBaseUser, PermissionsMixin):
    class Meta:
        db_table = "users"
        indexes = [
            models.Index(fields=["status"], name="idx_users_status"),
            models.Index(fields=["role"], name="idx_users_role"),
        ]

    email = models.EmailField(max_length=255, unique=True)
    name = models.CharField(max_length=50)
    phone = models.CharField(max_length=20)
    role = models.CharField(max_length=10, choices=UserRole.choices, default=UserRole.USER)
    status = models.CharField(
        max_length=10,
        choices=UserStatus.choices,
        default=UserStatus.ACTIVE,
    )
    email_verified = models.BooleanField(default=False)
    email_verified_at = models.DateTimeField(null=True, blank=True)
    nickname = models.CharField(max_length=30, unique=True)
    bio = models.CharField(max_length=200, null=True, blank=True)
    profile_image_url = models.CharField(max_length=500, null=True, blank=True)
    search_history_enabled = models.BooleanField(default=True)
    point = models.IntegerField(default=0)
    preferred_locale = models.CharField(max_length=5, default="ko")
    preferred_currency = models.CharField(max_length=3, default="KRW")
    refresh_token = models.CharField(max_length=500, null=True, blank=True)
    last_login = models.DateTimeField(db_column="last_login_at", null=True, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    deleted_at = models.DateTimeField(null=True, blank=True)

    objects = UserManager()

    USERNAME_FIELD = "email"
    REQUIRED_FIELDS = ["name", "phone", "nickname"]

    def __str__(self) -> str:
        return self.email

    @property
    def is_active(self) -> bool:
        return self.status == UserStatus.ACTIVE

    @property
    def is_staff(self) -> bool:
        return self.role == UserRole.ADMIN

    @property
    def last_login_at(self):
        return self.last_login

    @last_login_at.setter
    def last_login_at(self, value):
        self.last_login = value

