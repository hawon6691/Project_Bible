from django.urls import include, path

from apps.api.views import api_root
from apps.catalog.views import categories_collection
from apps.catalog.views import products_collection
from apps.users.views import users_collection

urlpatterns = [
    path("", api_root, name="api-root"),
    path("auth/", include("apps.auth.urls")),
    path("categories", categories_collection, name="categories-collection-no-slash"),
    path("categories/", include("apps.catalog.urls")),
    path("products", products_collection, name="products-collection-no-slash"),
    path("products/", include("apps.catalog.product_urls")),
    path("users", users_collection, name="users-collection-no-slash"),
    path("users/", include("apps.users.urls")),
]
