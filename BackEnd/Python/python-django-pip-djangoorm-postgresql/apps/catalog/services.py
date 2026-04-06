from __future__ import annotations

import json
import os
import uuid
from decimal import Decimal, ROUND_HALF_UP
from pathlib import Path

from django.conf import settings
from django.core.files.uploadedfile import UploadedFile
from django.db.models import ProtectedError
from django.db.models.functions import Coalesce
from django.db import transaction
from django.utils import timezone
from PIL import Image, UnidentifiedImageError

from apps.api.errors import ApiError
from apps.api.pagination import build_pagination_meta, parse_pagination
from apps.api.utils import require_fields
from apps.catalog.models import (
    Category,
    Product,
    ProductImage,
    ProductOption,
    ProductSpec,
    ProductStatus,
    SpecDataType,
    SpecDefinition,
    SpecDefinitionType,
)
from apps.catalog.repositories import CategoryRepository, ProductRepository, SpecRepository


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


class SpecService:
    def __init__(
        self,
        spec_repository: SpecRepository | None = None,
        product_repository: ProductRepository | None = None,
        category_repository: CategoryRepository | None = None,
    ) -> None:
        self.spec_repository = spec_repository or SpecRepository()
        self.product_repository = product_repository or ProductRepository()
        self.category_repository = category_repository or CategoryRepository()

    def list_definitions(self, querydict) -> list[dict]:
        category_id = querydict.get("categoryId", "").strip()
        resolved_category_id = self._parse_positive_int(category_id, "categoryId") if category_id else None
        definitions = self.spec_repository.list_definitions(category_id=resolved_category_id)
        return [self._serialize_definition(item) for item in definitions]

    def create_definition(self, data) -> dict:
        normalized = self._normalize_definition_payload(data, partial=False)
        definition = self.spec_repository.create_definition(**normalized)
        return self._serialize_definition(definition)

    def update_definition(self, definition_id: int, data) -> dict:
        definition = self.spec_repository.find_definition_by_id(definition_id)
        if definition is None:
            raise ApiError("SPEC_DEFINITION_NOT_FOUND", "스펙 정의를 찾을 수 없습니다.", 404)

        if not isinstance(data, dict):
            raise ApiError("VALIDATION_ERROR", "요청 본문은 객체여야 합니다.", 400)
        if not data:
            return self._serialize_definition(definition)

        normalized = self._normalize_definition_payload(data, partial=True, current=definition)
        for field_name, value in normalized.items():
            setattr(definition, field_name, value)
        self.spec_repository.save_definition(definition, update_fields=list(normalized.keys()))
        return self._serialize_definition(definition)

    def delete_definition(self, definition_id: int) -> dict:
        definition = self.spec_repository.find_definition_by_id(definition_id)
        if definition is None:
            raise ApiError("SPEC_DEFINITION_NOT_FOUND", "스펙 정의를 찾을 수 없습니다.", 404)
        if self.spec_repository.definition_has_product_specs(definition.id):
            raise ApiError("CONFLICT", "상품에 연결된 스펙 정의는 삭제할 수 없습니다.", 409)
        self.spec_repository.delete_definition(definition)
        return {"message": "스펙 정의가 삭제되었습니다."}

    def get_product_specs(self, product_id: int) -> list[dict]:
        self._ensure_product(product_id)
        return self.serialize_product_specs(self.spec_repository.list_product_specs(product_id))

    def replace_product_specs(self, product_id: int, data) -> list[dict]:
        product = self._ensure_product(product_id)
        if not isinstance(data, list):
            raise ApiError("VALIDATION_ERROR", "요청 본문은 배열이어야 합니다.", 400)

        normalized_specs: list[dict] = []
        seen_definition_ids: set[int] = set()
        for item in data:
            if not isinstance(item, dict):
                raise ApiError("VALIDATION_ERROR", "상품 스펙 항목 형식이 올바르지 않습니다.", 400)
            require_fields(item, ["specDefinitionId", "value"])

            definition = self._resolve_definition(item.get("specDefinitionId"))
            if definition.category_id != product.category_id:
                raise ApiError("VALIDATION_ERROR", "상품 카테고리와 일치하는 스펙 정의만 설정할 수 있습니다.", 400)
            if definition.id in seen_definition_ids:
                raise ApiError("VALIDATION_ERROR", "동일한 스펙 정의를 중복으로 설정할 수 없습니다.", 400)
            seen_definition_ids.add(definition.id)

            value = self._validate_required_text(item.get("value"), field_name="value", max_length=200)
            numeric_value = self._normalize_product_spec_numeric_value(item.get("numericValue"), definition)
            if definition.type == SpecDefinitionType.SELECT:
                options = definition.options or []
                if value not in options:
                    raise ApiError("VALIDATION_ERROR", "SELECT 타입 스펙 값이 정의된 옵션과 일치하지 않습니다.", 400)

            normalized_specs.append(
                {
                    "product": product,
                    "spec_definition": definition,
                    "value": value,
                    "numeric_value": numeric_value,
                }
            )

        with transaction.atomic():
            self.spec_repository.delete_product_specs(product.id)
            for item in normalized_specs:
                self.spec_repository.create_product_spec(**item)

        return self.get_product_specs(product.id)

    def compare_products(self, data) -> dict:
        if not isinstance(data, dict):
            raise ApiError("VALIDATION_ERROR", "요청 본문은 객체여야 합니다.", 400)

        product_ids = data.get("productIds")
        if not isinstance(product_ids, list):
            raise ApiError("VALIDATION_ERROR", "productIds는 배열이어야 합니다.", 400)
        if len(product_ids) < 2 or len(product_ids) > 4:
            raise ApiError("VALIDATION_ERROR", "비교 상품은 2개 이상 4개 이하로 요청해야 합니다.", 400)

        ordered_product_ids = [self._parse_positive_int(item, "productIds") for item in product_ids]
        unique_product_ids = list(dict.fromkeys(ordered_product_ids))
        products = list(self.spec_repository.list_compare_products(unique_product_ids))
        product_by_id = {product.id: product for product in products}
        if len(product_by_id) != len(unique_product_ids):
            raise ApiError("PRODUCT_NOT_FOUND", "비교할 상품을 찾을 수 없습니다.", 404)

        ordered_products = [product_by_id[product_id] for product_id in ordered_product_ids]
        comparable_meta: dict[str, dict] = {}
        value_maps: dict[int, dict[str, str]] = {}

        for product in ordered_products:
            value_maps[product.id] = {}
            for item in product.product_specs.all():
                definition = item.spec_definition
                if not definition.is_comparable:
                    continue
                name = definition.name
                if name not in comparable_meta:
                    comparable_meta[name] = {
                        "sortOrder": definition.sort_order,
                        "definitionId": definition.id,
                    }
                value_maps[product.id][name] = item.value

        ordered_spec_names = sorted(
            comparable_meta.keys(),
            key=lambda name: (
                comparable_meta[name]["sortOrder"],
                comparable_meta[name]["definitionId"],
                name,
            ),
        )

        return {
            "products": [self._serialize_compare_product(product) for product in ordered_products],
            "specs": [
                {
                    "name": spec_name,
                    "values": [value_maps[product.id].get(spec_name, "-") for product in ordered_products],
                }
                for spec_name in ordered_spec_names
            ],
        }

    @staticmethod
    def parse_filter_payload(raw_specs: str) -> list[dict]:
        try:
            spec_filters = json.loads(raw_specs)
        except json.JSONDecodeError as exc:
            raise ApiError("VALIDATION_ERROR", "specs는 올바른 JSON 문자열이어야 합니다.", 400) from exc

        if not isinstance(spec_filters, dict):
            raise ApiError("VALIDATION_ERROR", "specs는 key/value 객체여야 합니다.", 400)

        normalized_filters: list[dict] = []
        for spec_name, spec_value in spec_filters.items():
            normalized_name = str(spec_name or "").strip()
            if not normalized_name or spec_value in (None, ""):
                raise ApiError("VALIDATION_ERROR", "specs 필터는 비어 있을 수 없습니다.", 400)
            if isinstance(spec_value, (dict, list)):
                raise ApiError("VALIDATION_ERROR", "specs 필터 값은 문자열, 숫자, 불리언만 허용됩니다.", 400)

            numeric_value = None
            if isinstance(spec_value, bool):
                normalized_value = "true" if spec_value else "false"
            elif isinstance(spec_value, (int, float, Decimal)):
                numeric_value = Decimal(str(spec_value))
                normalized_value = str(spec_value)
            else:
                normalized_value = str(spec_value).strip()
                if not normalized_value:
                    raise ApiError("VALIDATION_ERROR", "specs 필터는 비어 있을 수 없습니다.", 400)

            normalized_filters.append(
                {
                    "name": normalized_name,
                    "value": normalized_value,
                    "numericValue": numeric_value,
                }
            )
        return normalized_filters

    @staticmethod
    def serialize_product_specs(specs) -> list[dict]:
        sorted_specs = sorted(
            specs,
            key=lambda item: (item.spec_definition.sort_order, item.spec_definition.id, item.id),
        )
        return [
            {
                "id": item.id,
                "specDefinitionId": item.spec_definition_id,
                "name": item.spec_definition.name,
                "value": item.value,
                "numericValue": float(item.numeric_value) if item.numeric_value is not None else None,
                "unit": item.spec_definition.unit,
            }
            for item in sorted_specs
        ]

    def _normalize_definition_payload(self, data, *, partial: bool, current: SpecDefinition | None = None) -> dict:
        if not isinstance(data, dict):
            raise ApiError("VALIDATION_ERROR", "요청 본문은 객체여야 합니다.", 400)

        if not partial:
            require_fields(data, ["categoryId", "name", "type"])

        raw_type = data.get("type", current.type if current else None)
        spec_type = self._parse_spec_type(raw_type)

        category = current.category if current else None
        if not partial or "categoryId" in data:
            category = self._resolve_category(data.get("categoryId"))

        name = current.name if current else None
        if not partial or "name" in data:
            name = self._validate_required_text(data.get("name"), field_name="name", max_length=50)

        options_value = data.get("options", current.options if current else None)
        options = self._normalize_definition_options(options_value, spec_type)

        unit = current.unit if current else None
        if not partial or "unit" in data:
            unit = self._parse_optional_string(data.get("unit"), "unit", 20)

        is_comparable = current.is_comparable if current else True
        if not partial or "isComparable" in data:
            is_comparable = self._parse_bool(data.get("isComparable"), default=True)

        raw_data_type = data.get("dataType", current.data_type if current else self._default_data_type_for_type(spec_type))
        data_type = self._parse_data_type(raw_data_type)
        if spec_type == SpecDefinitionType.NUMBER:
            data_type = SpecDataType.NUMBER

        sort_order = current.sort_order if current else 0
        if not partial or "sortOrder" in data:
            sort_order = self._parse_non_negative_int(data.get("sortOrder", 0), "sortOrder")

        return {
            "category": category,
            "name": name,
            "type": spec_type,
            "options": options,
            "unit": unit,
            "is_comparable": is_comparable,
            "data_type": data_type,
            "sort_order": sort_order,
        }

    def _normalize_definition_options(self, value, spec_type: str) -> list[str] | None:
        if spec_type != SpecDefinitionType.SELECT:
            return None
        if not isinstance(value, list) or not value:
            raise ApiError("VALIDATION_ERROR", "SELECT 타입 스펙 정의는 비어 있지 않은 options 배열이 필요합니다.", 400)

        normalized_options: list[str] = []
        for item in value:
            normalized_item = str(item or "").strip()
            if not normalized_item:
                raise ApiError("VALIDATION_ERROR", "스펙 옵션 값은 비워둘 수 없습니다.", 400)
            normalized_options.append(normalized_item)
        return normalized_options

    def _normalize_product_spec_numeric_value(self, value, definition: SpecDefinition):
        if definition.data_type != SpecDataType.NUMBER:
            return None
        if value in (None, ""):
            raise ApiError("VALIDATION_ERROR", "숫자형 스펙은 numericValue가 필요합니다.", 400)
        try:
            return Decimal(str(value))
        except Exception as exc:
            raise ApiError("VALIDATION_ERROR", "numericValue는 숫자여야 합니다.", 400) from exc

    def _serialize_definition(self, definition: SpecDefinition) -> dict:
        return {
            "id": definition.id,
            "categoryId": definition.category_id,
            "name": definition.name,
            "type": definition.type,
            "options": definition.options,
            "unit": definition.unit,
            "isComparable": definition.is_comparable,
            "dataType": definition.data_type,
            "sortOrder": definition.sort_order,
        }

    def _serialize_compare_product(self, product: Product) -> dict:
        return {
            "id": product.id,
            "name": product.name,
            "thumbnailUrl": product.thumbnail_url,
            "lowestPrice": product.lowest_price if product.lowest_price is not None else product.price,
        }

    def _resolve_category(self, raw_category_id) -> Category:
        category_id = self._parse_positive_int(raw_category_id, "categoryId")
        category = self.category_repository.find_by_id(category_id)
        if category is None:
            raise ApiError("CATEGORY_NOT_FOUND", "카테고리를 찾을 수 없습니다.", 404)
        return category

    def _resolve_definition(self, raw_definition_id) -> SpecDefinition:
        definition_id = self._parse_positive_int(raw_definition_id, "specDefinitionId")
        definition = self.spec_repository.find_definition_by_id(definition_id)
        if definition is None:
            raise ApiError("SPEC_DEFINITION_NOT_FOUND", "스펙 정의를 찾을 수 없습니다.", 404)
        return definition

    def _ensure_product(self, product_id: int) -> Product:
        product = self.product_repository.find_by_id(product_id)
        if product is None:
            raise ApiError("PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다.", 404)
        return product

    def _validate_required_text(self, value, *, field_name: str, max_length: int) -> str:
        normalized = str(value or "").strip()
        if not normalized:
            raise ApiError("VALIDATION_ERROR", f"{field_name}은(는) 비워둘 수 없습니다.", 400)
        if len(normalized) > max_length:
            raise ApiError("VALIDATION_ERROR", f"{field_name}은(는) {max_length}자 이하여야 합니다.", 400)
        return normalized

    def _parse_optional_string(self, value, field_name: str, max_length: int) -> str | None:
        if value in (None, ""):
            return None
        normalized = str(value).strip()
        if len(normalized) > max_length:
            raise ApiError("VALIDATION_ERROR", f"{field_name}은(는) {max_length}자 이하여야 합니다.", 400)
        return normalized or None

    def _parse_bool(self, value, *, default: bool) -> bool:
        if value in (None, ""):
            return default
        if isinstance(value, bool):
            return value
        normalized = str(value).strip().lower()
        if normalized in {"1", "true", "yes", "on"}:
            return True
        if normalized in {"0", "false", "no", "off"}:
            return False
        raise ApiError("VALIDATION_ERROR", "불리언 값 형식이 올바르지 않습니다.", 400)

    def _parse_non_negative_int(self, value, field_name: str) -> int:
        try:
            parsed = int(value)
        except (TypeError, ValueError) as exc:
            raise ApiError("VALIDATION_ERROR", f"{field_name}는 정수여야 합니다.", 400) from exc
        if parsed < 0:
            raise ApiError("VALIDATION_ERROR", f"{field_name}는 0 이상이어야 합니다.", 400)
        return parsed

    def _parse_positive_int(self, value, field_name: str) -> int:
        parsed = self._parse_non_negative_int(value, field_name)
        if parsed < 1:
            raise ApiError("VALIDATION_ERROR", f"{field_name}는 1 이상이어야 합니다.", 400)
        return parsed

    def _parse_spec_type(self, value) -> str:
        normalized = str(value or "").strip().upper()
        if normalized not in SpecDefinitionType.values:
            raise ApiError("VALIDATION_ERROR", "유효하지 않은 스펙 type입니다.", 400)
        return normalized

    def _parse_data_type(self, value) -> str:
        normalized = str(value or "").strip().upper()
        if normalized not in SpecDataType.values:
            raise ApiError("VALIDATION_ERROR", "유효하지 않은 dataType입니다.", 400)
        return normalized

    def _default_data_type_for_type(self, spec_type: str) -> str:
        if spec_type == SpecDefinitionType.NUMBER:
            return SpecDataType.NUMBER
        return SpecDataType.STRING


class ProductService:
    SORT_VALUES = {
        "newest",
        "popularity",
        "price_asc",
        "price_desc",
        "rating_desc",
        "rating_asc",
    }

    def __init__(
        self,
        product_repository: ProductRepository | None = None,
        category_repository: CategoryRepository | None = None,
    ) -> None:
        self.product_repository = product_repository or ProductRepository()
        self.category_repository = category_repository or CategoryRepository()

    def list_products(self, querydict) -> tuple[list[dict], dict]:
        page, limit = parse_pagination(querydict)
        queryset = self.product_repository.list_queryset().annotate(
            effective_price=Coalesce("lowest_price", "price")
        )

        category_id = querydict.get("categoryId", "").strip()
        if category_id:
            queryset = queryset.filter(category_id=self._parse_positive_int(category_id, "categoryId"))

        search = querydict.get("search", "").strip()
        if search:
            queryset = queryset.filter(name__icontains=search)

        min_price = querydict.get("minPrice", "").strip()
        if min_price:
            queryset = queryset.filter(effective_price__gte=self._parse_non_negative_int(min_price, "minPrice"))

        max_price = querydict.get("maxPrice", "").strip()
        if max_price:
            queryset = queryset.filter(effective_price__lte=self._parse_non_negative_int(max_price, "maxPrice"))

        specs = querydict.get("specs", "").strip()
        if specs:
            queryset = self._apply_spec_filters(queryset, specs)

        sort = self._parse_sort(querydict.get("sort", "newest"))
        queryset = self._apply_sort(queryset, sort)

        total_count = queryset.count()
        start = (page - 1) * limit
        items = queryset[start : start + limit]
        return [self._serialize_product_summary(item) for item in items], build_pagination_meta(
            page=page,
            limit=limit,
            total_count=total_count,
        )

    def get_product(self, product_id: int) -> dict:
        product = self.product_repository.find_detail_by_id(product_id)
        if product is None:
            raise ApiError("PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다.", 404)

        price_entries = sorted(
            [entry for entry in product.price_entries.all() if entry.is_available],
            key=lambda item: (item.price, item.id),
        )
        price_values = [entry.price for entry in price_entries]

        lowest_price = min(price_values) if price_values else self._display_price(product)
        highest_price = max(price_values) if price_values else None
        average_price = int(sum(price_values) / len(price_values)) if price_values else None

        return {
            "id": product.id,
            "name": product.name,
            "description": product.description,
            "lowestPrice": lowest_price,
            "highestPrice": highest_price,
            "averagePrice": average_price,
            "stock": product.stock,
            "status": product.status,
            "category": {
                "id": product.category.id,
                "name": product.category.name,
            },
            "options": [self._serialize_option(option) for option in product.options.all()],
            "images": [self._serialize_image(image) for image in product.images.all()],
            "specs": SpecService.serialize_product_specs(product.product_specs.all()),
            "priceEntries": [self._serialize_price_entry(entry) for entry in price_entries],
            "reviewCount": product.review_count,
            "averageRating": float(product.average_rating),
            "createdAt": product.created_at.isoformat().replace("+00:00", "Z"),
        }

    def create_product(self, data: dict) -> dict:
        require_fields(data, ["name", "description", "price", "stock", "categoryId"])
        normalized = self._normalize_product_payload(data, partial=False)
        options = self._normalize_options(data.get("options", []))
        images = self._normalize_image_payloads(data.get("images", []))

        with transaction.atomic():
            thumbnail_url = self._resolve_thumbnail_url(normalized.get("thumbnail_url"), images)
            product = self.product_repository.create(
                name=normalized["name"],
                description=normalized["description"],
                price=normalized["price"],
                discount_price=normalized["discount_price"],
                stock=normalized["stock"],
                status=normalized["status"],
                category=normalized["category"],
                thumbnail_url=thumbnail_url,
            )

            for option in options:
                self.product_repository.create_option(
                    product=product,
                    name=option["name"],
                    values=option["values"],
                )

            for image in images:
                self.product_repository.create_image(
                    product=product,
                    url=image["url"],
                    is_main=image["isMain"],
                    sort_order=image["sortOrder"],
                )

        return self.get_product(product.id)

    def update_product(self, product_id: int, data: dict) -> dict:
        product = self.product_repository.find_by_id(product_id)
        if product is None:
            raise ApiError("PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다.", 404)

        normalized = self._normalize_product_payload(data, partial=True)
        updated_fields: list[str] = []
        for field_name, value in normalized.items():
            setattr(product, field_name, value)
            updated_fields.append(field_name)

        if updated_fields:
            updated_fields.append("updated_at")
            self.product_repository.save(product, update_fields=updated_fields)

        return self.get_product(product.id)

    def delete_product(self, product_id: int) -> dict:
        product = self.product_repository.find_by_id(product_id)
        if product is None:
            raise ApiError("PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다.", 404)
        product.deleted_at = timezone.now()
        self.product_repository.save(product, update_fields=["deleted_at", "updated_at"])
        return {"message": "상품이 삭제되었습니다."}

    def add_option(self, product_id: int, data: dict) -> dict:
        product = self._ensure_product(product_id)
        option_data = self._normalize_option(data)
        option = self.product_repository.create_option(
            product=product,
            name=option_data["name"],
            values=option_data["values"],
        )
        return self._serialize_option(option)

    def update_option(self, product_id: int, option_id: int, data: dict) -> dict:
        self._ensure_product(product_id)
        option = self.product_repository.find_option(product_id, option_id)
        if option is None:
            raise ApiError("PRODUCT_OPTION_NOT_FOUND", "상품 옵션을 찾을 수 없습니다.", 404)

        option_data = self._normalize_option(data)
        option.name = option_data["name"]
        option.values = option_data["values"]
        self.product_repository.save_option(option, update_fields=["name", "values", "updated_at"])
        return self._serialize_option(option)

    def delete_option(self, product_id: int, option_id: int) -> dict:
        self._ensure_product(product_id)
        option = self.product_repository.find_option(product_id, option_id)
        if option is None:
            raise ApiError("PRODUCT_OPTION_NOT_FOUND", "상품 옵션을 찾을 수 없습니다.", 404)
        self.product_repository.delete_option(option)
        return {"message": "옵션이 삭제되었습니다."}

    def upload_image(self, product_id: int, file: UploadedFile | None, form_data) -> dict:
        product = self._ensure_product(product_id)
        if file is None:
            raise ApiError("VALIDATION_ERROR", "업로드할 이미지 파일이 필요합니다.", 400)

        self._validate_image_file(file)
        requested_main = self._parse_bool(form_data.get("isMain"), default=False)
        sort_order = self._parse_non_negative_int(form_data.get("sortOrder", 0), "sortOrder")

        upload_dir = Path(settings.MEDIA_ROOT) / getattr(settings, "PBSHOP_PRODUCT_IMAGE_DIR", "products")
        upload_dir.mkdir(parents=True, exist_ok=True)
        extension = Path(file.name).suffix.lower() or ".png"
        filename = f"{uuid.uuid4().hex}{extension}"
        file_path = upload_dir / filename
        with file_path.open("wb") as destination:
            for chunk in file.chunks():
                destination.write(chunk)

        image_url = (
            f"{settings.MEDIA_URL.rstrip('/')}/{getattr(settings, 'PBSHOP_PRODUCT_IMAGE_DIR', 'products')}/{filename}"
        )
        existing_images = list(self.product_repository.list_images(product.id))
        is_main = requested_main or not existing_images
        if is_main:
            self.product_repository.unset_main_images(product.id)

        image = self.product_repository.create_image(
            product=product,
            url=image_url,
            is_main=is_main,
            sort_order=sort_order,
        )
        if is_main or not product.thumbnail_url:
            product.thumbnail_url = image.url
            self.product_repository.save(product, update_fields=["thumbnail_url", "updated_at"])

        return self._serialize_image(image)

    def delete_image(self, product_id: int, image_id: int) -> dict:
        product = self._ensure_product(product_id)
        image = self.product_repository.find_image(product_id, image_id)
        if image is None:
            raise ApiError("PRODUCT_IMAGE_NOT_FOUND", "상품 이미지를 찾을 수 없습니다.", 404)

        deleted_was_main = image.is_main or product.thumbnail_url == image.url
        image_url = image.url
        self.product_repository.delete_image(image)
        self._delete_local_image(image_url)

        remaining_images = list(self.product_repository.list_images(product.id))
        if not remaining_images:
            if product.thumbnail_url is not None:
                product.thumbnail_url = None
                self.product_repository.save(product, update_fields=["thumbnail_url", "updated_at"])
            return {"message": "이미지가 삭제되었습니다."}

        main_image = next((item for item in remaining_images if item.is_main), None)
        if main_image is None:
            main_image = remaining_images[0]
            main_image.is_main = True
            self.product_repository.save_image(main_image, update_fields=["is_main"])

        if deleted_was_main:
            product.thumbnail_url = main_image.url
            self.product_repository.save(product, update_fields=["thumbnail_url", "updated_at"])

        return {"message": "이미지가 삭제되었습니다."}

    def _apply_spec_filters(self, queryset, raw_specs: str):
        spec_filters = SpecService.parse_filter_payload(raw_specs)
        for spec_filter in spec_filters:
            filter_kwargs = {
                "deleted_at__isnull": True,
                "product_specs__spec_definition__name": spec_filter["name"],
            }
            if spec_filter["numericValue"] is not None:
                filter_kwargs["product_specs__numeric_value"] = spec_filter["numericValue"]
            else:
                filter_kwargs["product_specs__value"] = spec_filter["value"]
            queryset = queryset.filter(
                id__in=Product.objects.filter(**filter_kwargs).values("id")
            )
        return queryset

    def _apply_sort(self, queryset, sort: str):
        if sort == "price_asc":
            return queryset.order_by("effective_price", "id")
        if sort == "price_desc":
            return queryset.order_by("-effective_price", "id")
        if sort == "popularity":
            return queryset.order_by("-popularity_score", "id")
        if sort == "rating_desc":
            return queryset.order_by("-average_rating", "-review_count", "id")
        if sort == "rating_asc":
            return queryset.order_by("average_rating", "id")
        return queryset.order_by("-created_at", "id")

    def _normalize_product_payload(self, data: dict, *, partial: bool) -> dict:
        normalized: dict = {}
        if not partial or "name" in data:
            normalized["name"] = self._validate_name(data.get("name"), field_name="상품명", max_length=200)
        if not partial or "description" in data:
            normalized["description"] = self._validate_required_text(data.get("description"), field_name="상품 설명")
        if not partial or "price" in data:
            normalized["price"] = self._parse_non_negative_int(data.get("price"), "price")
        if "discountPrice" in data:
            normalized["discount_price"] = self._parse_optional_non_negative_int(data.get("discountPrice"), "discountPrice")
        elif not partial:
            normalized["discount_price"] = None
        if not partial or "stock" in data:
            normalized["stock"] = self._parse_non_negative_int(data.get("stock"), "stock")
        if "status" in data:
            normalized["status"] = self._parse_status(data.get("status"))
        elif not partial:
            normalized["status"] = ProductStatus.ON_SALE
        if not partial or "categoryId" in data:
            normalized["category"] = self._resolve_category(data.get("categoryId"))
        if "thumbnailUrl" in data:
            normalized["thumbnail_url"] = self._parse_optional_string(data.get("thumbnailUrl"), "thumbnailUrl", 500)
        return normalized

    def _normalize_options(self, value) -> list[dict]:
        if value in (None, ""):
            return []
        if not isinstance(value, list):
            raise ApiError("VALIDATION_ERROR", "options는 배열이어야 합니다.", 400)
        return [self._normalize_option(item) for item in value]

    def _normalize_option(self, value) -> dict:
        if not isinstance(value, dict):
            raise ApiError("VALIDATION_ERROR", "옵션 형식이 올바르지 않습니다.", 400)
        require_fields(value, ["name", "values"])
        name = self._validate_name(value.get("name"), field_name="옵션명", max_length=50)
        values = value.get("values")
        if not isinstance(values, list) or not values:
            raise ApiError("VALIDATION_ERROR", "옵션 값은 비어 있지 않은 배열이어야 합니다.", 400)

        normalized_values: list[str] = []
        for item in values:
            normalized_item = str(item or "").strip()
            if not normalized_item:
                raise ApiError("VALIDATION_ERROR", "옵션 값은 비워둘 수 없습니다.", 400)
            normalized_values.append(normalized_item)
        return {
            "name": name,
            "values": normalized_values,
        }

    def _normalize_image_payloads(self, value) -> list[dict]:
        if value in (None, ""):
            return []
        if not isinstance(value, list):
            raise ApiError("VALIDATION_ERROR", "images는 배열이어야 합니다.", 400)

        normalized_images: list[dict] = []
        explicit_main_indexes: list[int] = []
        for index, item in enumerate(value):
            if not isinstance(item, dict):
                raise ApiError("VALIDATION_ERROR", "이미지 형식이 올바르지 않습니다.", 400)
            url = self._validate_required_text(item.get("url"), field_name="이미지 URL", max_length=500)
            is_main = self._parse_bool(item.get("isMain"), default=False)
            sort_order = self._parse_non_negative_int(item.get("sortOrder", 0), "sortOrder")
            normalized_images.append(
                {
                    "url": url,
                    "isMain": is_main,
                    "sortOrder": sort_order,
                }
            )
            if is_main:
                explicit_main_indexes.append(index)

        if normalized_images:
            main_index = explicit_main_indexes[0] if explicit_main_indexes else 0
            for index, item in enumerate(normalized_images):
                item["isMain"] = index == main_index
        return normalized_images

    def _resolve_thumbnail_url(self, thumbnail_url: str | None, images: list[dict]) -> str | None:
        if thumbnail_url:
            return thumbnail_url
        main_image = next((item for item in images if item["isMain"]), None)
        if main_image:
            return main_image["url"]
        return images[0]["url"] if images else None

    def _resolve_category(self, raw_category_id) -> Category:
        category_id = self._parse_positive_int(raw_category_id, "categoryId")
        category = self.category_repository.find_by_id(category_id)
        if category is None:
            raise ApiError("CATEGORY_NOT_FOUND", "카테고리를 찾을 수 없습니다.", 404)
        return category

    def _ensure_product(self, product_id: int) -> Product:
        product = self.product_repository.find_by_id(product_id)
        if product is None:
            raise ApiError("PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다.", 404)
        return product

    def _validate_name(self, value, *, field_name: str, max_length: int) -> str:
        normalized = str(value or "").strip()
        if not normalized:
            raise ApiError("VALIDATION_ERROR", f"{field_name}은(는) 비워둘 수 없습니다.", 400)
        if len(normalized) > max_length:
            raise ApiError("VALIDATION_ERROR", f"{field_name}은(는) {max_length}자 이하여야 합니다.", 400)
        return normalized

    def _validate_required_text(self, value, *, field_name: str, max_length: int | None = None) -> str:
        normalized = str(value or "").strip()
        if not normalized:
            raise ApiError("VALIDATION_ERROR", f"{field_name}은(는) 비워둘 수 없습니다.", 400)
        if max_length is not None and len(normalized) > max_length:
            raise ApiError("VALIDATION_ERROR", f"{field_name}은(는) {max_length}자 이하여야 합니다.", 400)
        return normalized

    def _parse_non_negative_int(self, value, field_name: str) -> int:
        try:
            parsed = int(value)
        except (TypeError, ValueError) as exc:
            raise ApiError("VALIDATION_ERROR", f"{field_name}는 정수여야 합니다.", 400) from exc
        if parsed < 0:
            raise ApiError("VALIDATION_ERROR", f"{field_name}는 0 이상이어야 합니다.", 400)
        return parsed

    def _parse_positive_int(self, value, field_name: str) -> int:
        parsed = self._parse_non_negative_int(value, field_name)
        if parsed < 1:
            raise ApiError("VALIDATION_ERROR", f"{field_name}는 1 이상이어야 합니다.", 400)
        return parsed

    def _parse_optional_non_negative_int(self, value, field_name: str) -> int | None:
        if value in (None, ""):
            return None
        return self._parse_non_negative_int(value, field_name)

    def _parse_optional_string(self, value, field_name: str, max_length: int) -> str | None:
        if value in (None, ""):
            return None
        normalized = str(value).strip()
        if len(normalized) > max_length:
            raise ApiError("VALIDATION_ERROR", f"{field_name}은(는) {max_length}자 이하여야 합니다.", 400)
        return normalized or None

    def _parse_status(self, value) -> str:
        normalized = str(value or "").strip()
        if normalized not in ProductStatus.values:
            raise ApiError("VALIDATION_ERROR", "유효하지 않은 상품 상태입니다.", 400)
        return normalized

    def _parse_sort(self, value) -> str:
        normalized = str(value or "newest").strip().lower()
        if normalized not in self.SORT_VALUES:
            raise ApiError("VALIDATION_ERROR", "유효하지 않은 정렬 조건입니다.", 400)
        return normalized

    def _parse_bool(self, value, *, default: bool) -> bool:
        if value in (None, ""):
            return default
        if isinstance(value, bool):
            return value
        normalized = str(value).strip().lower()
        if normalized in {"1", "true", "yes", "on"}:
            return True
        if normalized in {"0", "false", "no", "off"}:
            return False
        raise ApiError("VALIDATION_ERROR", "불리언 값 형식이 올바르지 않습니다.", 400)

    def _display_price(self, product: Product) -> int:
        return product.lowest_price if product.lowest_price is not None else product.price

    def _serialize_product_summary(self, product: Product) -> dict:
        display_price = self._display_price(product)
        price_diff = None
        price_diff_percent = None
        if product.discount_price is not None:
            price_diff = display_price - product.discount_price
            if product.discount_price > 0:
                ratio = (Decimal(price_diff) / Decimal(product.discount_price)) * Decimal("100")
                price_diff_percent = float(ratio.quantize(Decimal("0.01"), rounding=ROUND_HALF_UP))
            else:
                price_diff_percent = 0.0

        return {
            "id": product.id,
            "name": product.name,
            "lowestPrice": display_price,
            "sellerCount": product.seller_count,
            "thumbnailUrl": product.thumbnail_url,
            "reviewCount": product.review_count,
            "averageRating": float(product.average_rating),
            "priceDiff": price_diff,
            "priceDiffPercent": price_diff_percent,
            "createdAt": product.created_at.isoformat().replace("+00:00", "Z"),
        }

    def _serialize_option(self, option: ProductOption) -> dict:
        return {
            "id": option.id,
            "name": option.name,
            "values": option.values,
        }

    def _serialize_image(self, image: ProductImage) -> dict:
        return {
            "id": image.id,
            "url": image.url,
            "isMain": image.is_main,
            "sortOrder": image.sort_order,
        }

    def _serialize_price_entry(self, entry) -> dict:
        return {
            "seller": {
                "id": entry.seller.id,
                "name": entry.seller.name,
                "logoUrl": entry.seller.logo_url,
                "trustScore": entry.seller.trust_score,
            },
            "price": entry.price,
            "url": entry.product_url,
            "shipping": self._serialize_shipping(entry),
        }

    def _serialize_shipping(self, entry) -> str:
        if entry.shipping_info:
            return entry.shipping_info
        if entry.shipping_fee == 0:
            return "무료배송"
        return f"배송비 {entry.shipping_fee}원"

    def _validate_image_file(self, file: UploadedFile) -> None:
        try:
            with Image.open(file) as image:
                image.verify()
        except (UnidentifiedImageError, OSError) as exc:
            raise ApiError("VALIDATION_ERROR", "유효한 이미지 파일만 업로드할 수 있습니다.", 400) from exc
        finally:
            file.seek(0)

    def _delete_local_image(self, image_url: str | None) -> None:
        if not image_url:
            return
        prefix = settings.MEDIA_URL.rstrip("/") + "/"
        if not image_url.startswith(prefix):
            return
        relative_path = image_url[len(prefix) :]
        target = Path(settings.MEDIA_ROOT) / relative_path
        if target.exists():
            os.remove(target)
