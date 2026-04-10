from django.urls import path

from apps.auth.views import (
    auth_callback,
    auth_login,
    auth_login_provider,
    auth_logout,
    auth_password_reset_confirm,
    auth_password_reset_request,
    auth_password_reset_verify,
    auth_refresh,
    auth_resend_verification,
    auth_signup,
    auth_social_complete,
    auth_social_link,
    auth_social_unlink,
    auth_verify_email,
)

urlpatterns = [
    path("signup", auth_signup, name="auth-signup"),
    path("verify-email", auth_verify_email, name="auth-verify-email"),
    path("resend-verification", auth_resend_verification, name="auth-resend-verification"),
    path("login", auth_login, name="auth-login"),
    path("logout", auth_logout, name="auth-logout"),
    path("refresh", auth_refresh, name="auth-refresh"),
    path("password-reset/request", auth_password_reset_request, name="auth-password-reset-request"),
    path("password-reset/verify", auth_password_reset_verify, name="auth-password-reset-verify"),
    path("password-reset/confirm", auth_password_reset_confirm, name="auth-password-reset-confirm"),
    path("login/<str:provider>", auth_login_provider, name="auth-login-provider"),
    path("callback/<str:provider>", auth_callback, name="auth-callback-provider"),
    path("social/complete", auth_social_complete, name="auth-social-complete"),
    path("social/link", auth_social_link, name="auth-social-link"),
    path("social/unlink/<str:provider>", auth_social_unlink, name="auth-social-unlink"),
]
