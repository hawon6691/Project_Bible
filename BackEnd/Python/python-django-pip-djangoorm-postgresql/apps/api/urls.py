from django.urls import path

from apps.api.views import api_root

urlpatterns = [
    path("", api_root, name="api-root"),
]
