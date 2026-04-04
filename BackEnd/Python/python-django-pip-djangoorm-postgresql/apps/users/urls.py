from django.urls import path

from apps.users.views import (
    users_collection,
    users_me,
    users_me_profile,
    users_me_profile_image,
    users_profile,
    users_status,
)

urlpatterns = [
    path("me", users_me, name="users-me"),
    path("me/profile", users_me_profile, name="users-me-profile"),
    path("me/profile-image", users_me_profile_image, name="users-me-profile-image"),
    path("<int:user_id>/profile", users_profile, name="users-profile"),
    path("<int:user_id>/status", users_status, name="users-status"),
    path("", users_collection, name="users-collection"),
]
