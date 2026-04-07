from django.urls import path

from apps.pricing.views import price_alert_detail, price_alerts_collection

urlpatterns = [
    path("", price_alerts_collection, name="price-alerts-collection"),
    path("<int:alert_id>", price_alert_detail, name="price-alert-detail"),
]
