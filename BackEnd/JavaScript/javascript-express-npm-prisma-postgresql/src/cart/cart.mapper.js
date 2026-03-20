export function toCartItemDto(item) {
  return {
    id: item.id,
    userId: item.userId,
    productId: item.productId,
    sellerId: item.sellerId,
    selectedOptions: item.selectedOptions,
    quantity: item.quantity,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
    product: item.product,
    seller: item.seller,
  };
}
