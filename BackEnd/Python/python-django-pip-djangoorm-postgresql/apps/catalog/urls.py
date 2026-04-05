from django.urls import path

from apps.catalog.views import categories_collection, category_detail

urlpatterns = [
    path("", categories_collection, name="categories-collection"),
    path("<int:category_id>", category_detail, name="categories-detail"),
]
