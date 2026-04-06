from __future__ import annotations

from django.db.models import Prefetch, QuerySet

from apps.catalog.models import Category, Product, ProductImage, ProductOption


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


class ProductRepository:
    def list_queryset(self) -> QuerySet[Product]:
        return Product.objects.select_related("category").filter(deleted_at__isnull=True)

    def find_by_id(self, product_id: int) -> Product | None:
        return Product.objects.select_related("category").filter(id=product_id, deleted_at__isnull=True).first()

    def find_detail_by_id(self, product_id: int) -> Product | None:
        option_queryset = ProductOption.objects.order_by("id")
        image_queryset = ProductImage.objects.order_by("sort_order", "id")
        return (
            Product.objects.select_related("category")
            .prefetch_related(
                Prefetch("options", queryset=option_queryset),
                Prefetch("images", queryset=image_queryset),
                "product_specs__spec_definition",
                "price_entries__seller",
            )
            .filter(id=product_id, deleted_at__isnull=True)
            .first()
        )

    def create(self, **kwargs) -> Product:
        return Product.objects.create(**kwargs)

    def save(self, product: Product, *, update_fields: list[str] | None = None) -> Product:
        product.save(update_fields=update_fields)
        return product

    def list_images(self, product_id: int) -> QuerySet[ProductImage]:
        return ProductImage.objects.filter(product_id=product_id).order_by("sort_order", "id")

    def list_options(self, product_id: int) -> QuerySet[ProductOption]:
        return ProductOption.objects.filter(product_id=product_id).order_by("id")

    def create_option(self, *, product: Product, name: str, values: list[str]) -> ProductOption:
        return ProductOption.objects.create(product=product, name=name, values=values)

    def find_option(self, product_id: int, option_id: int) -> ProductOption | None:
        return ProductOption.objects.filter(product_id=product_id, id=option_id).first()

    def save_option(self, option: ProductOption, *, update_fields: list[str] | None = None) -> ProductOption:
        option.save(update_fields=update_fields)
        return option

    def delete_option(self, option: ProductOption) -> None:
        option.delete()

    def unset_main_images(self, product_id: int) -> None:
        ProductImage.objects.filter(product_id=product_id, is_main=True).update(is_main=False)

    def create_image(self, *, product: Product, url: str, is_main: bool, sort_order: int) -> ProductImage:
        return ProductImage.objects.create(
            product=product,
            url=url,
            is_main=is_main,
            sort_order=sort_order,
        )

    def find_image(self, product_id: int, image_id: int) -> ProductImage | None:
        return ProductImage.objects.filter(product_id=product_id, id=image_id).first()

    def save_image(self, image: ProductImage, *, update_fields: list[str] | None = None) -> ProductImage:
        image.save(update_fields=update_fields)
        return image

    def delete_image(self, image: ProductImage) -> None:
        image.delete()
