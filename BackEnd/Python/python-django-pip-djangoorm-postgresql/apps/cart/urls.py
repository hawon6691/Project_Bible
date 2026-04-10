from django.urls import path

from apps.cart.views import cart_collection, cart_item_detail


urlpatterns = [
    path("", cart_collection, name="cart-collection"),
    path("<int:item_id>", cart_item_detail, name="cart-item-detail"),
]
