from django.urls import path

from apps.catalog.views import (
    product_detail,
    product_image_collection,
    product_image_detail,
    product_option_collection,
    product_option_detail,
    product_spec_collection,
    products_collection,
)

urlpatterns = [
    path("", products_collection, name="products-collection"),
    path("<int:product_id>", product_detail, name="products-detail"),
    path("<int:product_id>/specs", product_spec_collection, name="products-specs-collection"),
    path("<int:product_id>/options", product_option_collection, name="products-options-collection"),
    path("<int:product_id>/options/<int:option_id>", product_option_detail, name="products-options-detail"),
    path("<int:product_id>/images", product_image_collection, name="products-images-collection"),
    path("<int:product_id>/images/<int:image_id>", product_image_detail, name="products-images-detail"),
]
