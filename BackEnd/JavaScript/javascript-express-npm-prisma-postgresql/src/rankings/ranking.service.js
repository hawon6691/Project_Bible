import {
  findPopularProducts,
  findPopularSearchKeywords,
  findProductsForRecalculation,
  recalculatePopularityScores,
} from "./ranking.repository.js";

export async function getPopularProducts(query) {
  const limit = Math.min(Math.max(Number(query?.limit ?? 20), 1), 100);
  const items = await findPopularProducts({
    categoryId: query?.categoryId,
    limit,
  });

  return {
    items: items.map((item, index) => ({
      rank: index + 1,
      rankChange: 0,
      product: item,
      score: Number(item.popularityScore ?? 0),
    })),
    meta: { total: items.length, limit },
  };
}

export async function getPopularSearches(query) {
  const limit = Math.min(Math.max(Number(query?.limit ?? 20), 1), 100);
  const items = await findPopularSearchKeywords(limit);
  return {
    items: items.map((item, index) => ({
      rank: index + 1,
      keyword: item.keyword,
      searchCount: item._count.keyword,
      rankChange: 0,
    })),
    meta: { total: items.length, limit },
  };
}

export async function recalculateRankings() {
  const products = await findProductsForRecalculation();
  await recalculatePopularityScores(products);
  return { updatedCount: products.length };
}
