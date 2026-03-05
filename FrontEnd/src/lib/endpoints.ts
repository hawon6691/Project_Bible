import { request } from '@/lib/apiClient';
import { clearAuth, getAccessToken, getRefreshToken } from '@/lib/auth';
import { getOrCreateGuestCartKey } from '@/lib/cartKey';
import { API_BASE_URL } from '@/lib/config';
import type {
  AddCartItemPayload,
  Address,
  ActivitySearchItem,
  ActivitySummary,
  BoardItem,
  CartItem,
  Category,
  ChatMessageItem,
  ChatRoomItem,
  CreateAddressPayload,
  CreateOrderPayload,
  FaqItem,
  InquiryItem,
  ImageUploadResult,
  ImageVariantItem,
  NoticeItem,
  PostCommentItem,
  PostDetailItem,
  PostSummaryItem,
  PricePredictionResult,
  CreateReviewPayload,
  DealItem,
  FraudAlertItem,
  FraudRealPriceResult,
  LoginPayload,
  NewsItem,
  ObservabilityDashboard,
  ObservabilityMetricsSummary,
  ObservabilityTraceItem,
  OrderDetail,
  OrderSummary,
  PopularKeywordItem,
  ProductDetail,
  ProductQuery,
  ProductSummary,
  RecommendationResult,
  RankingProductItem,
  RequestPasswordResetPayload,
  ResetPasswordPayload,
  ReviewItem,
  SearchResultItem,
  SignupPayload,
  SupportTicketItem,
  TokenResponse,
  TranslationItem,
  BadgeItem,
  PcBuildSummaryItem,
  PcBuildDetailItem,
  PcCompatibilityRuleItem,
  FriendListItem,
  FriendFeedItem,
  ShortformItem,
  ShortformCommentItem,
  MediaAssetItem,
  NewsCategoryItem,
  NewsSummaryItem,
  NewsDetailItem,
  ProductMappingItem,
  LowestEverAnalyticsResult,
  UnitPriceAnalyticsResult,
  UsedProductPriceResult,
  UsedCategoryPriceItem,
  AutoModelItem,
  AutoTrimItem,
  AutoEstimateResult,
  AutoLeaseOfferItem,
  AuctionSummaryItem,
  AuctionDetailItem,
  AuctionBidItem,
  CompareListItem,
  CompareDetailResult,
  AdminAllowedExtensionsResult,
  AdminUploadLimitsResult,
  AdminReviewPolicyResult,
  HealthCheckResult,
  ResilienceCircuitSnapshot,
  ResiliencePolicyItem,
  ErrorCodeItem,
  OpsDashboardSummary,
  QueueAutoRetryResult,
  QueueFailedJobsResult,
  QueueRetryFailedResult,
  QueueStatsResult,
  UserBadgeItem,
  ExchangeRateItem,
  ConvertedAmountResult,
  TrustCurrentScore,
  TrustHistoryItem,
  UpdateCartQuantityPayload,
  UserProfile,
  VerifyEmailPayload,
  VerifyResetCodePayload,
  WishlistItem,
} from '@/lib/types';

function authToken() {
  return getAccessToken();
}

function requireToken() {
  const token = authToken();
  if (!token) {
    throw new Error('login required');
  }
  return token;
}

export async function fetchCategories() {
  return request<Category[]>('/categories');
}

export async function fetchProducts(query: ProductQuery) {
  return request<ProductSummary[]>('/products', { query: query as Record<string, unknown> });
}

export async function fetchProduct(id: number) {
  return request<ProductDetail>(`/products/${id}`);
}

export async function searchProducts(query: {
  keyword?: string;
  categoryId?: number;
  minPrice?: number;
  maxPrice?: number;
  minRating?: number;
  page?: number;
  limit?: number;
}) {
  return request<SearchResultItem[]>('/search', { query: query as Record<string, unknown> });
}

export async function fetchPopularKeywords(limit = 10) {
  return request<{ items: PopularKeywordItem[] }>('/search/popular', { query: { limit } });
}

export async function fetchRankingProducts(limit = 10) {
  return request<RankingProductItem[]>('/rankings/products/popular', { query: { limit } });
}

export async function fetchRankingKeywords(limit = 10) {
  return request<PopularKeywordItem[]>('/rankings/keywords/popular', { query: { limit } });
}

export async function recalculateRankingAdmin() {
  return request<{ updatedCount: number }>('/rankings/admin/recalculate', {
    method: 'POST',
    token: requireToken(),
  });
}

export async function fetchRecommendationTrending(limit = 20) {
  return request<RecommendationResult>('/recommendations/trending', {
    query: { limit },
  });
}

export async function fetchRecommendationPersonal(limit = 20) {
  return request<RecommendationResult>('/recommendations/personal', {
    token: requireToken(),
    query: { limit },
  });
}

export async function fetchNews(limit = 6) {
  return request<NewsItem[]>('/news', { query: { page: 1, limit } });
}

export async function fetchNewsCategories() {
  return request<NewsCategoryItem[]>('/news/categories');
}

export async function fetchNewsDetail(id: number) {
  return request<NewsDetailItem>(`/news/${id}`);
}

export async function createNewsAdmin(payload: {
  title: string;
  content: string;
  categoryId: number;
  thumbnailUrl?: string;
  productIds?: number[];
}) {
  return request<NewsDetailItem>('/news', {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function updateNewsAdmin(
  id: number,
  payload: {
    title?: string;
    content?: string;
    categoryId?: number;
    thumbnailUrl?: string;
    productIds?: number[];
  },
) {
  return request<NewsDetailItem>(`/news/${id}`, {
    method: 'PATCH',
    token: requireToken(),
    body: payload,
  });
}

export async function removeNewsAdmin(id: number) {
  return request<{ success: boolean; message: string }>(`/news/${id}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function createNewsCategoryAdmin(payload: { name: string; slug: string }) {
  return request<NewsCategoryItem>('/news/categories', {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function removeNewsCategoryAdmin(id: number) {
  return request<{ success: boolean; message: string }>(`/news/categories/${id}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function fetchDeals(limit = 6) {
  return request<DealItem[]>('/deals', { query: { limit, activeOnly: true } });
}

export async function createDealAdmin(payload: {
  productId: number;
  title: string;
  description?: string;
  discountRate: number;
  startAt: string;
  endAt: string;
  isActive?: boolean;
}) {
  return request<DealItem>('/deals/admin', {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function updateDealAdmin(
  id: number,
  payload: {
    productId?: number;
    title?: string;
    description?: string;
    discountRate?: number;
    startAt?: string;
    endAt?: string;
    isActive?: boolean;
  },
) {
  return request<DealItem>(`/deals/admin/${id}`, {
    method: 'PATCH',
    token: requireToken(),
    body: payload,
  });
}

export async function removeDealAdmin(id: number) {
  return request<{ message: string }>(`/deals/admin/${id}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function fetchPricePrediction(
  productId: number,
  query?: { horizonDays?: number; lookbackDays?: number },
) {
  return request<PricePredictionResult>(`/predictions/products/${productId}/price-trend`, {
    query: query as Record<string, unknown> | undefined,
  });
}

export async function fetchFraudAlertsAdmin(query?: {
  page?: number;
  limit?: number;
  status?: 'PENDING' | 'APPROVED' | 'REJECTED';
}) {
  return request<FraudAlertItem[]>('/fraud/alerts', {
    token: requireToken(),
    query: query as Record<string, unknown> | undefined,
  });
}

export async function approveFraudAlertAdmin(alertId: number) {
  return request<{ success: boolean; message: string }>(`/fraud/alerts/${alertId}/approve`, {
    method: 'PATCH',
    token: requireToken(),
  });
}

export async function rejectFraudAlertAdmin(alertId: number) {
  return request<{ success: boolean; message: string }>(`/fraud/alerts/${alertId}/reject`, {
    method: 'PATCH',
    token: requireToken(),
  });
}

export async function fetchProductRealPrice(productId: number, sellerId?: number) {
  return request<FraudRealPriceResult>(`/products/${productId}/real-price`, {
    query: sellerId ? { sellerId } : undefined,
  });
}

export async function fetchEffectivePrices(productId: number) {
  return request<Array<{
    priceEntryId: number;
    sellerId: number;
    sellerName?: string;
    productPrice: number;
    shippingFee: number;
    shippingType: string;
    effectivePrice: number;
    shippingInfo: string | null;
    productUrl: string;
  }>>(`/fraud/products/${productId}/effective-prices`);
}

export async function detectFraudAnomalies(productId: number, query?: {
  lowerBoundRatio?: number;
  upperBoundRatio?: number;
  limit?: number;
}) {
  return request<{
    productId: number;
    baselineAverage: number;
    lowerBound: number;
    upperBound: number;
    scannedCount: number;
    anomalyCount: number;
    anomalies: Array<{
      priceEntryId: number;
      sellerId: number;
      sellerName?: string;
      rawPrice: number;
      effectivePrice: number;
      baselineAverage: number;
      lowerBound: number;
      upperBound: number;
      severity: 'LOW' | 'MEDIUM' | 'HIGH';
      reason: string;
    }>;
  }>(`/fraud/products/${productId}/anomalies`, {
    query: query as Record<string, unknown> | undefined,
  });
}

export async function scanFraudAnomaliesAdmin(productId: number, query?: {
  lowerBoundRatio?: number;
  upperBoundRatio?: number;
  limit?: number;
}) {
  return request<{
    productId: number;
    baselineAverage: number;
    lowerBound: number;
    upperBound: number;
    scannedCount: number;
    anomalyCount: number;
    anomalies: Array<{
      priceEntryId: number;
      sellerId: number;
      sellerName?: string;
      rawPrice: number;
      effectivePrice: number;
      baselineAverage: number;
      lowerBound: number;
      upperBound: number;
      severity: 'LOW' | 'MEDIUM' | 'HIGH';
      reason: string;
    }>;
  }>(`/fraud/admin/products/${productId}/scan`, {
    method: 'POST',
    token: requireToken(),
    query: query as Record<string, unknown> | undefined,
  });
}

export async function fetchFraudFlagsAdmin(productId: number) {
  return request<FraudAlertItem[]>(`/fraud/admin/products/${productId}/flags`, {
    token: requireToken(),
  });
}

export async function fetchTrustCurrentScore(sellerId: number) {
  return request<TrustCurrentScore>(`/trust/sellers/${sellerId}`);
}

export async function fetchTrustHistory(sellerId: number, limit = 20) {
  return request<TrustHistoryItem[]>(`/trust/sellers/${sellerId}/history`, {
    query: { limit },
  });
}

export async function recalculateTrustScoreAdmin(
  sellerId: number,
  payload: {
    deliveryAccuracy: number;
    priceAccuracy: number;
    customerRating: number;
    responseSpeed: number;
    returnRate: number;
  },
) {
  return request<{
    sellerId: number;
    sellerName: string;
    trustScore: number;
    trustGrade: string;
    history: TrustHistoryItem;
  }>(`/trust/admin/sellers/${sellerId}/recalculate`, {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function fetchTranslations(query?: {
  locale?: string;
  namespace?: string;
  key?: string;
}) {
  return request<TranslationItem[]>('/i18n/translations', {
    query: query as Record<string, unknown> | undefined,
  });
}

export async function upsertTranslationAdmin(payload: {
  locale: string;
  namespace: string;
  key: string;
  value: string;
}) {
  return request<TranslationItem>('/i18n/admin/translations', {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function removeTranslationAdmin(id: number) {
  return request<{ success: boolean; message: string }>(`/i18n/admin/translations/${id}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function fetchExchangeRates() {
  return request<ExchangeRateItem[]>('/i18n/exchange-rates');
}

export async function upsertExchangeRateAdmin(payload: {
  baseCurrency: string;
  targetCurrency: string;
  rate: number;
}) {
  return request<ExchangeRateItem>('/i18n/admin/exchange-rates', {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function convertAmount(query: {
  amount: number;
  from: string;
  to: string;
}) {
  return request<ConvertedAmountResult>('/i18n/convert', {
    query: query as Record<string, unknown>,
  });
}

export async function uploadImage(
  file: File,
  category: 'product' | 'community' | 'support' | 'seller',
) {
  const token = requireToken();
  const formData = new FormData();
  formData.append('file', file);
  formData.append('category', category);

  const res = await fetch(`${API_BASE_URL}/images/upload`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`,
    },
    body: formData,
    credentials: 'include',
  });

  const raw = await res.json().catch(() => ({}));
  if (!res.ok) {
    const message =
      typeof raw?.message === 'string'
        ? raw.message
        : Array.isArray(raw?.message)
          ? raw.message.join(', ')
          : `HTTP ${res.status}`;
    throw new Error(message);
  }

  if (raw && typeof raw === 'object' && 'success' in raw && 'data' in raw) {
    return { data: raw.data as ImageUploadResult };
  }

  return { data: raw as ImageUploadResult };
}

export async function fetchImageVariants(imageId: number) {
  return request<ImageVariantItem[]>(`/images/${imageId}/variants`);
}

export async function removeImageAdmin(imageId: number) {
  return request<{ success: boolean; message: string }>(`/images/${imageId}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function fetchBadges() {
  return request<BadgeItem[]>('/badges');
}

export async function fetchMyBadges() {
  return request<UserBadgeItem[]>('/badges/me', {
    token: requireToken(),
  });
}

export async function fetchUserBadges(userId: number) {
  return request<UserBadgeItem[]>(`/users/${userId}/badges`);
}

export async function createBadgeAdmin(payload: {
  name: string;
  description: string;
  iconUrl: string;
  type: 'AUTO' | 'MANUAL';
  condition?: {
    metric: 'review_count' | 'post_count' | 'order_count' | 'point_total' | 'login_streak';
    threshold: number;
  };
  rarity: 'COMMON' | 'UNCOMMON' | 'RARE' | 'EPIC' | 'LEGENDARY';
}) {
  return request<BadgeItem>('/admin/badges', {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function updateBadgeAdmin(
  id: number,
  payload: {
    name?: string;
    description?: string;
    iconUrl?: string;
    type?: 'AUTO' | 'MANUAL';
    condition?: {
      metric: 'review_count' | 'post_count' | 'order_count' | 'point_total' | 'login_streak';
      threshold: number;
    };
    rarity?: 'COMMON' | 'UNCOMMON' | 'RARE' | 'EPIC' | 'LEGENDARY';
  },
) {
  return request<BadgeItem>(`/admin/badges/${id}`, {
    method: 'PATCH',
    token: requireToken(),
    body: payload,
  });
}

export async function removeBadgeAdmin(id: number) {
  return request<{ success: boolean; message: string }>(`/admin/badges/${id}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function grantBadgeAdmin(
  badgeId: number,
  payload: {
    userId: number;
    reason?: string;
  },
) {
  return request<UserBadgeItem>(`/admin/badges/${badgeId}/grant`, {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function revokeBadgeAdmin(badgeId: number, userId: number) {
  return request<{ success: boolean; message: string }>(`/admin/badges/${badgeId}/revoke/${userId}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function fetchMyPcBuilds(page = 1, limit = 20) {
  return request<PcBuildSummaryItem[]>('/pc-builds', {
    token: requireToken(),
    query: { page, limit },
  });
}

export async function createPcBuild(payload: {
  name: string;
  description?: string;
  purpose: 'GAMING' | 'OFFICE' | 'DESIGN' | 'DEVELOPMENT' | 'STREAMING';
  budget?: number;
}) {
  return request<PcBuildDetailItem>('/pc-builds', {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function fetchPcBuildDetail(id: number) {
  return request<PcBuildDetailItem>(`/pc-builds/${id}`);
}

export async function updatePcBuild(
  id: number,
  payload: {
    name?: string;
    description?: string;
    purpose?: 'GAMING' | 'OFFICE' | 'DESIGN' | 'DEVELOPMENT' | 'STREAMING';
    budget?: number;
  },
) {
  return request<PcBuildDetailItem>(`/pc-builds/${id}`, {
    method: 'PATCH',
    token: requireToken(),
    body: payload,
  });
}

export async function removePcBuild(id: number) {
  return request<{ success: boolean; message: string }>(`/pc-builds/${id}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function addPcBuildPart(
  id: number,
  payload: {
    productId: number;
    partType: 'CPU' | 'MOTHERBOARD' | 'RAM' | 'GPU' | 'SSD' | 'HDD' | 'PSU' | 'CASE' | 'COOLER' | 'MONITOR';
    sellerId?: number;
    quantity: number;
  },
) {
  return request<PcBuildDetailItem>(`/pc-builds/${id}/parts`, {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function removePcBuildPart(id: number, partId: number) {
  return request<PcBuildDetailItem>(`/pc-builds/${id}/parts/${partId}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function fetchPcBuildCompatibility(id: number) {
  return request<PcBuildDetailItem['compatibility']>(`/pc-builds/${id}/compatibility`);
}

export async function createPcBuildShareLink(id: number) {
  return request<{ shareCode: string; shareUrl: string }>(`/pc-builds/${id}/share`, {
    token: requireToken(),
  });
}

export async function fetchSharedPcBuild(shareCode: string) {
  return request<PcBuildDetailItem>(`/pc-builds/shared/${shareCode}`);
}

export async function fetchPopularPcBuilds(page = 1, limit = 20) {
  return request<PcBuildSummaryItem[]>('/pc-builds/popular', {
    query: { page, limit },
  });
}

export async function fetchCompatibilityRulesAdmin() {
  return request<PcCompatibilityRuleItem[]>('/admin/compatibility-rules', {
    token: requireToken(),
  });
}

export async function createCompatibilityRuleAdmin(payload: {
  partType: 'CPU' | 'MOTHERBOARD' | 'RAM' | 'GPU' | 'SSD' | 'HDD' | 'PSU' | 'CASE' | 'COOLER' | 'MONITOR';
  targetPartType?: 'CPU' | 'MOTHERBOARD' | 'RAM' | 'GPU' | 'SSD' | 'HDD' | 'PSU' | 'CASE' | 'COOLER' | 'MONITOR';
  title: string;
  description: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH';
  enabled?: boolean;
  metadata?: { required?: boolean };
}) {
  return request<PcCompatibilityRuleItem>('/admin/compatibility-rules', {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function updateCompatibilityRuleAdmin(
  id: number,
  payload: {
    partType?: 'CPU' | 'MOTHERBOARD' | 'RAM' | 'GPU' | 'SSD' | 'HDD' | 'PSU' | 'CASE' | 'COOLER' | 'MONITOR';
    targetPartType?: 'CPU' | 'MOTHERBOARD' | 'RAM' | 'GPU' | 'SSD' | 'HDD' | 'PSU' | 'CASE' | 'COOLER' | 'MONITOR';
    title?: string;
    description?: string;
    severity?: 'LOW' | 'MEDIUM' | 'HIGH';
    enabled?: boolean;
    metadata?: { required?: boolean };
  },
) {
  return request<PcCompatibilityRuleItem>(`/admin/compatibility-rules/${id}`, {
    method: 'PATCH',
    token: requireToken(),
    body: payload,
  });
}

export async function removeCompatibilityRuleAdmin(id: number) {
  return request<{ success: boolean; message: string }>(`/admin/compatibility-rules/${id}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function requestFriend(userId: number) {
  return request<{ success: boolean; message: string }>(`/friends/request/${userId}`, {
    method: 'POST',
    token: requireToken(),
  });
}

export async function acceptFriendRequest(friendshipId: number) {
  return request<{ success: boolean; message: string }>(`/friends/request/${friendshipId}/accept`, {
    method: 'PATCH',
    token: requireToken(),
  });
}

export async function rejectFriendRequest(friendshipId: number) {
  return request<{ success: boolean; message: string }>(`/friends/request/${friendshipId}/reject`, {
    method: 'PATCH',
    token: requireToken(),
  });
}

export async function fetchFriends(page = 1, limit = 20) {
  return request<FriendListItem[]>('/friends', {
    token: requireToken(),
    query: { page, limit },
  });
}

export async function fetchReceivedFriendRequests(page = 1, limit = 20) {
  return request<FriendListItem[]>('/friends/requests/received', {
    token: requireToken(),
    query: { page, limit },
  });
}

export async function fetchSentFriendRequests(page = 1, limit = 20) {
  return request<FriendListItem[]>('/friends/requests/sent', {
    token: requireToken(),
    query: { page, limit },
  });
}

export async function fetchFriendFeed(page = 1, limit = 20) {
  return request<FriendFeedItem[]>('/friends/feed', {
    token: requireToken(),
    query: { page, limit },
  });
}

export async function blockUser(userId: number) {
  return request<{ success: boolean; message: string }>(`/friends/block/${userId}`, {
    method: 'POST',
    token: requireToken(),
  });
}

export async function unblockUser(userId: number) {
  return request<{ success: boolean; message: string }>(`/friends/block/${userId}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function removeFriend(userId: number) {
  return request<{ success: boolean; message: string }>(`/friends/${userId}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function createShortform(payload: {
  videoFile: File;
  title: string;
  durationSec?: number;
  productIds?: number[];
}) {
  const token = requireToken();
  const formData = new FormData();
  formData.append('video', payload.videoFile);
  formData.append('title', payload.title);
  if (payload.durationSec !== undefined) {
    formData.append('durationSec', String(payload.durationSec));
  }
  if (payload.productIds?.length) {
    formData.append('productIds', JSON.stringify(payload.productIds));
  }

  const res = await fetch(`${API_BASE_URL}/shortforms`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`,
    },
    body: formData,
    credentials: 'include',
  });

  const raw = await res.json().catch(() => ({}));
  if (!res.ok) {
    const message =
      typeof raw?.message === 'string'
        ? raw.message
        : Array.isArray(raw?.message)
          ? raw.message.join(', ')
          : `HTTP ${res.status}`;
    throw new Error(message);
  }

  if (raw && typeof raw === 'object' && 'success' in raw && 'data' in raw) {
    return { data: raw.data as ShortformItem };
  }
  return { data: raw as ShortformItem };
}

export async function fetchShortformFeed(page = 1, limit = 20) {
  return request<ShortformItem[]>('/shortforms', {
    query: { page, limit },
  });
}

export async function fetchShortformDetail(id: number) {
  return request<ShortformItem>(`/shortforms/${id}`);
}

export async function toggleShortformLike(id: number) {
  return request<{ liked: boolean; likeCount: number }>(`/shortforms/${id}/like`, {
    method: 'POST',
    token: requireToken(),
  });
}

export async function createShortformComment(id: number, payload: { content: string }) {
  return request<ShortformCommentItem>(`/shortforms/${id}/comments`, {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function fetchShortformComments(id: number, page = 1, limit = 20) {
  return request<ShortformCommentItem[]>(`/shortforms/${id}/comments`, {
    query: { page, limit },
  });
}

export async function fetchShortformRanking(period: 'day' | 'week' | 'month' = 'day', limit = 20) {
  return request<ShortformItem[]>('/shortforms/ranking/list', {
    query: { period, limit },
  });
}

export async function removeShortform(id: number) {
  return request<{ success: boolean; message: string }>(`/shortforms/${id}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function fetchShortformTranscodeStatus(id: number) {
  return request<{
    shortformId: number;
    transcodeStatus: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
    transcodedVideoUrl: string | null;
    transcodeError: string | null;
    transcodedAt: string | null;
  }>(`/shortforms/${id}/transcode-status`);
}

export async function retryShortformTranscode(id: number) {
  return request<{ success: boolean; message: string }>(`/shortforms/${id}/transcode/retry`, {
    method: 'POST',
    token: requireToken(),
  });
}

export async function fetchUserShortforms(userId: number, page = 1, limit = 20) {
  return request<ShortformItem[]>(`/shortforms/user/${userId}`, {
    query: { page, limit },
  });
}

export async function uploadMedia(
  files: File[],
  payload: {
    ownerType: 'PRODUCT' | 'COMMUNITY' | 'SUPPORT' | 'SELLER' | 'SHORTFORM' | 'USER';
    ownerId: number;
  },
) {
  const token = requireToken();
  const formData = new FormData();
  files.forEach((file) => formData.append('files', file));
  formData.append('ownerType', payload.ownerType);
  formData.append('ownerId', String(payload.ownerId));

  const res = await fetch(`${API_BASE_URL}/media/upload`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`,
    },
    body: formData,
    credentials: 'include',
  });

  const raw = await res.json().catch(() => ({}));
  if (!res.ok) {
    const message =
      typeof raw?.message === 'string'
        ? raw.message
        : Array.isArray(raw?.message)
          ? raw.message.join(', ')
          : `HTTP ${res.status}`;
    throw new Error(message);
  }

  if (raw && typeof raw === 'object' && 'success' in raw && 'data' in raw) {
    return { data: raw.data as MediaAssetItem[] };
  }
  return { data: raw as MediaAssetItem[] };
}

export async function createMediaPresignedUrl(payload: {
  fileName: string;
  fileType: string;
  fileSize: number;
}) {
  return request<{ uploadUrl: string; fileKey: string; expiresInSec: number }>('/media/presigned-url', {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function fetchMediaStreamInfo(id: number) {
  return request<{
    id: number;
    fileUrl: string;
    mime: string;
    size: number;
    duration: number | null;
    resolution: string | null;
  }>(`/media/stream/${id}`);
}

export async function removeMedia(id: number) {
  return request<{ success: boolean; message: string }>(`/media/${id}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function fetchMediaMetadata(id: number) {
  return request<{
    id: number;
    mime: string;
    size: number;
    duration: number | null;
    resolution: string | null;
    ownerType: string;
    ownerId: number;
    uploadedAt: string;
  }>(`/media/${id}/metadata`);
}

export async function fetchPendingMappings(page = 1, limit = 20) {
  return request<ProductMappingItem[]>('/matching/pending', {
    token: requireToken(),
    query: { page, limit },
  });
}

export async function approveMapping(id: number, payload: { productId: number }) {
  return request<ProductMappingItem>(`/matching/${id}/approve`, {
    method: 'PATCH',
    token: requireToken(),
    body: payload,
  });
}

export async function rejectMapping(id: number, payload: { reason: string }) {
  return request<ProductMappingItem>(`/matching/${id}/reject`, {
    method: 'PATCH',
    token: requireToken(),
    body: payload,
  });
}

export async function autoMatchMappings() {
  return request<{ matchedCount: number; pendingCount: number }>('/matching/auto-match', {
    method: 'POST',
    token: requireToken(),
  });
}

export async function fetchMappingStats() {
  return request<{ pending: number; approved: number; rejected: number; total: number }>('/matching/stats', {
    token: requireToken(),
  });
}

export async function fetchLowestEverAnalytics(productId: number) {
  return request<LowestEverAnalyticsResult>(`/analytics/products/${productId}/lowest-ever`);
}

export async function fetchUnitPriceAnalytics(productId: number) {
  return request<UnitPriceAnalyticsResult>(`/analytics/products/${productId}/unit-price`);
}

export async function fetchUsedProductPrice(productId: number) {
  return request<UsedProductPriceResult>(`/used-market/products/${productId}/price`);
}

export async function fetchUsedCategoryPrices(categoryId: number, page = 1, limit = 20) {
  return request<UsedCategoryPriceItem[]>(`/used-market/categories/${categoryId}/prices`, {
    query: { page, limit },
  });
}

export async function estimateUsedPcBuildPrice(buildId: number) {
  return request<{
    buildId: number;
    estimatedPrice: number;
    partBreakdown: Array<{
      partId: number;
      partType: string;
      productId: number;
      productName: string | null;
      originalPrice: number;
      depreciationRate: number;
      estimatedUsedPrice: number;
    }>;
  }>(`/used-market/pc-builds/${buildId}/estimate`, {
    method: 'POST',
    token: requireToken(),
  });
}

export async function fetchAutoModels(query?: { brand?: string; type?: string }) {
  return request<AutoModelItem[]>('/auto/models', {
    query: query as Record<string, unknown> | undefined,
  });
}

export async function fetchAutoTrims(modelId: number) {
  return request<AutoTrimItem[]>(`/auto/models/${modelId}/trims`);
}

export async function estimateAuto(payload: {
  modelId: number;
  trimId: number;
  optionIds: number[];
}) {
  return request<AutoEstimateResult>('/auto/estimate', {
    method: 'POST',
    body: payload,
  });
}

export async function fetchAutoLeaseOffers(modelId: number) {
  return request<AutoLeaseOfferItem[]>(`/auto/models/${modelId}/lease-offers`);
}

export async function createAuction(payload: {
  title: string;
  description: string;
  categoryId: number;
  specs?: Record<string, unknown>;
  budget: number;
}) {
  return request<AuctionDetailItem>('/auctions', {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function fetchAuctions(query?: { page?: number; limit?: number; status?: string; categoryId?: number }) {
  return request<AuctionSummaryItem[]>('/auctions', {
    query: query as Record<string, unknown> | undefined,
  });
}

export async function fetchAuctionDetail(id: number) {
  return request<AuctionDetailItem>(`/auctions/${id}`);
}

export async function createAuctionBid(
  id: number,
  payload: { price: number; description?: string; deliveryDays: number },
) {
  return request<AuctionBidItem>(`/auctions/${id}/bids`, {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function selectAuctionBid(id: number, bidId: number) {
  return request<{ success: boolean; message: string }>(`/auctions/${id}/bids/${bidId}/select`, {
    method: 'PATCH',
    token: requireToken(),
  });
}

export async function cancelAuction(id: number) {
  return request<{ success: boolean; message: string }>(`/auctions/${id}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function updateAuctionBid(
  id: number,
  bidId: number,
  payload: { price?: number; description?: string; deliveryDays?: number },
) {
  return request<AuctionBidItem>(`/auctions/${id}/bids/${bidId}`, {
    method: 'PATCH',
    token: requireToken(),
    body: payload,
  });
}

export async function removeAuctionBid(id: number, bidId: number) {
  return request<{ success: boolean; message: string }>(`/auctions/${id}/bids/${bidId}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function addCompareItem(productId: number, compareKey?: string) {
  return request<{ compareList: CompareListItem[] }>('/compare/add', {
    method: 'POST',
    headers: compareKey ? { 'x-compare-key': compareKey } : undefined,
    body: { productId },
  });
}

export async function removeCompareItem(productId: number, compareKey?: string) {
  return request<{ compareList: CompareListItem[] }>(`/compare/${productId}`, {
    method: 'DELETE',
    headers: compareKey ? { 'x-compare-key': compareKey } : undefined,
  });
}

export async function fetchCompareList(compareKey?: string) {
  return request<{ compareList: CompareListItem[] }>('/compare', {
    headers: compareKey ? { 'x-compare-key': compareKey } : undefined,
  });
}

export async function fetchCompareDetail(compareKey?: string) {
  return request<CompareDetailResult>('/compare/detail', {
    headers: compareKey ? { 'x-compare-key': compareKey } : undefined,
  });
}

export async function fetchAllowedExtensionsAdmin() {
  return request<AdminAllowedExtensionsResult>('/admin/settings/extensions', {
    token: requireToken(),
  });
}

export async function setAllowedExtensionsAdmin(payload: { extensions: string[] }) {
  return request<AdminAllowedExtensionsResult>('/admin/settings/extensions', {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function fetchUploadLimitsAdmin() {
  return request<AdminUploadLimitsResult>('/admin/settings/upload-limits', {
    token: requireToken(),
  });
}

export async function updateUploadLimitsAdmin(payload: { image?: number; video?: number; audio?: number }) {
  return request<AdminUploadLimitsResult>('/admin/settings/upload-limits', {
    method: 'PATCH',
    token: requireToken(),
    body: payload,
  });
}

export async function fetchReviewPolicyAdmin() {
  return request<AdminReviewPolicyResult>('/admin/settings/review-policy', {
    token: requireToken(),
  });
}

export async function updateReviewPolicyAdmin(payload: { maxImageCount: number; pointAmount: number }) {
  return request<AdminReviewPolicyResult>('/admin/settings/review-policy', {
    method: 'PATCH',
    token: requireToken(),
    body: payload,
  });
}

export async function fetchHealthCheck() {
  return request<HealthCheckResult>('/health');
}

export async function fetchResilienceSnapshotsAdmin() {
  return request<{ items: ResilienceCircuitSnapshot[] }>('/resilience/circuit-breakers', {
    token: requireToken(),
  });
}

export async function fetchResiliencePoliciesAdmin() {
  return request<{ items: ResiliencePolicyItem[] }>('/resilience/circuit-breakers/policies', {
    token: requireToken(),
  });
}

export async function fetchResilienceSnapshotAdmin(name: string) {
  return request<ResilienceCircuitSnapshot>(`/resilience/circuit-breakers/${name}`, {
    token: requireToken(),
  });
}

export async function resetResilienceCircuitAdmin(name: string) {
  return request<ResilienceCircuitSnapshot>(`/resilience/circuit-breakers/${name}/reset`, {
    method: 'POST',
    token: requireToken(),
  });
}

export async function fetchErrorCodes() {
  return request<{ total: number; items: ErrorCodeItem[] }>('/errors/codes');
}

export async function fetchErrorCode(key: string) {
  return request<ErrorCodeItem | null>(`/errors/codes/${key}`);
}

export async function fetchSupportedQueuesAdmin() {
  return request<{ items: string[] }>('/admin/queues/supported', {
    token: requireToken(),
  });
}

export async function fetchQueueStatsAdmin() {
  return request<QueueStatsResult>('/admin/queues/stats', {
    token: requireToken(),
  });
}

export async function fetchQueueFailedJobsAdmin(
  queueName: string,
  query?: { page?: number; limit?: number; newestFirst?: boolean },
) {
  return request<QueueFailedJobsResult>(`/admin/queues/${queueName}/failed`, {
    token: requireToken(),
    query: {
      page: query?.page,
      limit: query?.limit,
      newestFirst: query?.newestFirst === undefined ? undefined : String(query.newestFirst),
    },
  });
}

export async function retryQueueFailedJobsAdmin(queueName: string, limit?: number) {
  return request<QueueRetryFailedResult>(`/admin/queues/${queueName}/failed/retry`, {
    method: 'POST',
    token: requireToken(),
    query: limit ? { limit } : undefined,
  });
}

export async function autoRetryQueuesAdmin(query?: { perQueueLimit?: number; maxTotal?: number }) {
  return request<QueueAutoRetryResult>('/admin/queues/auto-retry', {
    method: 'POST',
    token: requireToken(),
    query: query as Record<string, unknown> | undefined,
  });
}

export async function retryQueueJobAdmin(queueName: string, jobId: string) {
  return request<{ queueName: string; jobId: string; retried: true }>(
    `/admin/queues/${queueName}/jobs/${jobId}/retry`,
    {
      method: 'POST',
      token: requireToken(),
    },
  );
}

export async function removeQueueJobAdmin(queueName: string, jobId: string) {
  return request<{ queueName: string; jobId: string; removed: true }>(`/admin/queues/${queueName}/jobs/${jobId}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function fetchOpsDashboardSummaryAdmin() {
  return request<OpsDashboardSummary>('/admin/ops-dashboard/summary', {
    token: requireToken(),
  });
}

export async function fetchObservabilityMetricsAdmin() {
  return request<ObservabilityMetricsSummary>('/admin/observability/metrics', {
    token: requireToken(),
  });
}

export async function fetchObservabilityTracesAdmin(query?: { limit?: number; pathContains?: string }) {
  return request<{ items: ObservabilityTraceItem[] }>('/admin/observability/traces', {
    token: requireToken(),
    query: query as Record<string, unknown> | undefined,
  });
}

export async function fetchObservabilityDashboardAdmin() {
  return request<ObservabilityDashboard>('/admin/observability/dashboard', {
    token: requireToken(),
  });
}

export async function signup(payload: SignupPayload) {
  return request<{ id: number; email: string; name: string; message: string }>('/auth/signup', {
    method: 'POST',
    body: payload,
  });
}

export async function verifyEmail(payload: VerifyEmailPayload) {
  return request<{ message: string; verified: boolean }>('/auth/verify-email', {
    method: 'POST',
    body: payload,
  });
}

export async function resendVerificationEmail(email: string) {
  return request<{ message: string }>('/auth/resend-verification', {
    method: 'POST',
    body: { email },
  });
}

export async function requestPasswordReset(payload: RequestPasswordResetPayload) {
  return request<{ message: string }>('/auth/password-reset/request', {
    method: 'POST',
    body: payload,
  });
}

export async function verifyPasswordResetCode(payload: VerifyResetCodePayload) {
  return request<{ resetToken: string }>('/auth/password-reset/verify', {
    method: 'POST',
    body: payload,
  });
}

export async function resetPassword(payload: ResetPasswordPayload) {
  return request<{ message: string }>('/auth/password-reset/confirm', {
    method: 'POST',
    body: payload,
  });
}

export async function refreshToken() {
  const refreshTokenValue = getRefreshToken();
  if (!refreshTokenValue) {
    throw new Error('refresh token missing');
  }

  return request<TokenResponse>('/auth/refresh', {
    method: 'POST',
    body: { refreshToken: refreshTokenValue },
    retryOnAuthError: false,
  });
}

export async function login(payload: LoginPayload) {
  return request<TokenResponse>('/auth/login', {
    method: 'POST',
    body: payload,
  });
}

export async function logout() {
  const token = authToken();
  if (!token) {
    clearAuth();
    return;
  }

  try {
    await request('/auth/logout', { method: 'POST', token, retryOnAuthError: false });
  } finally {
    clearAuth();
  }
}

export async function fetchMe() {
  return request<UserProfile>('/users/me', { token: requireToken() });
}

export async function updateMe(payload: Partial<Pick<UserProfile, 'name' | 'phone'>> & { password?: string }) {
  return request<UserProfile>('/users/me', {
    method: 'PUT',
    token: requireToken(),
    body: payload,
  });
}

export async function fetchAddresses() {
  return request<Address[]>('/addresses', { token: requireToken() });
}

export async function createAddress(payload: CreateAddressPayload) {
  return request<Address>('/addresses', {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function updateAddress(id: number, payload: Partial<CreateAddressPayload>) {
  return request<Address>(`/addresses/${id}`, {
    method: 'PATCH',
    token: requireToken(),
    body: payload,
  });
}

export async function removeAddress(id: number) {
  return request<{ deleted: boolean }>(`/addresses/${id}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function fetchCart() {
  const token = authToken();

  if (token) {
    return request<CartItem[]>('/cart', { token });
  }

  const guestCartKey = getOrCreateGuestCartKey();
  return request<CartItem[]>('/cart/guest', {
    headers: { 'x-cart-key': guestCartKey },
  });
}

export async function addToCart(payload: AddCartItemPayload) {
  const token = authToken();

  if (token) {
    return request('/cart', {
      method: 'POST',
      token,
      body: payload,
    });
  }

  const guestCartKey = getOrCreateGuestCartKey();
  return request('/cart/guest', {
    method: 'POST',
    headers: { 'x-cart-key': guestCartKey },
    body: payload,
  });
}

export async function updateCartQuantity(itemId: number | string, payload: UpdateCartQuantityPayload) {
  const token = authToken();

  if (token) {
    return request(`/cart/${itemId}`, {
      method: 'PATCH',
      token,
      body: payload,
    });
  }

  const guestCartKey = getOrCreateGuestCartKey();
  return request(`/cart/guest/${itemId}`, {
    method: 'PATCH',
    headers: { 'x-cart-key': guestCartKey },
    body: payload,
  });
}

export async function removeCartItem(itemId: number | string) {
  const token = authToken();

  if (token) {
    return request(`/cart/${itemId}`, {
      method: 'DELETE',
      token,
    });
  }

  const guestCartKey = getOrCreateGuestCartKey();
  return request(`/cart/guest/${itemId}`, {
    method: 'DELETE',
    headers: { 'x-cart-key': guestCartKey },
  });
}

export async function clearCart() {
  const token = authToken();

  if (token) {
    return request('/cart', {
      method: 'DELETE',
      token,
    });
  }

  const guestCartKey = getOrCreateGuestCartKey();
  return request('/cart/guest', {
    method: 'DELETE',
    headers: { 'x-cart-key': guestCartKey },
  });
}

export async function createOrder(payload: CreateOrderPayload) {
  return request<OrderDetail>('/orders', {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function fetchMyOrders(page = 1, limit = 10) {
  return request<OrderSummary[]>('/orders', {
    token: requireToken(),
    query: { page, limit },
  });
}

export async function fetchOrderById(id: number) {
  return request<OrderDetail>(`/orders/${id}`, {
    token: requireToken(),
  });
}

export async function cancelOrder(id: number) {
  return request<OrderDetail>(`/orders/${id}/cancel`, {
    method: 'POST',
    token: requireToken(),
  });
}

export async function fetchAdminOrders(page = 1, limit = 20) {
  return request<OrderSummary[]>('/admin/orders', {
    token: requireToken(),
    query: { page, limit },
  });
}

export async function updateAdminOrderStatus(id: number, status: string) {
  return request<OrderDetail>(`/admin/orders/${id}/status`, {
    method: 'PATCH',
    token: requireToken(),
    body: { status },
  });
}

export async function requestPayment(payload: { orderId: number; method: string; amount: number }) {
  return request<{
    id: number;
    orderId: number;
    orderStatus: string;
    method: string;
    amount: number;
    status: string;
    paidAt: string | null;
    refundedAt: string | null;
    createdAt?: string;
    updatedAt?: string;
  }>('/payments', {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function fetchPayment(id: number) {
  return request<{
    id: number;
    orderId: number;
    orderStatus: string;
    method: string;
    amount: number;
    status: string;
    paidAt: string | null;
    refundedAt: string | null;
    createdAt?: string;
    updatedAt?: string;
  }>(`/payments/${id}`, {
    token: requireToken(),
  });
}

export async function refundPayment(id: number, payload?: { reason?: string }) {
  return request<{
    id: number;
    orderId: number;
    orderStatus: string;
    method: string;
    amount: number;
    status: string;
    paidAt: string | null;
    refundedAt: string | null;
    createdAt?: string;
    updatedAt?: string;
  }>(`/payments/${id}/refund`, {
    method: 'POST',
    token: requireToken(),
    body: payload || {},
  });
}

export async function adminRefundPayment(id: number, payload?: { reason?: string }) {
  return request<{
    id: number;
    orderId: number;
    orderStatus: string;
    method: string;
    amount: number;
    status: string;
    paidAt: string | null;
    refundedAt: string | null;
    createdAt?: string;
    updatedAt?: string;
  }>(`/admin/payments/${id}/refund`, {
    method: 'POST',
    token: requireToken(),
    body: payload || {},
  });
}

export async function fetchWishlist(page = 1, limit = 20) {
  return request<WishlistItem[]>('/wishlist', {
    token: requireToken(),
    query: { page, limit },
  });
}

export async function toggleWishlist(productId: number) {
  return request<{ wishlisted: boolean }>(`/wishlist/${productId}`, {
    method: 'POST',
    token: requireToken(),
  });
}

export async function removeWishlist(productId: number) {
  return request<{ message: string }>(`/wishlist/${productId}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function fetchPointBalance() {
  return request<{
    balance: number;
    expiringSoon?: number;
    expiringDate?: string;
  }>('/points/balance', {
    token: requireToken(),
  });
}

export async function fetchPointTransactions(query?: {
  page?: number;
  limit?: number;
  type?: string;
}) {
  return request<Array<{
    id: number;
    type: string;
    amount: number;
    balanceAfter?: number;
    balance?: number;
    description: string | null;
    createdAt: string;
  }>>('/points/transactions', {
    token: requireToken(),
    query: query as Record<string, unknown> | undefined,
  });
}

export async function adminGrantPoint(payload: {
  userId: number;
  amount: number;
  description?: string;
}) {
  return request<{
    userId: number;
    grantedAmount: number;
    balance: number;
    transactionId: number;
  }>('/admin/points/grant', {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function fetchBoards() {
  return request<BoardItem[]>('/boards');
}

export async function fetchBoardPosts(
  boardId: number,
  query?: {
    page?: number;
    limit?: number;
    search?: string;
    sort?: 'newest' | 'popular' | 'most_commented';
  },
) {
  return request<PostSummaryItem[]>(`/boards/${boardId}/posts`, {
    query: query as Record<string, unknown> | undefined,
  });
}

export async function fetchPost(id: number) {
  return request<PostDetailItem>(`/posts/${id}`);
}

export async function createBoardPost(
  boardId: number,
  payload: {
    title: string;
    content: string;
  },
) {
  return request<PostDetailItem>(`/boards/${boardId}/posts`, {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function updatePost(
  id: number,
  payload: {
    title?: string;
    content?: string;
  },
) {
  return request<PostDetailItem>(`/posts/${id}`, {
    method: 'PATCH',
    token: requireToken(),
    body: payload,
  });
}

export async function removePost(id: number) {
  return request<{ message: string }>(`/posts/${id}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function togglePostLike(id: number) {
  return request<{ liked: boolean; likeCount: number }>(`/posts/${id}/like`, {
    method: 'POST',
    token: requireToken(),
  });
}

export async function fetchPostComments(postId: number) {
  return request<PostCommentItem[]>(`/posts/${postId}/comments`);
}

export async function createPostComment(postId: number, payload: { content: string }) {
  return request<PostCommentItem>(`/posts/${postId}/comments`, {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function removeComment(id: number) {
  return request<{ message: string }>(`/comments/${id}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function fetchProductInquiries(
  productId: number,
  query?: { page?: number; limit?: number },
) {
  return request<InquiryItem[]>(`/products/${productId}/inquiries`, {
    query: query as Record<string, unknown> | undefined,
  });
}

export async function createProductInquiry(
  productId: number,
  payload: {
    title: string;
    content: string;
    isSecret?: boolean;
  },
) {
  return request<InquiryItem>(`/products/${productId}/inquiries`, {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function answerInquiry(
  inquiryId: number,
  payload: {
    content: string;
  },
) {
  return request<InquiryItem>(`/inquiries/${inquiryId}/answer`, {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function fetchMyInquiries(query?: { page?: number; limit?: number }) {
  return request<InquiryItem[]>('/inquiries/me', {
    token: requireToken(),
    query: query as Record<string, unknown> | undefined,
  });
}

export async function removeInquiry(inquiryId: number) {
  return request<{ message: string }>(`/inquiries/${inquiryId}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function createSupportTicket(payload: {
  category: string;
  title: string;
  content: string;
  attachmentUrl?: string;
}) {
  return request<SupportTicketItem>('/support/tickets', {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function fetchMySupportTickets(query?: {
  page?: number;
  limit?: number;
  status?: 'OPEN' | 'ANSWERED';
}) {
  return request<SupportTicketItem[]>('/support/tickets/me', {
    token: requireToken(),
    query: query as Record<string, unknown> | undefined,
  });
}

export async function fetchMySupportTicket(id: number) {
  return request<SupportTicketItem>(`/support/tickets/me/${id}`, {
    token: requireToken(),
  });
}

export async function answerSupportTicketAdmin(ticketId: number, payload: { content: string }) {
  return request<SupportTicketItem>(`/admin/support/tickets/${ticketId}/answer`, {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function fetchAdminSupportTickets(query?: {
  page?: number;
  limit?: number;
  status?: 'OPEN' | 'ANSWERED';
}) {
  return request<SupportTicketItem[]>('/admin/support/tickets', {
    token: requireToken(),
    query: query as Record<string, unknown> | undefined,
  });
}

export async function fetchFaqs(query?: {
  page?: number;
  limit?: number;
  category?: string;
  keyword?: string;
}) {
  return request<FaqItem[]>('/faq', {
    query: query as Record<string, unknown> | undefined,
  });
}

export async function createFaqAdmin(payload: {
  category: string;
  question: string;
  answer: string;
  isActive?: boolean;
}) {
  return request<FaqItem>('/admin/faq', {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function updateFaqAdmin(id: number, payload: {
  category?: string;
  question?: string;
  answer?: string;
  isActive?: boolean;
}) {
  return request<FaqItem>(`/admin/faq/${id}`, {
    method: 'PATCH',
    token: requireToken(),
    body: payload,
  });
}

export async function removeFaqAdmin(id: number) {
  return request<{ message: string }>(`/admin/faq/${id}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function fetchNotices(query?: { page?: number; limit?: number }) {
  return request<NoticeItem[]>('/notices', {
    query: query as Record<string, unknown> | undefined,
  });
}

export async function createNoticeAdmin(payload: {
  title: string;
  content: string;
  isPublished?: boolean;
}) {
  return request<NoticeItem>('/admin/notices', {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function updateNoticeAdmin(id: number, payload: {
  title?: string;
  content?: string;
  isPublished?: boolean;
}) {
  return request<NoticeItem>(`/admin/notices/${id}`, {
    method: 'PATCH',
    token: requireToken(),
    body: payload,
  });
}

export async function removeNoticeAdmin(id: number) {
  return request<{ message: string }>(`/admin/notices/${id}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function fetchActivitySummary() {
  return request<ActivitySummary>('/activities', {
    token: requireToken(),
  });
}

export async function fetchRecentProducts(query?: { page?: number; limit?: number }) {
  return request('/activities/recent-products', {
    token: requireToken(),
    query: query as Record<string, unknown> | undefined,
  });
}

export async function trackRecentProduct(productId: number) {
  return request<{ queued: boolean; event: string; userId: number; productId: number }>(`/activities/recent-products/${productId}`, {
    method: 'POST',
    token: requireToken(),
  });
}

export async function fetchSearchHistory(query?: { page?: number; limit?: number }) {
  return request<ActivitySearchItem[]>('/activities/searches', {
    token: requireToken(),
    query: query as Record<string, unknown> | undefined,
  });
}

export async function addSearchHistory(keyword: string) {
  return request<{ queued: boolean; event: string; userId: number; keyword: string }>('/activities/searches', {
    method: 'POST',
    token: requireToken(),
    body: { keyword },
  });
}

export async function removeSearchHistory(id: number) {
  return request<{ deleted: boolean; id: number }>(`/activities/searches/${id}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function clearSearchHistory() {
  return request<{ deleted: boolean; affected: number }>('/activities/searches', {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function fetchChatRooms(query?: { page?: number; limit?: number }) {
  return request<ChatRoomItem[]>('/chat/rooms', {
    token: requireToken(),
    query: query as Record<string, unknown> | undefined,
  });
}

export async function createChatRoom(payload: { name: string; isPrivate?: boolean }) {
  return request<ChatRoomItem>('/chat/rooms', {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function joinChatRoom(roomId: number) {
  return request<ChatRoomItem>(`/chat/rooms/${roomId}/join`, {
    method: 'POST',
    token: requireToken(),
  });
}

export async function fetchChatMessages(roomId: number, query?: { page?: number; limit?: number }) {
  return request<ChatMessageItem[]>(`/chat/rooms/${roomId}/messages`, {
    token: requireToken(),
    query: query as Record<string, unknown> | undefined,
  });
}

export async function sendChatMessage(roomId: number, payload: { message: string }) {
  return request<ChatMessageItem>(`/chat/rooms/${roomId}/messages`, {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function fetchReviews(productId: number, page = 1, limit = 10) {
  return request<ReviewItem[]>(`/products/${productId}/reviews`, {
    query: { page, limit },
  });
}

export async function createReview(productId: number, payload: CreateReviewPayload) {
  return request(`/products/${productId}/reviews`, {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function updateReview(id: number, payload: { rating?: number; content?: string }) {
  return request<ReviewItem>(`/reviews/${id}`, {
    method: 'PATCH',
    token: requireToken(),
    body: payload,
  });
}

export async function removeReview(id: number) {
  return request<{ message: string }>(`/reviews/${id}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function fetchPublicProfile(userId: number) {
  return request<{ id: number; nickname: string; bio: string | null; profileImageUrl: string | null; createdAt: string }>(`/users/profile/${userId}`);
}

export async function updateMyProfile(payload: { nickname?: string; bio?: string }) {
  return request<{ id: number; nickname: string; bio: string | null; profileImageUrl: string | null }>('/users/me/profile', {
    method: 'PATCH',
    token: requireToken(),
    body: payload,
  });
}

export async function deleteMyProfileImage() {
  return request<{ id: number; profileImageUrl: string | null }>('/users/me/profile-image', {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function deleteMe() {
  return request<{ message: string }>('/users/me', {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function fetchUsersAdmin(query: {
  page?: number;
  limit?: number;
  search?: string;
  status?: 'ACTIVE' | 'INACTIVE' | 'BLOCKED';
  role?: 'USER' | 'SELLER' | 'ADMIN';
} = {}) {
  return request<UserProfile[]>('/users', {
    token: requireToken(),
    query: query as Record<string, unknown>,
  });
}

export async function updateUserStatusAdmin(userId: number, status: 'ACTIVE' | 'INACTIVE' | 'BLOCKED') {
  return request<UserProfile>(`/users/${userId}/status`, {
    method: 'PATCH',
    token: requireToken(),
    body: { status },
  });
}

export async function fetchCategoryById(id: number) {
  return request<{ id: number; name: string; parentId: number | null; sortOrder: number; children: Array<{ id: number; name: string; sortOrder: number }>; createdAt: string }>(`/categories/${id}`);
}

export async function createCategoryAdmin(payload: { name: string; parentId?: number }) {
  return request<{ id: number; name: string; parentId: number | null; sortOrder: number; createdAt: string }>('/categories', {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function updateCategoryAdmin(id: number, payload: { name?: string; sortOrder?: number }) {
  return request<{ id: number; name: string; parentId: number | null; sortOrder: number; createdAt: string }>(`/categories/${id}`, {
    method: 'PATCH',
    token: requireToken(),
    body: payload,
  });
}

export async function removeCategoryAdmin(id: number) {
  return request<{ message: string }>(`/categories/${id}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function createProductAdmin(payload: {
  name: string;
  description: string;
  price: number;
  discountPrice?: number;
  stock: number;
  categoryId: number;
  status?: 'ON_SALE' | 'SOLD_OUT' | 'HIDDEN';
  thumbnailUrl?: string;
  options?: Array<{ name: string; values: string[] }>;
  images?: Array<{ url: string; isMain?: boolean; sortOrder?: number }>;
}) {
  return request<ProductDetail>('/products', {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function updateProductAdmin(
  id: number,
  payload: {
    name?: string;
    description?: string;
    price?: number;
    discountPrice?: number;
    stock?: number;
    categoryId?: number;
    status?: 'ON_SALE' | 'SOLD_OUT' | 'HIDDEN';
    thumbnailUrl?: string;
  },
) {
  return request<ProductDetail>(`/products/${id}`, {
    method: 'PATCH',
    token: requireToken(),
    body: payload,
  });
}

export async function removeProductAdmin(id: number) {
  return request<{ message: string }>(`/products/${id}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function addProductOptionAdmin(productId: number, payload: { name: string; values: string[] }) {
  return request<{ id: number; name: string; values: string[] }>(`/products/${productId}/options`, {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function updateProductOptionAdmin(productId: number, optionId: number, payload: { name: string; values: string[] }) {
  return request<{ id: number; name: string; values: string[] }>(`/products/${productId}/options/${optionId}`, {
    method: 'PATCH',
    token: requireToken(),
    body: payload,
  });
}

export async function removeProductOptionAdmin(productId: number, optionId: number) {
  return request<{ message: string }>(`/products/${productId}/options/${optionId}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function addProductImageAdmin(productId: number, payload: { url: string; isMain?: boolean; sortOrder?: number }) {
  return request<{ id: number; url: string; isMain: boolean; sortOrder: number }>(`/products/${productId}/images`, {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function removeProductImageAdmin(productId: number, imageId: number) {
  return request<{ message: string }>(`/products/${productId}/images/${imageId}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function fetchSpecDefinitions(categoryId?: number) {
  return request<Array<{
    id: number;
    categoryId: number;
    name: string;
    type: string;
    options: string[] | null;
    unit: string | null;
    groupName: string | null;
    parentDefinitionId: number | null;
    higherIsBetter: boolean;
    isComparable: boolean;
    dataType: string;
    sortOrder: number;
  }>>('/specs/definitions', {
    query: categoryId ? { categoryId } : undefined,
  });
}

export async function fetchResolvedSpecDefinitions(categoryId: number) {
  return request<Array<{
    id: number;
    categoryId: number;
    name: string;
    type: string;
    options: string[] | null;
    unit: string | null;
    groupName: string | null;
    parentDefinitionId: number | null;
    higherIsBetter: boolean;
    isComparable: boolean;
    dataType: string;
    sortOrder: number;
  }>>(`/specs/definitions/resolved/${categoryId}`);
}

export async function createSpecDefinitionAdmin(payload: {
  categoryId: number;
  name: string;
  type: string;
  options?: string[];
  unit?: string;
  groupName?: string;
  parentDefinitionId?: number;
  higherIsBetter?: boolean;
  isComparable?: boolean;
  dataType?: string;
  sortOrder?: number;
}) {
  return request('/specs/definitions', {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function updateSpecDefinitionAdmin(
  id: number,
  payload: {
    name?: string;
    type?: string;
    options?: string[];
    unit?: string;
    groupName?: string;
    parentDefinitionId?: number;
    higherIsBetter?: boolean;
    isComparable?: boolean;
    dataType?: string;
    sortOrder?: number;
  },
) {
  return request(`/specs/definitions/${id}`, {
    method: 'PATCH',
    token: requireToken(),
    body: payload,
  });
}

export async function removeSpecDefinitionAdmin(id: number) {
  return request<{ message: string }>(`/specs/definitions/${id}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function fetchProductSpecs(productId: number) {
  return request<Array<{ specDefinitionId: number; name: string; value: string; numericValue?: number }>>(`/products/${productId}/specs`);
}

export async function fetchProductGroupedSpecs(productId: number) {
  return request<Array<{ groupName: string; specs: Array<{ specDefinitionId: number; name: string; value: string; numericValue?: number }> }>>(`/products/${productId}/specs/grouped`);
}

export async function setProductSpecsAdmin(productId: number, payload: { specs: Array<{ specDefinitionId: number; value: string; numericValue?: number }> }) {
  return request(`/products/${productId}/specs`, {
    method: 'PUT',
    token: requireToken(),
    body: payload,
  });
}

export async function compareSpecs(payload: { productIds: number[] }) {
  return request('/specs/compare', {
    method: 'POST',
    body: payload,
  });
}

export async function compareSpecsNumeric(payload: { productIds: number[] }) {
  return request('/specs/compare/numeric', {
    method: 'POST',
    body: payload,
  });
}

export async function compareSpecsScored(payload: { productIds: number[]; weights?: Record<string, number> }) {
  return request('/specs/compare/scored', {
    method: 'POST',
    body: payload,
  });
}

export async function scoreByCategory(payload: { categoryId: number; productIds: number[] }) {
  return request('/specs/score', {
    method: 'POST',
    body: payload,
  });
}

export async function setSpecScoresAdmin(specDefId: number, payload: { scores: Array<{ value: string; score: number; benchmarkSource?: string }> }) {
  return request(`/specs/scores/${specDefId}`, {
    method: 'PUT',
    token: requireToken(),
    body: payload,
  });
}

export async function fetchSimilarProductsBySpec(productId: number, limit = 5) {
  return request<Array<{ id: number; name: string; similarityScore: number }>>(`/products/${productId}/similar-spec-products`, {
    query: { limit },
  });
}

export async function fetchSellers(page = 1, limit = 20) {
  return request<Array<{
    id: number;
    name: string;
    url: string;
    logoUrl: string | null;
    description: string | null;
    trustScore?: number;
    createdAt?: string;
  }>>('/sellers', {
    query: { page, limit },
  });
}

export async function fetchSellerById(id: number) {
  return request<{
    id: number;
    name: string;
    url: string;
    logoUrl: string | null;
    description: string | null;
    trustScore?: number;
    createdAt?: string;
  }>(`/sellers/${id}`);
}

export async function createSellerAdmin(payload: {
  name: string;
  url: string;
  logoUrl?: string;
  description?: string;
}) {
  return request('/sellers', {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function updateSellerAdmin(id: number, payload: {
  name?: string;
  url?: string;
  logoUrl?: string;
  description?: string;
}) {
  return request(`/sellers/${id}`, {
    method: 'PATCH',
    token: requireToken(),
    body: payload,
  });
}

export async function removeSellerAdmin(id: number) {
  return request<{ message: string }>(`/sellers/${id}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function fetchProductPrices(productId: number) {
  return request<{
    lowestPrice: number;
    averagePrice: number;
    highestPrice: number;
    entries: Array<{
      id: number;
      seller: { id: number; name: string; logoUrl: string | null; trustScore?: number };
      price: number;
      shippingCost: number;
      shippingInfo: string | null;
      productUrl: string;
      updatedAt: string;
    }>;
  }>(`/products/${productId}/prices`);
}

export async function createProductPrice(payload: {
  productId: number;
  sellerId: number;
  price: number;
  shippingCost?: number;
  shippingInfo?: string;
  productUrl: string;
}) {
  return request(`/products/${payload.productId}/prices`, {
    method: 'POST',
    token: requireToken(),
    body: {
      sellerId: payload.sellerId,
      price: payload.price,
      ...(payload.shippingCost !== undefined ? { shippingCost: payload.shippingCost } : {}),
      ...(payload.shippingInfo ? { shippingInfo: payload.shippingInfo } : {}),
      productUrl: payload.productUrl,
    },
  });
}

export async function updatePriceEntry(priceId: number, payload: {
  price?: number;
  shippingCost?: number;
  shippingInfo?: string;
  productUrl?: string;
}) {
  return request(`/prices/${priceId}`, {
    method: 'PATCH',
    token: requireToken(),
    body: payload,
  });
}

export async function removePriceEntry(priceId: number) {
  return request<{ message: string }>(`/prices/${priceId}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}

export async function fetchPriceHistory(productId: number, query?: {
  period?: '1w' | '1m' | '3m' | '6m' | '1y';
  type?: 'daily' | 'weekly' | 'monthly';
}) {
  return request<{
    productId: number;
    productName: string;
    allTimeLowest: number;
    allTimeHighest: number;
    history: Array<{ date: string; lowestPrice: number; averagePrice: number }>;
  }>(`/products/${productId}/price-history`, {
    query: query as Record<string, unknown> | undefined,
  });
}

export async function fetchPriceAlerts() {
  return request<Array<{
    id: number;
    productId: number;
    productName: string;
    targetPrice: number;
    currentLowestPrice: number;
    isTriggered: boolean;
    createdAt: string;
  }>>('/price-alerts', {
    token: requireToken(),
  });
}

export async function createPriceAlert(payload: { productId: number; targetPrice: number }) {
  return request('/price-alerts', {
    method: 'POST',
    token: requireToken(),
    body: payload,
  });
}

export async function removePriceAlert(id: number) {
  return request<{ message: string }>(`/price-alerts/${id}`, {
    method: 'DELETE',
    token: requireToken(),
  });
}
