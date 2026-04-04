from django.urls import path

from apps.docs.views import openapi_view, swagger_redirect_view, swagger_ui_view

urlpatterns = [
    path("openapi", openapi_view, name="docs-openapi"),
    path("swagger", swagger_redirect_view, name="docs-swagger"),
    path("swagger-ui/index.html", swagger_ui_view, name="docs-swagger-ui"),
]
