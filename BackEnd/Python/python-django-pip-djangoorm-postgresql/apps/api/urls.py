from django.urls import include, path

from apps.api.views import api_root
from apps.users.views import users_collection

urlpatterns = [
    path("", api_root, name="api-root"),
    path("auth/", include("apps.auth.urls")),
    path("users", users_collection, name="users-collection-no-slash"),
    path("users/", include("apps.users.urls")),
]
