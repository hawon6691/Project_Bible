export function toRecentViewDto(item) {
  return {
    id: item.id,
    userId: item.userId,
    productId: item.productId,
    viewedAt: item.viewedAt,
    product: item.product ?? undefined,
  };
}

export function toSearchHistoryDto(item) {
  return {
    id: item.id,
    userId: item.userId,
    keyword: item.keyword,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
  };
}
