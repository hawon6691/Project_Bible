from django.urls import include, path

from apps.api.views import api_root
from apps.cart.views import cart_collection
from apps.catalog.views import categories_collection
from apps.catalog.views import products_collection
from apps.pricing.views import price_alerts_collection, sellers_collection
from apps.users.views import users_collection

urlpatterns = [
    path("", api_root, name="api-root"),
    path("auth/", include("apps.auth.urls")),
    path("cart", cart_collection, name="cart-collection-no-slash"),
    path("cart/", include("apps.cart.urls")),
    path("categories", categories_collection, name="categories-collection-no-slash"),
    path("categories/", include("apps.catalog.urls")),
    path("products", products_collection, name="products-collection-no-slash"),
    path("products/", include("apps.catalog.product_urls")),
    path("products/", include("apps.pricing.product_urls")),
    path("price-alerts", price_alerts_collection, name="price-alerts-collection-no-slash"),
    path("price-alerts/", include("apps.pricing.alert_urls")),
    path("specs/", include("apps.catalog.spec_urls")),
    path("prices/", include("apps.pricing.price_urls")),
    path("sellers", sellers_collection, name="sellers-collection-no-slash"),
    path("sellers/", include("apps.pricing.urls")),
    path("users", users_collection, name="users-collection-no-slash"),
    path("users/", include("apps.users.urls")),
]
