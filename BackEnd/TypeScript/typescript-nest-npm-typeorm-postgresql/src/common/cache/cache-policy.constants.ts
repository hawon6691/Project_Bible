export const CACHE_TTL_SECONDS = {
  PRODUCT_LIST: 60 * 5,
  PRODUCT_DETAIL: 60 * 5,
  PRICE_COMPARE: 60 * 5,
  PRICE_HISTORY: 60 * 5,
  RANKING_PRODUCTS: 60 * 60,
  RANKING_KEYWORDS: 60 * 60,
  NEWS_LIST: 60 * 10,
  NEWS_DETAIL: 60 * 10,
  NEWS_CATEGORIES: 60 * 10,
} as const;

export const CACHE_KEY_PREFIX = {
  PRODUCT: 'cache:product',
  PRICE: 'cache:price',
  RANKING: 'cache:ranking',
  NEWS: 'cache:news',
} as const;

// 모듈별 캐시 키를 중앙 관리해 중복 키/오타를 방지한다.
export const CACHE_KEYS = {
  productList: (queryHash: string) => `${CACHE_KEY_PREFIX.PRODUCT}:list:${queryHash}`,
  productDetail: (id: number) => `${CACHE_KEY_PREFIX.PRODUCT}:detail:${id}`,
  priceCompare: (productId: number) => `${CACHE_KEY_PREFIX.PRICE}:compare:${productId}`,
  priceHistory: (productId: number, period: string) =>
    `${CACHE_KEY_PREFIX.PRICE}:history:${productId}:${period}`,
  rankingProducts: (limit: number) => `${CACHE_KEY_PREFIX.RANKING}:products:${limit}`,
  rankingKeywords: (limit: number) => `${CACHE_KEY_PREFIX.RANKING}:keywords:${limit}`,
  newsCategories: () => `${CACHE_KEY_PREFIX.NEWS}:categories`,
  newsList: (queryHash: string) => `${CACHE_KEY_PREFIX.NEWS}:list:${queryHash}`,
  newsDetail: (newsId: number) => `${CACHE_KEY_PREFIX.NEWS}:detail:${newsId}`,
} as const;
