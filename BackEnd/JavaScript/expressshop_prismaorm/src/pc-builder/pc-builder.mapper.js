function toIsoString(value) {
  return value instanceof Date ? value.toISOString() : value ?? null;
}

function resolveProductPrice(product) {
  return product?.lowestPrice ?? product?.discountPrice ?? product?.price ?? 0;
}

export function toPcBuildPartDto(part) {
  return {
    id: part.id,
    partType: part.partType,
    quantity: part.quantity,
    unitPrice: part.unitPrice,
    totalPrice: part.totalPrice,
    product: {
      id: part.product.id,
      name: part.product.name,
      lowestPrice: resolveProductPrice(part.product),
    },
    seller: part.seller
      ? {
          id: part.seller.id,
          name: part.seller.name,
          price: part.unitPrice,
        }
      : null,
  };
}

export function toPcBuildSummaryDto(build) {
  return {
    id: build.id,
    name: build.name,
    description: build.description,
    purpose: build.purpose,
    budget: build.budget,
    totalPrice: build.totalPrice,
    shareCode: build.shareCode,
    viewCount: build.viewCount,
    partCount: build.pcParts?.length ?? 0,
    createdAt: toIsoString(build.createdAt),
    updatedAt: toIsoString(build.updatedAt),
  };
}

export function toPcBuildDetailDto(build, compatibility) {
  return {
    ...toPcBuildSummaryDto(build),
    userId: build.userId,
    parts: (build.pcParts ?? []).map(toPcBuildPartDto),
    compatibility,
    bottleneck: compatibility?.bottleneck ?? null,
  };
}

export function toCompatibilityRuleDto(rule) {
  return {
    id: rule.id,
    partType: rule.partType,
    targetPartType: rule.targetPartType,
    title: rule.title,
    description: rule.description,
    severity: rule.severity,
    enabled: rule.enabled,
    metadata: rule.metadata ?? null,
    createdAt: toIsoString(rule.createdAt),
    updatedAt: toIsoString(rule.updatedAt),
  };
}
