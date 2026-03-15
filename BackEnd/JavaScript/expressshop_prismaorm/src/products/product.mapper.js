export function toProductSummaryDto(item) {
  return {
    id: item.id,
    name: item.name,
    description: item.description,
    price: item.price,
    discountPrice: item.discountPrice,
    status: item.status,
    stock: item.stock,
    thumbnailUrl: item.thumbnailUrl,
    lowestPrice: item.lowestPrice,
    sellerCount: item.sellerCount,
    reviewCount: item.reviewCount,
    averageRating: item.averageRating,
    popularityScore: item.popularityScore,
    category: item.category
      ? {
          id: item.category.id,
          name: item.category.name,
        }
      : null,
  };
}

export function toProductDetailDto(item) {
  return {
    ...toProductSummaryDto(item),
    category: item.category
      ? {
          id: item.category.id,
          name: item.category.name,
          parentId: item.category.parentId,
        }
      : null,
    options: (item.options ?? []).map((option) => ({
      id: option.id,
      name: option.name,
      values: option.values,
      createdAt: option.createdAt,
      updatedAt: option.updatedAt,
    })),
    images: (item.images ?? []).map((image) => ({
      id: image.id,
      url: image.url,
      isMain: image.isMain,
      sortOrder: image.sortOrder,
      createdAt: image.createdAt,
    })),
    specs: (item.specs ?? []).map((spec) => toProductSpecDto(spec)),
  };
}

export function toSpecDefinitionDto(item) {
  return {
    id: item.id,
    name: item.name,
    unit: item.unit,
    dataType: item.dataType,
    isComparable: item.isComparable,
    categoryId: item.categoryId,
    sortOrder: item.sortOrder,
  };
}

export function toProductSpecDto(item) {
  return {
    id: item.id,
    value: item.value,
    numericValue: item.numericValue,
    specDefinition: item.specDefinition
      ? {
          id: item.specDefinition.id,
          name: item.specDefinition.name,
          unit: item.specDefinition.unit,
          dataType: item.specDefinition.dataType,
          isComparable: item.specDefinition.isComparable,
        }
      : null,
  };
}

export function toPriceEntryDto(item) {
  return {
    id: item.id,
    productId: item.productId,
    sellerId: item.sellerId,
    price: item.price,
    seller: item.seller
      ? {
          id: item.seller.id,
          name: item.seller.name,
          trustScore: item.seller.trustScore,
          trustGrade: item.seller.trustGrade,
          isActive: item.seller.isActive,
        }
      : null,
  };
}

export function toPriceHistoryDto(item) {
  return {
    id: item.id,
    productId: item.productId,
    date: item.date,
    minPrice: item.minPrice,
    avgPrice: item.avgPrice,
    maxPrice: item.maxPrice,
  };
}
