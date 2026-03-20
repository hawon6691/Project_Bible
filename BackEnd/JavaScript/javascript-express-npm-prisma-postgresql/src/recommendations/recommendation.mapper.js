import { toProductSummaryDto } from "../products/product.mapper.js";

export function toRecommendationDto(item) {
  return {
    id: item.id,
    productId: item.productId,
    type: item.type,
    sortOrder: item.sortOrder,
    startDate: item.startDate,
    endDate: item.endDate,
    createdAt: item.createdAt,
    product: item.product ? toProductSummaryDto(item.product) : undefined,
  };
}
