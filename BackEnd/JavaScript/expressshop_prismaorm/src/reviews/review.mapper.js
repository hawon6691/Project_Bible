export function toReviewDto(item) {
  return {
    id: item.id,
    userId: item.userId,
    productId: item.productId,
    orderId: item.orderId,
    rating: item.rating,
    content: item.content,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
    user: item.user ?? undefined,
  };
}
