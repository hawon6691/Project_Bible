from __future__ import annotations

from django.db.models import ProtectedError

from apps.api.errors import ApiError
from apps.api.utils import require_fields
from apps.catalog.models import Category
from apps.catalog.repositories import CategoryRepository


class CategoryService:
    def __init__(self, repository: CategoryRepository | None = None) -> None:
        self.repository = repository or CategoryRepository()

    def list_tree(self) -> list[dict]:
        categories = list(self.repository.list_ordered())
        nodes: dict[int, dict] = {}
        roots: list[dict] = []

        for category in categories:
            nodes[category.id] = self._serialize_tree_node(category)

        for category in categories:
            node = nodes[category.id]
            if category.parent_id and category.parent_id in nodes:
                nodes[category.parent_id]["children"].append(node)
            else:
                roots.append(node)

        return roots

    def get_category(self, category_id: int) -> dict:
        category = self.repository.find_with_children(category_id)
        if category is None:
            raise ApiError("CATEGORY_NOT_FOUND", "카테고리를 찾을 수 없습니다.", 404)

        return {
            "id": category.id,
            "name": category.name,
            "parentId": category.parent_id,
            "sortOrder": category.sort_order,
            "children": [
                {
                    "id": child.id,
                    "name": child.name,
                    "sortOrder": child.sort_order,
                }
                for child in category.children.all()
            ],
            "createdAt": category.created_at.isoformat().replace("+00:00", "Z"),
        }

    def create_category(self, data: dict) -> dict:
        require_fields(data, ["name"])
        name = self._validate_name(data.get("name"))
        parent = self._resolve_parent(data.get("parentId"))
        sort_order = self._parse_sort_order(data.get("sortOrder", 0))
        category = self.repository.create(name=name, parent=parent, sort_order=sort_order)
        return self._serialize_category(category)

    def update_category(self, category_id: int, data: dict) -> dict:
        category = self.repository.find_by_id(category_id)
        if category is None:
            raise ApiError("CATEGORY_NOT_FOUND", "카테고리를 찾을 수 없습니다.", 404)

        updated_fields: list[str] = []
        if "name" in data:
            category.name = self._validate_name(data.get("name"))
            updated_fields.append("name")
        if "sortOrder" in data:
            category.sort_order = self._parse_sort_order(data.get("sortOrder"))
            updated_fields.append("sort_order")

        if updated_fields:
            updated_fields.append("updated_at")
            self.repository.save(category, update_fields=updated_fields)

        return self._serialize_category(category)

    def delete_category(self, category_id: int) -> dict:
        category = self.repository.find_by_id(category_id)
        if category is None:
            raise ApiError("CATEGORY_NOT_FOUND", "카테고리를 찾을 수 없습니다.", 404)
        if self.repository.has_children(category_id):
            raise ApiError("CATEGORY_HAS_CHILDREN", "하위 카테고리가 있어 삭제할 수 없습니다.", 400)

        try:
            self.repository.delete(category)
        except ProtectedError as exc:
            raise ApiError("CONFLICT", "연결된 리소스가 있어 카테고리를 삭제할 수 없습니다.", 409) from exc
        return {"message": "카테고리가 삭제되었습니다."}

    def _resolve_parent(self, raw_parent_id) -> Category | None:
        if raw_parent_id in (None, ""):
            return None
        parent_id = self._parse_int(raw_parent_id, "parentId")
        parent = self.repository.find_by_id(parent_id)
        if parent is None:
            raise ApiError("CATEGORY_NOT_FOUND", "카테고리를 찾을 수 없습니다.", 404)
        return parent

    def _validate_name(self, value) -> str:
        normalized = str(value or "").strip()
        if not normalized:
            raise ApiError("VALIDATION_ERROR", "카테고리명은 비워둘 수 없습니다.", 400)
        if len(normalized) > 50:
            raise ApiError("VALIDATION_ERROR", "카테고리명은 50자 이하여야 합니다.", 400)
        return normalized

    def _parse_sort_order(self, value) -> int:
        sort_order = self._parse_int(value, "sortOrder")
        if sort_order < 0:
            raise ApiError("VALIDATION_ERROR", "정렬 순서는 0 이상이어야 합니다.", 400)
        return sort_order

    def _parse_int(self, value, field_name: str) -> int:
        try:
            return int(value)
        except (TypeError, ValueError) as exc:
            raise ApiError("VALIDATION_ERROR", f"{field_name}는 정수여야 합니다.", 400) from exc

    def _serialize_tree_node(self, category: Category) -> dict:
        return {
            "id": category.id,
            "name": category.name,
            "sortOrder": category.sort_order,
            "children": [],
        }

    def _serialize_category(self, category: Category) -> dict:
        return {
            "id": category.id,
            "name": category.name,
            "parentId": category.parent_id,
            "sortOrder": category.sort_order,
            "createdAt": category.created_at.isoformat().replace("+00:00", "Z"),
        }
