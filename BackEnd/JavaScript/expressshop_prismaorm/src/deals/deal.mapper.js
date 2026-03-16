export function toDealDto(item) {
  return {
    id: item.id,
    productId: item.productId,
    title: item.title,
    description: item.description,
    discountRate: item.discountRate,
    startAt: item.startAt,
    endAt: item.endAt,
    isActive: item.isActive,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
    product: item.product ?? undefined,
  };
}

export function toDealDetailDto(item) {
  return {
    ...toDealDto(item),
    products: (item.dealProducts ?? []).map((dealProduct) => ({
      id: dealProduct.id,
      productId: dealProduct.productId,
      dealPrice: dealProduct.dealPrice,
      stock: dealProduct.stock,
      soldCount: dealProduct.soldCount,
      product: dealProduct.product ?? undefined,
    })),
  };
}
