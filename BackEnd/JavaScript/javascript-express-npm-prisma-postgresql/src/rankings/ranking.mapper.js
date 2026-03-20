export function toRankedProductDto(item) {
  return {
    rank: item.rank,
    rankChange: item.rankChange,
    product: {
      id: item.product.id,
      name: item.product.name,
      lowestPrice: item.product.lowestPrice,
      thumbnailUrl: item.product.thumbnailUrl,
    },
    score: item.score,
  };
}

export function toSearchRankDto(item) {
  return {
    rank: item.rank,
    keyword: item.keyword,
    searchCount: item.searchCount,
    rankChange: item.rankChange,
  };
}
