from __future__ import annotations

from datetime import date

from django.db.models import Avg, Count, Max, Min, QuerySet

from apps.pricing.models import PriceAlert, PriceEntry, PriceHistory, Seller


class SellerRepository:
    def list_active(self) -> QuerySet[Seller]:
        return Seller.objects.filter(is_active=True).order_by("-trust_score", "name", "id")

    def find_by_id(self, seller_id: int) -> Seller | None:
        return Seller.objects.filter(id=seller_id).first()

    def find_active_by_id(self, seller_id: int) -> Seller | None:
        return Seller.objects.filter(id=seller_id, is_active=True).first()

    def create(self, **kwargs) -> Seller:
        return Seller.objects.create(**kwargs)

    def save(self, seller: Seller, *, update_fields: list[str] | None = None) -> Seller:
        seller.save(update_fields=update_fields)
        return seller


class PriceEntryRepository:
    def list_active_by_product(self, product_id: int) -> QuerySet[PriceEntry]:
        return (
            PriceEntry.objects.select_related("seller", "product")
            .filter(
                product_id=product_id,
                product__deleted_at__isnull=True,
                is_available=True,
                seller__is_active=True,
            )
            .order_by("price", "id")
        )

    def list_by_seller(self, seller_id: int) -> QuerySet[PriceEntry]:
        return PriceEntry.objects.filter(seller_id=seller_id)

    def find_by_id(self, price_id: int) -> PriceEntry | None:
        return PriceEntry.objects.select_related("seller", "product").filter(id=price_id).first()

    def find_by_product_and_seller(self, product_id: int, seller_id: int) -> PriceEntry | None:
        return (
            PriceEntry.objects.select_related("seller", "product")
            .filter(product_id=product_id, seller_id=seller_id)
            .first()
        )

    def create(self, **kwargs) -> PriceEntry:
        return PriceEntry.objects.create(**kwargs)

    def save(self, entry: PriceEntry, *, update_fields: list[str] | None = None) -> PriceEntry:
        entry.save(update_fields=update_fields)
        return entry

    def delete(self, entry: PriceEntry) -> None:
        entry.delete()

    def summarize_product(self, product_id: int) -> dict:
        return (
            PriceEntry.objects.filter(
                product_id=product_id,
                is_available=True,
                seller__is_active=True,
            )
            .aggregate(
                lowest_price=Min("price"),
                highest_price=Max("price"),
                average_price=Avg("price"),
                seller_count=Count("id"),
            )
        )


class PriceHistoryRepository:
    def list_by_product_since(self, product_id: int, since_date: date) -> QuerySet[PriceHistory]:
        return (
            PriceHistory.objects.filter(
                product_id=product_id,
                product__deleted_at__isnull=True,
                date__gte=since_date,
            )
            .order_by("date", "id")
        )

    def summarize_all_time(self, product_id: int) -> dict:
        return (
            PriceHistory.objects.filter(
                product_id=product_id,
                product__deleted_at__isnull=True,
            )
            .aggregate(
                all_time_lowest=Min("lowest_price"),
                all_time_highest=Max("highest_price"),
            )
        )


class PriceAlertRepository:
    def list_active_by_user(self, user_id: int) -> QuerySet[PriceAlert]:
        return (
            PriceAlert.objects.select_related("product")
            .filter(
                user_id=user_id,
                is_active=True,
                product__deleted_at__isnull=True,
            )
            .order_by("-created_at", "-id")
        )

    def find_by_user_and_product(self, user_id: int, product_id: int) -> PriceAlert | None:
        return (
            PriceAlert.objects.select_related("product")
            .filter(
                user_id=user_id,
                product_id=product_id,
                product__deleted_at__isnull=True,
            )
            .first()
        )

    def find_active_by_id_for_user(self, alert_id: int, user_id: int) -> PriceAlert | None:
        return (
            PriceAlert.objects.select_related("product")
            .filter(
                id=alert_id,
                user_id=user_id,
                is_active=True,
                product__deleted_at__isnull=True,
            )
            .first()
        )

    def create(self, **kwargs) -> PriceAlert:
        return PriceAlert.objects.create(**kwargs)

    def save(self, alert: PriceAlert, *, update_fields: list[str] | None = None) -> PriceAlert:
        alert.save(update_fields=update_fields)
        return alert

    def trigger_eligible_for_product(self, product_id: int, current_lowest_price: int, *, triggered_at) -> int:
        return (
            PriceAlert.objects.filter(
                product_id=product_id,
                product__deleted_at__isnull=True,
                is_active=True,
                is_triggered=False,
                target_price__gte=current_lowest_price,
            )
            .update(
                is_triggered=True,
                triggered_at=triggered_at,
            )
        )
