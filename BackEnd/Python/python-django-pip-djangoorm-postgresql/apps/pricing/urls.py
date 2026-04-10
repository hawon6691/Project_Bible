from django.urls import path

from apps.pricing.views import seller_detail, sellers_collection

urlpatterns = [
    path("", sellers_collection, name="sellers-collection"),
    path("<int:seller_id>", seller_detail, name="seller-detail"),
]
