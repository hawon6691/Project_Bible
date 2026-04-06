from django.urls import path

from apps.pricing.views import product_prices_collection

urlpatterns = [
    path("<int:product_id>/prices", product_prices_collection, name="product-prices-collection"),
]
