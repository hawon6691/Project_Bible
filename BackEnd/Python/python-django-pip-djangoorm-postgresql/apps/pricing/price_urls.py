from django.urls import path

from apps.pricing.views import price_entry_detail

urlpatterns = [
    path("<int:price_id>", price_entry_detail, name="price-entry-detail"),
]
