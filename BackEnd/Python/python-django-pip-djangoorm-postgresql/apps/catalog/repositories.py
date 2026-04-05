from __future__ import annotations

from django.db.models import Prefetch

from apps.catalog.models import Category


class CategoryRepository:
    def list_ordered(self):
        return Category.objects.select_related("parent").order_by("sort_order", "id")

    def find_by_id(self, category_id: int) -> Category | None:
        return Category.objects.select_related("parent").filter(id=category_id).first()

    def find_with_children(self, category_id: int) -> Category | None:
        children_queryset = Category.objects.order_by("sort_order", "id")
        return (
            Category.objects.select_related("parent")
            .prefetch_related(Prefetch("children", queryset=children_queryset))
            .filter(id=category_id)
            .first()
        )

    def has_children(self, category_id: int) -> bool:
        return Category.objects.filter(parent_id=category_id).exists()

    def create(self, *, name: str, parent: Category | None, sort_order: int = 0) -> Category:
        return Category.objects.create(
            name=name,
            parent=parent,
            sort_order=sort_order,
        )

    def save(self, category: Category, *, update_fields: list[str] | None = None) -> Category:
        category.save(update_fields=update_fields)
        return category

    def delete(self, category: Category) -> None:
        category.delete()
