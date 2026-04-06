from django.urls import path

from apps.catalog.views import spec_compare, spec_definition_detail, spec_definitions_collection

urlpatterns = [
    path("definitions", spec_definitions_collection, name="spec-definitions-collection"),
    path("definitions/<int:definition_id>", spec_definition_detail, name="spec-definitions-detail"),
    path("compare", spec_compare, name="spec-compare"),
]
