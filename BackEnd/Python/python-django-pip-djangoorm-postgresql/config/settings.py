import os
import sys
from pathlib import Path

from dotenv import load_dotenv

BASE_DIR = Path(__file__).resolve().parent.parent
load_dotenv(BASE_DIR / ".env")


def get_env_bool(name: str, default: bool) -> bool:
    value = os.getenv(name)
    if value is None:
        return default
    return value.strip().lower() in {"1", "true", "yes", "on"}


def get_env_list(name: str, default: str = "") -> list[str]:
    raw = os.getenv(name, default)
    return [item.strip() for item in raw.split(",") if item.strip()]


def use_sqlite_for_tests() -> bool:
    raw = os.getenv("DJANGO_USE_SQLITE_FOR_TESTS")
    if raw is not None:
        return raw.strip().lower() in {"1", "true", "yes", "on"}
    return "test" in sys.argv


SECRET_KEY = os.getenv("DJANGO_SECRET_KEY", "pbshop-python-bootstrap-secret-key")
DEBUG = get_env_bool("DJANGO_DEBUG", True)
ALLOWED_HOSTS = get_env_list("DJANGO_ALLOWED_HOSTS", "127.0.0.1,localhost,testserver")


INSTALLED_APPS = [
    "django.contrib.admin",
    "django.contrib.auth",
    "django.contrib.contenttypes",
    "django.contrib.sessions",
    "django.contrib.messages",
    "django.contrib.staticfiles",
    "apps.auth.apps.PBShopAuthConfig",
    "apps.docs.apps.DocsConfig",
    "apps.users",
    "apps.catalog",
    "apps.pricing",
    "apps.cart",
    "apps.address",
    "apps.order",
    "apps.payment",
    "apps.commerce",
    "apps.health",
]

MIDDLEWARE = [
    "django.middleware.security.SecurityMiddleware",
    "django.contrib.sessions.middleware.SessionMiddleware",
    "django.middleware.common.CommonMiddleware",
    "django.middleware.csrf.CsrfViewMiddleware",
    "django.contrib.auth.middleware.AuthenticationMiddleware",
    "django.contrib.messages.middleware.MessageMiddleware",
    "django.middleware.clickjacking.XFrameOptionsMiddleware",
]

ROOT_URLCONF = "config.urls"

TEMPLATES = [
    {
        "BACKEND": "django.template.backends.django.DjangoTemplates",
        "DIRS": [],
        "APP_DIRS": True,
        "OPTIONS": {
            "context_processors": [
                "django.template.context_processors.request",
                "django.contrib.auth.context_processors.auth",
                "django.contrib.messages.context_processors.messages",
            ],
        },
    },
]

WSGI_APPLICATION = "config.wsgi.application"
ASGI_APPLICATION = "config.asgi.application"


if use_sqlite_for_tests():
    DATABASES = {
        "default": {
            "ENGINE": "django.db.backends.sqlite3",
            "NAME": ":memory:",
        }
    }
else:
    DATABASES = {
        "default": {
            "ENGINE": "django.db.backends.postgresql",
            "NAME": os.getenv("POSTGRES_DB", "pbshop"),
            "USER": os.getenv("POSTGRES_USER", "postgres"),
            "PASSWORD": os.getenv("POSTGRES_PASSWORD", "postgres"),
            "HOST": os.getenv("POSTGRES_HOST", "127.0.0.1"),
            "PORT": os.getenv("POSTGRES_PORT", "5432"),
            "CONN_MAX_AGE": int(os.getenv("POSTGRES_CONN_MAX_AGE", "60")),
        }
    }


AUTH_PASSWORD_VALIDATORS = [
    {
        "NAME": "django.contrib.auth.password_validation.UserAttributeSimilarityValidator",
    },
    {
        "NAME": "django.contrib.auth.password_validation.MinimumLengthValidator",
    },
    {
        "NAME": "django.contrib.auth.password_validation.CommonPasswordValidator",
    },
    {
        "NAME": "django.contrib.auth.password_validation.NumericPasswordValidator",
    },
]


LANGUAGE_CODE = "ko-kr"
TIME_ZONE = "Asia/Seoul"

USE_I18N = True
USE_TZ = True


STATIC_URL = "/static/"
STATIC_ROOT = BASE_DIR / "staticfiles"
MEDIA_URL = os.getenv("DJANGO_MEDIA_URL", "/media/")
MEDIA_ROOT = BASE_DIR / os.getenv("DJANGO_MEDIA_ROOT", "media")

APPEND_SLASH = True

DEFAULT_AUTO_FIELD = "django.db.models.BigAutoField"
AUTH_USER_MODEL = "users.CustomUser"

JWT_ACCESS_SECRET = os.getenv(
    "JWT_ACCESS_SECRET",
    "pbshop-python-access-secret-key-2026",
)
JWT_REFRESH_SECRET = os.getenv(
    "JWT_REFRESH_SECRET",
    "pbshop-python-refresh-secret-key-2026",
)
JWT_ACCESS_EXPIRES_IN_SECONDS = int(os.getenv("JWT_ACCESS_EXPIRES_IN_SECONDS", "1800"))
JWT_REFRESH_EXPIRES_IN_SECONDS = int(os.getenv("JWT_REFRESH_EXPIRES_IN_SECONDS", "604800"))
JWT_SIGNUP_TOKEN_EXPIRES_IN_SECONDS = int(os.getenv("JWT_SIGNUP_TOKEN_EXPIRES_IN_SECONDS", "600"))
PASSWORD_RESET_TOKEN_EXPIRES_IN_SECONDS = int(
    os.getenv("PASSWORD_RESET_TOKEN_EXPIRES_IN_SECONDS", "300")
)
EMAIL_VERIFICATION_CODE_EXPIRES_IN_SECONDS = int(
    os.getenv("EMAIL_VERIFICATION_CODE_EXPIRES_IN_SECONDS", "600")
)
EMAIL_VERIFICATION_MAX_ATTEMPTS = int(os.getenv("EMAIL_VERIFICATION_MAX_ATTEMPTS", "5"))
EMAIL_VERIFICATION_RESEND_INTERVAL_SECONDS = int(
    os.getenv("EMAIL_VERIFICATION_RESEND_INTERVAL_SECONDS", "60")
)
PBSHOP_DEFAULT_PROFILE_IMAGE_URL = os.getenv(
    "PBSHOP_DEFAULT_PROFILE_IMAGE_URL",
    "/media/profiles/default.png",
)
PBSHOP_PROFILE_IMAGE_DIR = os.getenv("PBSHOP_PROFILE_IMAGE_DIR", "profiles")
PBSHOP_SOCIAL_SIGNUP_COOKIE_NAME = os.getenv(
    "PBSHOP_SOCIAL_SIGNUP_COOKIE_NAME",
    "pbshop_social_signup_token",
)
PBSHOP_SOCIAL_STATE_EXPIRES_IN_SECONDS = int(
    os.getenv("PBSHOP_SOCIAL_STATE_EXPIRES_IN_SECONDS", "600")
)

EMAIL_BACKEND = os.getenv(
    "EMAIL_BACKEND",
    "django.core.mail.backends.locmem.EmailBackend",
)
DEFAULT_FROM_EMAIL = os.getenv("DEFAULT_FROM_EMAIL", "no-reply@pbshop.local")
