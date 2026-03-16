export function toSellerDto(item) {
  return {
    id: item.id,
    name: item.name,
    url: item.url,
    logoUrl: item.logoUrl,
    trustScore: item.trustScore,
    trustGrade: item.trustGrade,
    description: item.description,
    isActive: item.isActive,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
  };
}
