from django.urls import path

from apps.pricing.views import product_price_history_detail, product_prices_collection

urlpatterns = [
    path("<int:product_id>/prices", product_prices_collection, name="product-prices-collection"),
    path("<int:product_id>/price-history", product_price_history_detail, name="product-price-history-detail"),
]
