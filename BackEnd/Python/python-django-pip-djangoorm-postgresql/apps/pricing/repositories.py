from __future__ import annotations

from django.db.models import Avg, Count, Min, Max, QuerySet

from apps.pricing.models import PriceEntry, Seller


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
