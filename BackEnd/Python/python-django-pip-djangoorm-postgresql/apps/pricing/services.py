from __future__ import annotations

from collections import OrderedDict
from datetime import timedelta
from urllib.parse import urlparse

from django.db import transaction
from django.utils import timezone

from apps.api.errors import ApiError
from apps.api.pagination import build_pagination_meta, parse_pagination
from apps.api.utils import require_fields
from apps.catalog.repositories import ProductRepository
from apps.pricing.models import PriceAlert, PriceEntry, Seller, ShippingType
from apps.pricing.repositories import (
    PriceAlertRepository,
    PriceEntryRepository,
    PriceHistoryRepository,
    SellerRepository,
)


TRUST_GRADE_VALUES = {"A+", "A", "B+", "B", "C+", "C", "D", "F"}
HISTORY_PERIOD_DAYS = {
    "1w": 7,
    "1m": 30,
    "3m": 90,
    "6m": 180,
    "1y": 365,
}
HISTORY_TYPES = {"daily", "weekly", "monthly"}


def refresh_product_price_state(
    *,
    product_id: int,
    product_repository: ProductRepository,
    price_entry_repository: PriceEntryRepository,
    price_alert_repository: PriceAlertRepository,
) -> None:
    product = product_repository.find_by_id(product_id)
    if product is None:
        return

    summary = price_entry_repository.summarize_product(product_id)
    product.lowest_price = summary["lowest_price"]
    product.seller_count = summary["seller_count"] or 0
    product_repository.save(product, update_fields=["lowest_price", "seller_count", "updated_at"])

    current_lowest_price = product.lowest_price if product.lowest_price is not None else product.price
    price_alert_repository.trigger_eligible_for_product(
        product_id,
        current_lowest_price,
        triggered_at=timezone.now(),
    )


class SellerService:
    def __init__(
        self,
        seller_repository: SellerRepository | None = None,
        price_entry_repository: PriceEntryRepository | None = None,
        product_repository: ProductRepository | None = None,
        price_alert_repository: PriceAlertRepository | None = None,
    ) -> None:
        self.seller_repository = seller_repository or SellerRepository()
        self.price_entry_repository = price_entry_repository or PriceEntryRepository()
        self.product_repository = product_repository or ProductRepository()
        self.price_alert_repository = price_alert_repository or PriceAlertRepository()

    def list_sellers(self, querydict) -> tuple[list[dict], dict]:
        page, limit = parse_pagination(querydict)
        queryset = self.seller_repository.list_active()
        total_count = queryset.count()
        start = (page - 1) * limit
        items = list(queryset[start : start + limit])
        return [self._serialize_seller(item) for item in items], build_pagination_meta(
            page=page,
            limit=limit,
            total_count=total_count,
        )

    def get_seller(self, seller_id: int) -> dict:
        seller = self.seller_repository.find_active_by_id(seller_id)
        if seller is None:
            raise ApiError("SELLER_NOT_FOUND", "판매처를 찾을 수 없습니다.", 404)
        return self._serialize_seller(seller)

    def create_seller(self, data: dict) -> dict:
        require_fields(data, ["name", "url"])
        seller = self.seller_repository.create(**self._normalize_seller_payload(data, partial=False))
        return self._serialize_seller(seller)

    def update_seller(self, seller_id: int, data: dict) -> dict:
        seller = self._ensure_seller(seller_id)
        normalized = self._normalize_seller_payload(data, partial=True)

        if not normalized:
            return self._serialize_seller(seller)

        is_active_changed = "is_active" in normalized and normalized["is_active"] != seller.is_active
        update_fields: list[str] = []
        for field_name, value in normalized.items():
            setattr(seller, field_name, value)
            update_fields.append(field_name)

        update_fields.append("updated_at")
        self.seller_repository.save(seller, update_fields=update_fields)
        if is_active_changed:
            self._refresh_products_for_seller(seller.id)
        return self._serialize_seller(seller)

    def delete_seller(self, seller_id: int) -> dict:
        seller = self._ensure_seller(seller_id)
        if seller.is_active:
            seller.is_active = False
            self.seller_repository.save(seller, update_fields=["is_active", "updated_at"])
            self._refresh_products_for_seller(seller.id)
        return {"message": "판매처가 비활성화되었습니다."}

    def _ensure_seller(self, seller_id: int) -> Seller:
        seller = self.seller_repository.find_by_id(seller_id)
        if seller is None:
            raise ApiError("SELLER_NOT_FOUND", "판매처를 찾을 수 없습니다.", 404)
        return seller

    def _normalize_seller_payload(self, data: dict, *, partial: bool) -> dict:
        if not isinstance(data, dict):
            raise ApiError("VALIDATION_ERROR", "요청 본문은 객체여야 합니다.", 400)

        normalized: dict = {}
        if not partial or "name" in data:
            normalized["name"] = self._validate_required_text(data.get("name"), field_name="name", max_length=100)
        if not partial or "url" in data:
            normalized["url"] = self._validate_url(data.get("url"), field_name="url", max_length=500)
        if "logoUrl" in data:
            normalized["logo_url"] = self._parse_optional_string(data.get("logoUrl"), "logoUrl", 500)
        if "description" in data:
            normalized["description"] = self._parse_optional_string(data.get("description"), "description", 200)
        if "trustScore" in data:
            normalized["trust_score"] = self._parse_score(data.get("trustScore"))
        if "trustGrade" in data:
            normalized["trust_grade"] = self._parse_trust_grade(data.get("trustGrade"))
        if "isActive" in data:
            normalized["is_active"] = self._parse_bool(data.get("isActive"))
        return normalized

    def _serialize_seller(self, seller: Seller) -> dict:
        return {
            "id": seller.id,
            "name": seller.name,
            "url": seller.url,
            "logoUrl": seller.logo_url,
            "trustScore": seller.trust_score,
            "trustGrade": seller.trust_grade,
            "description": seller.description,
            "isActive": seller.is_active,
            "createdAt": seller.created_at.isoformat().replace("+00:00", "Z"),
        }

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

    def _validate_url(self, value, *, field_name: str, max_length: int) -> str:
        normalized = self._validate_required_text(value, field_name=field_name, max_length=max_length)
        parsed = urlparse(normalized)
        if parsed.scheme not in {"http", "https"} or not parsed.netloc:
            raise ApiError("VALIDATION_ERROR", f"{field_name}은(는) 올바른 URL이어야 합니다.", 400)
        return normalized

    def _parse_score(self, value) -> int:
        try:
            parsed = int(value)
        except (TypeError, ValueError) as exc:
            raise ApiError("VALIDATION_ERROR", "trustScore는 정수여야 합니다.", 400) from exc
        if parsed < 0 or parsed > 100:
            raise ApiError("VALIDATION_ERROR", "trustScore는 0 이상 100 이하여야 합니다.", 400)
        return parsed

    def _parse_trust_grade(self, value) -> str | None:
        if value in (None, ""):
            return None
        normalized = str(value).strip().upper()
        if normalized not in TRUST_GRADE_VALUES:
            raise ApiError("VALIDATION_ERROR", "유효하지 않은 trustGrade입니다.", 400)
        return normalized

    def _parse_bool(self, value) -> bool:
        if isinstance(value, bool):
            return value
        normalized = str(value or "").strip().lower()
        if normalized in {"1", "true", "yes", "on"}:
            return True
        if normalized in {"0", "false", "no", "off"}:
            return False
        raise ApiError("VALIDATION_ERROR", "isActive는 불리언이어야 합니다.", 400)

    def _refresh_products_for_seller(self, seller_id: int) -> None:
        product_ids = list(self.price_entry_repository.list_by_seller(seller_id).values_list("product_id", flat=True).distinct())
        for product_id in product_ids:
            refresh_product_price_state(
                product_id=product_id,
                product_repository=self.product_repository,
                price_entry_repository=self.price_entry_repository,
                price_alert_repository=self.price_alert_repository,
            )


class PriceEntryService:
    def __init__(
        self,
        price_entry_repository: PriceEntryRepository | None = None,
        seller_repository: SellerRepository | None = None,
        product_repository: ProductRepository | None = None,
        price_alert_repository: PriceAlertRepository | None = None,
    ) -> None:
        self.price_entry_repository = price_entry_repository or PriceEntryRepository()
        self.seller_repository = seller_repository or SellerRepository()
        self.product_repository = product_repository or ProductRepository()
        self.price_alert_repository = price_alert_repository or PriceAlertRepository()

    def get_product_prices(self, product_id: int) -> dict:
        self._ensure_product(product_id)
        entries = list(self.price_entry_repository.list_active_by_product(product_id))
        prices = [entry.price for entry in entries]
        lowest_price = min(prices) if prices else None
        highest_price = max(prices) if prices else None
        average_price = int(sum(prices) / len(prices)) if prices else None

        return {
            "lowestPrice": lowest_price,
            "averagePrice": average_price,
            "highestPrice": highest_price,
            "entries": [self._serialize_price_entry(entry) for entry in entries],
        }

    def create_price_entry(self, product_id: int, data: dict) -> dict:
        self._ensure_product(product_id)
        require_fields(data, ["sellerId", "price", "productUrl"])
        normalized = self._normalize_create_payload(data)

        existing = self.price_entry_repository.find_by_product_and_seller(product_id, normalized["seller"].id)
        if existing is not None:
            raise ApiError("CONFLICT", "이미 등록된 판매처 가격이 있습니다.", 409)

        with transaction.atomic():
            entry = self.price_entry_repository.create(
                product_id=product_id,
                seller=normalized["seller"],
                price=normalized["price"],
                shipping_cost=normalized["shipping_cost"],
                shipping_info=normalized["shipping_info"],
                product_url=normalized["product_url"],
                shipping_fee=normalized["shipping_cost"],
                shipping_type=normalized["shipping_type"],
            )
            refresh_product_price_state(
                product_id=product_id,
                product_repository=self.product_repository,
                price_entry_repository=self.price_entry_repository,
                price_alert_repository=self.price_alert_repository,
            )
        return self._serialize_price_entry(entry)

    def update_price_entry(self, price_id: int, data: dict) -> dict:
        entry = self._ensure_price_entry(price_id)
        normalized = self._normalize_update_payload(data)
        if not normalized:
            return self._serialize_price_entry(entry)

        update_fields: list[str] = []
        for field_name, value in normalized.items():
            setattr(entry, field_name, value)
            update_fields.append(field_name)

        if "shipping_cost" in normalized:
            entry.shipping_fee = normalized["shipping_cost"]
            update_fields.append("shipping_fee")

        update_fields.append("updated_at")
        with transaction.atomic():
            self.price_entry_repository.save(entry, update_fields=update_fields)
            refresh_product_price_state(
                product_id=entry.product_id,
                product_repository=self.product_repository,
                price_entry_repository=self.price_entry_repository,
                price_alert_repository=self.price_alert_repository,
            )
        return self._serialize_price_entry(entry)

    def delete_price_entry(self, price_id: int) -> dict:
        entry = self._ensure_price_entry(price_id)
        product_id = entry.product_id
        with transaction.atomic():
            self.price_entry_repository.delete(entry)
            refresh_product_price_state(
                product_id=product_id,
                product_repository=self.product_repository,
                price_entry_repository=self.price_entry_repository,
                price_alert_repository=self.price_alert_repository,
            )
        return {"message": "가격이 삭제되었습니다."}

    def _normalize_create_payload(self, data: dict) -> dict:
        seller = self._resolve_active_seller(data.get("sellerId"))
        return {
            "seller": seller,
            "price": self._parse_non_negative_int(data.get("price"), "price"),
            "shipping_cost": self._parse_non_negative_int(data.get("shippingCost", 0), "shippingCost"),
            "shipping_info": self._parse_optional_string(data.get("shippingInfo"), "shippingInfo", 100),
            "product_url": self._validate_url(data.get("productUrl"), field_name="productUrl", max_length=1000),
            "shipping_type": self._parse_shipping_type(data.get("shippingType", ShippingType.PAID)),
        }

    def _normalize_update_payload(self, data: dict) -> dict:
        if not isinstance(data, dict):
            raise ApiError("VALIDATION_ERROR", "요청 본문은 객체여야 합니다.", 400)

        normalized: dict = {}
        if "price" in data:
            normalized["price"] = self._parse_non_negative_int(data.get("price"), "price")
        if "shippingCost" in data:
            normalized["shipping_cost"] = self._parse_non_negative_int(data.get("shippingCost"), "shippingCost")
        if "shippingInfo" in data:
            normalized["shipping_info"] = self._parse_optional_string(data.get("shippingInfo"), "shippingInfo", 100)
        if "productUrl" in data:
            normalized["product_url"] = self._validate_url(data.get("productUrl"), field_name="productUrl", max_length=1000)
        if "shippingType" in data:
            normalized["shipping_type"] = self._parse_shipping_type(data.get("shippingType"))
        if "isAvailable" in data:
            normalized["is_available"] = self._parse_bool(data.get("isAvailable"), "isAvailable")
        return normalized

    def _serialize_price_entry(self, entry: PriceEntry) -> dict:
        return {
            "id": entry.id,
            "seller": self._serialize_seller_summary(entry.seller),
            "price": entry.price,
            "shippingCost": entry.shipping_cost,
            "shippingInfo": self._build_shipping_info(entry),
            "productUrl": entry.product_url,
            "updatedAt": entry.updated_at.isoformat().replace("+00:00", "Z"),
        }

    def _serialize_seller_summary(self, seller: Seller) -> dict:
        return {
            "id": seller.id,
            "name": seller.name,
            "logoUrl": seller.logo_url,
            "trustScore": seller.trust_score,
        }

    def _build_shipping_info(self, entry: PriceEntry) -> str:
        if entry.shipping_info:
            return entry.shipping_info
        if entry.shipping_cost == 0:
            return "무료배송"
        return f"배송비 {entry.shipping_cost}원"

    def _ensure_product(self, product_id: int):
        product = self.product_repository.find_by_id(product_id)
        if product is None:
            raise ApiError("PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다.", 404)
        return product

    def _ensure_price_entry(self, price_id: int) -> PriceEntry:
        entry = self.price_entry_repository.find_by_id(price_id)
        if entry is None:
            raise ApiError("PRICE_ENTRY_NOT_FOUND", "가격 정보를 찾을 수 없습니다.", 404)
        return entry

    def _resolve_active_seller(self, raw_seller_id) -> Seller:
        seller_id = self._parse_positive_int(raw_seller_id, "sellerId")
        seller = self.seller_repository.find_active_by_id(seller_id)
        if seller is None:
            raise ApiError("SELLER_NOT_FOUND", "활성 판매처를 찾을 수 없습니다.", 404)
        return seller

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

    def _parse_optional_string(self, value, field_name: str, max_length: int) -> str | None:
        if value in (None, ""):
            return None
        normalized = str(value).strip()
        if len(normalized) > max_length:
            raise ApiError("VALIDATION_ERROR", f"{field_name}은(는) {max_length}자 이하여야 합니다.", 400)
        return normalized or None

    def _validate_url(self, value, *, field_name: str, max_length: int) -> str:
        normalized = str(value or "").strip()
        if not normalized:
            raise ApiError("VALIDATION_ERROR", f"{field_name}은(는) 비워둘 수 없습니다.", 400)
        if len(normalized) > max_length:
            raise ApiError("VALIDATION_ERROR", f"{field_name}은(는) {max_length}자 이하여야 합니다.", 400)
        parsed = urlparse(normalized)
        if parsed.scheme not in {"http", "https"} or not parsed.netloc:
            raise ApiError("VALIDATION_ERROR", f"{field_name}은(는) 올바른 URL이어야 합니다.", 400)
        return normalized

    def _parse_shipping_type(self, value) -> str:
        normalized = str(value or "").strip().upper()
        if normalized not in ShippingType.values:
            raise ApiError("VALIDATION_ERROR", "유효하지 않은 shippingType입니다.", 400)
        return normalized

    def _parse_bool(self, value, field_name: str) -> bool:
        if isinstance(value, bool):
            return value
        normalized = str(value or "").strip().lower()
        if normalized in {"1", "true", "yes", "on"}:
            return True
        if normalized in {"0", "false", "no", "off"}:
            return False
        raise ApiError("VALIDATION_ERROR", f"{field_name}는 불리언이어야 합니다.", 400)


class PriceHistoryService:
    def __init__(
        self,
        history_repository: PriceHistoryRepository | None = None,
        product_repository: ProductRepository | None = None,
    ) -> None:
        self.history_repository = history_repository or PriceHistoryRepository()
        self.product_repository = product_repository or ProductRepository()

    def get_product_history(self, product_id: int, querydict) -> dict:
        product = self._ensure_product(product_id)
        period = self._parse_period(querydict.get("period"))
        history_type = self._parse_history_type(querydict.get("type"))
        since_date = timezone.localdate() - timedelta(days=HISTORY_PERIOD_DAYS[period])

        history_rows = list(self.history_repository.list_by_product_since(product_id, since_date))
        summary = self.history_repository.summarize_all_time(product_id)

        if history_type == "daily":
            history = [
                {
                    "date": row.date.isoformat(),
                    "lowestPrice": row.lowest_price,
                    "averagePrice": row.average_price,
                }
                for row in history_rows
            ]
        else:
            history = self._aggregate_history(history_rows, history_type)

        return {
            "productId": product.id,
            "productName": product.name,
            "allTimeLowest": summary["all_time_lowest"],
            "allTimeHighest": summary["all_time_highest"],
            "history": history,
        }

    def _ensure_product(self, product_id: int):
        product = self.product_repository.find_by_id(product_id)
        if product is None:
            raise ApiError("PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다.", 404)
        return product

    def _parse_period(self, value) -> str:
        normalized = str(value or "3m").strip().lower()
        if normalized not in HISTORY_PERIOD_DAYS:
            raise ApiError("VALIDATION_ERROR", "유효하지 않은 period입니다.", 400)
        return normalized

    def _parse_history_type(self, value) -> str:
        normalized = str(value or "daily").strip().lower()
        if normalized not in HISTORY_TYPES:
            raise ApiError("VALIDATION_ERROR", "유효하지 않은 type입니다.", 400)
        return normalized

    def _aggregate_history(self, history_rows, history_type: str) -> list[dict]:
        buckets: OrderedDict[str, dict] = OrderedDict()

        for row in history_rows:
            if history_type == "weekly":
                bucket_date = row.date - timedelta(days=row.date.weekday())
            else:
                bucket_date = row.date.replace(day=1)

            bucket_key = bucket_date.isoformat()
            bucket = buckets.setdefault(
                bucket_key,
                {
                    "date": bucket_key,
                    "lowestPrice": row.lowest_price,
                    "averagePriceTotal": 0,
                    "count": 0,
                },
            )
            bucket["lowestPrice"] = min(bucket["lowestPrice"], row.lowest_price)
            bucket["averagePriceTotal"] += row.average_price
            bucket["count"] += 1

        result: list[dict] = []
        for bucket in buckets.values():
            result.append(
                {
                    "date": bucket["date"],
                    "lowestPrice": bucket["lowestPrice"],
                    "averagePrice": round(bucket["averagePriceTotal"] / bucket["count"]),
                }
            )
        return result


class PriceAlertService:
    def __init__(
        self,
        alert_repository: PriceAlertRepository | None = None,
        product_repository: ProductRepository | None = None,
    ) -> None:
        self.alert_repository = alert_repository or PriceAlertRepository()
        self.product_repository = product_repository or ProductRepository()

    def list_alerts(self, user) -> list[dict]:
        return [self._serialize_alert(alert) for alert in self.alert_repository.list_active_by_user(user.id)]

    def create_alert(self, user, data: dict) -> dict:
        if not isinstance(data, dict):
            raise ApiError("VALIDATION_ERROR", "요청 본문은 객체여야 합니다.", 400)

        require_fields(data, ["productId", "targetPrice"])
        product = self._ensure_product(self._parse_positive_int(data.get("productId"), "productId"))
        target_price = self._parse_positive_int(data.get("targetPrice"), "targetPrice")
        existing = self.alert_repository.find_by_user_and_product(user.id, product.id)
        current_lowest_price = self._current_lowest_price(product)
        triggered = current_lowest_price <= target_price
        triggered_at = timezone.now() if triggered else None

        if existing is not None and existing.is_active:
            raise ApiError("ALERT_EXISTS", "해당 상품에 대한 알림이 이미 존재합니다.", 409)

        if existing is not None:
            existing.target_price = target_price
            existing.is_active = True
            existing.is_triggered = triggered
            existing.triggered_at = triggered_at
            self.alert_repository.save(
                existing,
                update_fields=["target_price", "is_active", "is_triggered", "triggered_at"],
            )
            return self._serialize_alert(existing)

        alert = self.alert_repository.create(
            user=user,
            product=product,
            target_price=target_price,
            is_triggered=triggered,
            triggered_at=triggered_at,
            is_active=True,
        )
        return self._serialize_alert(alert)

    def delete_alert(self, user, alert_id: int) -> dict:
        alert = self.alert_repository.find_active_by_id_for_user(alert_id, user.id)
        if alert is None:
            raise ApiError("PRICE_ALERT_NOT_FOUND", "알림을 찾을 수 없습니다.", 404)

        alert.is_active = False
        self.alert_repository.save(alert, update_fields=["is_active"])
        return {"message": "최저가 알림이 해제되었습니다."}

    def _serialize_alert(self, alert: PriceAlert) -> dict:
        return {
            "id": alert.id,
            "productId": alert.product_id,
            "productName": alert.product.name,
            "targetPrice": alert.target_price,
            "currentLowestPrice": self._current_lowest_price(alert.product),
            "isTriggered": alert.is_triggered,
            "createdAt": alert.created_at.isoformat().replace("+00:00", "Z"),
        }

    def _ensure_product(self, product_id: int):
        product = self.product_repository.find_by_id(product_id)
        if product is None:
            raise ApiError("PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다.", 404)
        return product

    def _parse_positive_int(self, value, field_name: str) -> int:
        try:
            parsed = int(value)
        except (TypeError, ValueError) as exc:
            raise ApiError("VALIDATION_ERROR", f"{field_name}는 정수여야 합니다.", 400) from exc
        if parsed < 1:
            raise ApiError("VALIDATION_ERROR", f"{field_name}는 1 이상이어야 합니다.", 400)
        return parsed

    def _current_lowest_price(self, product) -> int:
        return product.lowest_price if product.lowest_price is not None else product.price
