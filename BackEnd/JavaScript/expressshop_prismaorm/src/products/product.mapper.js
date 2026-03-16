function computePriceSummary(item) {
  const prices = (item.priceEntries ?? []).map((entry) => Number(entry.price)).filter(Number.isFinite);
  if (prices.length === 0) {
    return {
      lowestPrice: item.lowestPrice ?? null,
      highestPrice: null,
      averagePrice: null,
    };
  }

  const total = prices.reduce((sum, value) => sum + value, 0);
  return {
    lowestPrice: Math.min(...prices),
    highestPrice: Math.max(...prices),
    averagePrice: Math.round(total / prices.length),
  };
}

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
    createdAt: item.createdAt,
    category: item.category
      ? {
          id: item.category.id,
          name: item.category.name,
        }
      : null,
  };
}

export function toProductDetailDto(item) {
  const priceSummary = computePriceSummary(item);
  return {
    ...toProductSummaryDto(item),
    lowestPrice: priceSummary.lowestPrice,
    highestPrice: priceSummary.highestPrice,
    averagePrice: priceSummary.averagePrice,
    category: item.category
      ? {
          id: item.category.id,
          name: item.category.name,
          parentId: item.category.parentId,
        }
      : null,
    options: (item.options ?? []).map((option) => toProductOptionDto(option)),
    images: (item.images ?? []).map((image) => ({
      id: image.id,
      url: image.url,
      isMain: image.isMain,
      sortOrder: image.sortOrder,
      createdAt: image.createdAt,
    })),
    specs: (item.specs ?? []).map((spec) => toProductSpecDto(spec)),
    priceEntries: (item.priceEntries ?? []).map((entry) => toPriceEntryDto(entry)),
  };
}

export function toProductOptionDto(item) {
  return {
    id: item.id,
    name: item.name,
    values: item.values,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
  };
}

export function toSpecDefinitionDto(item) {
  return {
    id: item.id,
    name: item.name,
    type: item.type,
    options: item.options,
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

export function toSpecScoreDto(item) {
  return {
    id: item.id,
    specDefinitionId: item.specDefinitionId,
    value: item.value,
    score: item.score,
    benchmarkSource: item.benchmarkSource,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
  };
}

export function toPriceEntryDto(item) {
  return {
    id: item.id,
    productId: item.productId,
    sellerId: item.sellerId,
    price: item.price,
    shippingCost: item.shippingCost,
    shippingInfo: item.shippingInfo,
    productUrl: item.productUrl,
    shippingFee: item.shippingFee,
    shippingType: item.shippingType,
    updatedAt: item.updatedAt,
    seller: item.seller
      ? {
          id: item.seller.id,
          name: item.seller.name,
          logoUrl: item.seller.logoUrl,
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
    lowestPrice: item.lowestPrice,
    averagePrice: item.averagePrice,
    highestPrice: item.highestPrice,
  };
}

export function toPriceAlertDto(item) {
  return {
    id: item.id,
    productId: item.productId,
    productName: item.product?.name ?? null,
    targetPrice: item.targetPrice,
    currentLowestPrice: item.product?.lowestPrice ?? null,
    isTriggered: item.isTriggered,
    createdAt: item.createdAt,
  };
}
