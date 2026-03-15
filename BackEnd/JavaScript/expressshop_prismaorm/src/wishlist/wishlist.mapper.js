export function toWishlistItemDto(item) {
  return {
    id: item.id,
    userId: item.userId,
    productId: item.productId,
    createdAt: item.createdAt,
    product: item.product ?? undefined,
  };
}
