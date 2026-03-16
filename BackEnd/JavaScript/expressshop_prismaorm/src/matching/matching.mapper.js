export function toMappingDto(item) {
  return {
    id: item.id,
    crawledProductName: item.crawledProductName,
    extractedModel: item.extractedModel,
    sellerId: item.sellerId,
    productId: item.productId,
    status: item.status,
    confidence: item.confidence,
    reviewedBy: item.reviewedBy,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
    seller: item.seller ?? undefined,
    product: item.product ?? undefined,
    reviewer: item.reviewer ?? undefined,
  };
}
