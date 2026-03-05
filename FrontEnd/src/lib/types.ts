export interface ApiEnvelope<T> {
  success: boolean;
  data: T;
  meta?: {
    page?: number;
    limit?: number;
    totalCount?: number;
    totalPages?: number;
    [key: string]: unknown;
  };
  requestId?: string;
  timestamp?: string;
}

export interface ApiErrorEnvelope {
  success: false;
  statusCode?: number;
  message?: string | string[];
  errorCode?: string;
  timestamp?: string;
  requestId?: string;
}

export interface Category {
  id: number;
  name: string;
  sortOrder?: number;
  children?: Category[];
}

export interface ProductSummary {
  id: number;
  name: string;
  lowestPrice: number | null;
  sellerCount: number;
  thumbnailUrl: string | null;
  reviewCount: number;
  averageRating: number;
  createdAt: string;
}

export interface ProductDetail {
  id: number;
  name: string;
  description: string;
  price: number;
  discountPrice: number | null;
  lowestPrice: number | null;
  stock: number;
  status: string;
  category: { id: number; name: string } | null;
  options: Array<{ id: number; name: string; values: string[] }>;
  images: Array<{ id: number; url: string; isMain: boolean; sortOrder: number }>;
  reviewCount: number;
  averageRating: number;
  viewCount: number;
  createdAt: string;
}

export interface ProductQuery {
  page?: number;
  limit?: number;
  categoryId?: number;
  search?: string;
  minPrice?: number;
  maxPrice?: number;
  sort?: 'newest' | 'popularity' | 'price_asc' | 'price_desc' | 'rating_desc' | 'rating_asc';
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface SignupPayload {
  email: string;
  password: string;
  name: string;
  phone: string;
}

export interface VerifyEmailPayload {
  email: string;
  code: string;
}

export interface RequestPasswordResetPayload {
  email: string;
  phone: string;
}

export interface VerifyResetCodePayload {
  email: string;
  code: string;
}

export interface ResetPasswordPayload {
  resetToken: string;
  newPassword: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface UserProfile {
  id: number;
  email: string;
  name: string;
  phone: string;
  role: string;
  status: string;
  emailVerified: boolean;
  nickname: string;
  bio: string | null;
  profileImageUrl: string | null;
  point: number;
  createdAt: string;
}

export interface Address {
  id: number;
  recipientName: string;
  phone: string;
  zipCode: string;
  address: string;
  addressDetail: string | null;
  isDefault: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateAddressPayload {
  recipientName: string;
  phone: string;
  zipCode: string;
  address: string;
  addressDetail?: string;
  isDefault?: boolean;
}

export interface CartItem {
  id: number | string;
  product: {
    id: number | null;
    name: string;
    thumbnailUrl: string | null;
    price: number;
    lowestPrice: number | null;
  } | null;
  seller: {
    id: number | null;
    name: string;
    logoUrl: string | null;
  } | null;
  selectedOptions: string | null;
  quantity: number;
  createdAt: string;
}

export interface AddCartItemPayload {
  productId: number;
  sellerId: number;
  quantity: number;
  selectedOptions?: string;
}

export interface UpdateCartQuantityPayload {
  quantity: number;
}

export interface CreateOrderItemPayload {
  productId: number;
  sellerId: number;
  quantity: number;
  selectedOptions?: string;
}

export interface CreateOrderPayload {
  addressId: number;
  items: CreateOrderItemPayload[];
  fromCart?: boolean;
  cartItemIds?: number[];
  usePoint?: number;
  memo?: string;
}

export interface OrderSummary {
  id: number;
  orderNumber: string;
  status: string;
  totalAmount: number;
  finalAmount: number;
  itemCount: number;
  createdAt: string;
}

export interface OrderDetail {
  id: number;
  orderNumber: string;
  status: string;
  items: Array<{
    id: number;
    productId: number;
    productName: string;
    sellerName: string;
    selectedOptions: string | null;
    quantity: number;
    unitPrice: number;
    totalPrice: number;
  }>;
  totalAmount: number;
  pointUsed: number;
  finalAmount: number;
  shippingAddress: {
    recipientName: string;
    recipientPhone: string;
    zipCode: string;
    address: string;
    addressDetail: string | null;
  };
  payments: Array<{
    id: number;
    orderId: number;
    orderStatus: string;
    method: string;
    amount: number;
    status: string;
    paidAt: string | null;
    refundedAt: string | null;
  }>;
  memo: string | null;
  createdAt: string;
}

export interface WishlistItem {
  id: number;
  product: {
    id: number;
    name: string;
    thumbnailUrl: string | null;
    lowestPrice: number | null;
    averageRating: number;
    reviewCount: number;
  };
  createdAt: string;
}

export interface ReviewItem {
  id: number;
  productId: number;
  orderId: number;
  rating: number;
  content: string;
  user: {
    id: number;
    nickname: string;
  };
  createdAt: string;
  updatedAt: string;
}

export interface CreateReviewPayload {
  orderId: number;
  rating: number;
  content: string;
}

export interface SearchResultItem {
  id: number;
  name: string;
  lowestPrice: number | null;
  averageRating: number;
  sellerCount: number;
  thumbnailUrl: string | null;
}

export interface PopularKeywordItem {
  rank: number;
  keyword: string;
  count: number;
}

export interface RankingProductItem {
  rank: number;
  productId: number;
  name: string;
  popularityScore: number;
  lowestPrice: number | null;
}

export interface RecommendationItem {
  rank: number;
  productId: number;
  name: string;
  thumbnailUrl: string | null;
  lowestPrice: number | null;
  averageRating: number;
  reviewCount: number;
  salesCount: number;
  popularityScore: number;
  categoryId: number | null;
}

export interface RecommendationResult {
  source: string;
  items: RecommendationItem[];
}

export interface PricePredictionResult {
  productId: number;
  productName: string;
  basis: {
    dataPoints: number;
    horizonDays: number;
    lookbackDays: number;
    fromDate?: string;
    toDate?: string;
  };
  trend: 'UP' | 'DOWN' | 'FLAT' | 'UNKNOWN';
  currentPrice: number;
  predictedPrice: number;
  expectedChange: number;
  confidence: number;
  message: string;
}

export interface FraudAlertItem {
  id: number;
  productId: number;
  priceEntryId: number;
  sellerId: number;
  reason: string;
  rawPrice: number;
  effectivePrice: number;
  baselineAverage: number;
  severity: 'LOW' | 'MEDIUM' | 'HIGH';
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  reviewedBy: number | null;
  reviewedAt: string | null;
  createdAt: string;
}

export interface FraudRealPriceResult {
  productId: number;
  sellerId: number;
  productPrice: number;
  shippingFee: number;
  shippingType: string;
  totalPrice: number;
}

export interface TrustCurrentScore {
  sellerId: number;
  sellerName: string;
  trustScore: number;
  trustGrade: string;
  updatedAt: string;
}

export interface TrustHistoryItem {
  id: number;
  sellerId: number;
  metrics: {
    deliveryAccuracy: number;
    priceAccuracy: number;
    customerRating: number;
    responseSpeed: number;
    returnRate: number;
  };
  trustScore: number;
  trustGrade: string;
  createdAt: string;
}

export interface TranslationItem {
  id: number;
  locale: string;
  namespace: string;
  key: string;
  value: string;
  updatedAt: string;
}

export interface ExchangeRateItem {
  id: number;
  baseCurrency: string;
  targetCurrency: string;
  rate: number;
  updatedAt: string;
}

export interface ConvertedAmountResult {
  originalAmount: number;
  originalCurrency: string;
  convertedAmount: number;
  targetCurrency: string;
  rate: number;
}

export interface ImageVariantItem {
  id: number;
  type: 'THUMBNAIL' | 'MEDIUM' | 'LARGE';
  url: string;
  width: number;
  height: number;
  format: string;
  size: number;
  createdAt: string;
}

export interface ImageUploadResult {
  id: number;
  originalUrl: string;
  variants: ImageVariantItem[];
  processingStatus: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
}

export interface BadgeItem {
  id: number;
  name: string;
  description: string;
  iconUrl: string;
  type: 'AUTO' | 'MANUAL';
  condition: Record<string, unknown> | null;
  rarity: 'COMMON' | 'UNCOMMON' | 'RARE' | 'EPIC' | 'LEGENDARY';
  holderCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface UserBadgeItem {
  id: number;
  userId: number;
  badgeId: number;
  grantedByAdminId: number;
  reason: string | null;
  grantedAt: string;
  badge: {
    id: number;
    name: string;
    description: string;
    iconUrl: string;
    type: 'AUTO' | 'MANUAL';
    rarity: 'COMMON' | 'UNCOMMON' | 'RARE' | 'EPIC' | 'LEGENDARY';
  } | null;
}

export interface PcBuildSummaryItem {
  id: number;
  name: string;
  purpose: 'GAMING' | 'OFFICE' | 'DESIGN' | 'DEVELOPMENT' | 'STREAMING';
  budget: number | null;
  totalPrice: number;
  shareCode: string | null;
  viewCount: number;
  updatedAt: string;
}

export interface PcBuildDetailItem {
  id: number;
  userId: number;
  name: string;
  description: string | null;
  purpose: 'GAMING' | 'OFFICE' | 'DESIGN' | 'DEVELOPMENT' | 'STREAMING';
  budget: number | null;
  totalPrice: number;
  shareCode: string | null;
  viewCount: number;
  parts: Array<{
    id: number;
    partType: 'CPU' | 'MOTHERBOARD' | 'RAM' | 'GPU' | 'SSD' | 'HDD' | 'PSU' | 'CASE' | 'COOLER' | 'MONITOR';
    quantity: number;
    product: { id: number; name: string; lowestPrice: number | null } | null;
    seller: { id: number; name: string; price: number } | null;
    unitPrice: number;
    totalPrice: number;
  }>;
  compatibility: {
    status: string;
    issues: unknown[];
    warnings: Array<{ type: string; message: string; severity: string }>;
    missingParts: string[];
    powerEstimate?: {
      totalWattage: number;
      psuWattage: number;
      headroom: number;
      sufficient: boolean;
    };
    socketCompatible?: boolean;
    ramCompatible?: boolean;
    formFactorCompatible?: boolean;
  };
  createdAt: string;
  updatedAt: string;
}

export interface PcCompatibilityRuleItem {
  id: number;
  partType: string;
  targetPartType: string | null;
  title: string;
  description: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH';
  enabled: boolean;
  metadata: Record<string, unknown> | null;
  createdAt: string;
  updatedAt: string;
}

export interface FriendListItem {
  friendshipId: number;
  userId: number;
  nickname: string | null;
  profileImageUrl: string | null;
  status?: string;
  since?: string | null;
  requestedAt?: string;
}

export interface FriendFeedItem {
  id: number;
  userId: number;
  nickname: string | null;
  profileImageUrl: string | null;
  type: string;
  message: string;
  metadata: Record<string, unknown> | null;
  createdAt: string;
}

export interface ShortformItem {
  id: number;
  userId: number;
  nickname: string | null;
  profileImageUrl: string | null;
  title: string;
  videoUrl: string;
  thumbnailUrl: string | null;
  durationSec: number;
  viewCount: number;
  likeCount: number;
  commentCount: number;
  transcodeStatus: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  transcodedVideoUrl: string | null;
  transcodeError: string | null;
  transcodedAt: string | null;
  productIds: number[];
  liked: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ShortformCommentItem {
  id: number;
  userId: number;
  nickname: string | null;
  profileImageUrl: string | null;
  content: string;
  createdAt: string;
}

export interface MediaAssetItem {
  id: number;
  ownerType: 'PRODUCT' | 'COMMUNITY' | 'SUPPORT' | 'SELLER' | 'SHORTFORM' | 'USER';
  ownerId: number;
  fileKey: string;
  fileUrl: string;
  type: 'IMAGE' | 'VIDEO' | 'AUDIO' | 'DOCUMENT';
  mime: string;
  size: number;
  duration: number | null;
  resolution: string | null;
  createdAt: string;
}

export interface NewsCategoryItem {
  id: number;
  name: string;
  slug: string;
  createdAt: string;
  updatedAt: string;
}

export interface NewsSummaryItem {
  id: number;
  title: string;
  thumbnailUrl: string | null;
  category: {
    id: number;
    name: string;
    slug: string;
  } | null;
  viewCount: number;
  createdAt: string;
}

export interface NewsDetailItem {
  id: number;
  title: string;
  content: string;
  thumbnailUrl: string | null;
  category: {
    id: number;
    name: string;
    slug: string;
  } | null;
  viewCount: number;
  createdAt: string;
  updatedAt: string;
  relatedProducts: Array<{
    id: number;
    name: string;
    thumbnailUrl: string | null;
    lowestPrice: number | null;
  }>;
}

export interface ProductMappingItem {
  id: number;
  sourceName: string;
  sourceBrand: string | null;
  sourceSeller: string | null;
  sourceUrl: string | null;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  productId: number | null;
  confidence: number;
  reason: string | null;
  reviewedBy: number | null;
  reviewedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface LowestEverAnalyticsResult {
  isLowestEver: boolean;
  currentPrice: number;
  lowestPrice: number;
  lowestDate: string | null;
}

export interface UnitPriceAnalyticsResult {
  unitPrice: number;
  unit: string;
  quantity: number;
}

export interface UsedProductPriceResult {
  productId: number;
  averagePrice: number;
  minPrice: number;
  maxPrice: number;
  trend: 'UP' | 'DOWN' | 'STABLE';
}

export interface UsedCategoryPriceItem {
  productId: number;
  productName: string;
  averagePrice: number;
  minPrice: number;
  maxPrice: number;
  trend: 'UP' | 'DOWN' | 'STABLE';
}

export interface AutoModelItem {
  id: number;
  brand: string;
  name: string;
  type: 'SEDAN' | 'SUV' | 'EV';
  basePrice: number;
}

export interface AutoTrimItem {
  id: number;
  modelId: number;
  name: string;
  priceDelta: number;
  options: Array<{
    id: number;
    trimId: number;
    name: string;
    price: number;
  }>;
}

export interface AutoEstimateResult {
  modelId: number;
  trimId: number;
  selectedOptionIds: number[];
  basePrice: number;
  optionPrice: number;
  tax: number;
  totalPrice: number;
  monthlyPayment: number;
}

export interface AutoLeaseOfferItem {
  modelId: number;
  provider: string;
  months: number;
  monthlyFee: number;
  downPayment: number;
}

export interface AuctionSummaryItem {
  id: number;
  ownerId: number;
  title: string;
  categoryId: number;
  budget: number;
  status: 'OPEN' | 'CLOSED' | 'CANCELLED';
  createdAt: string;
}

export interface AuctionBidItem {
  id: number;
  auctionId: number;
  sellerId: number;
  price: number;
  description: string | null;
  deliveryDays: number;
  createdAt: string;
  updatedAt: string;
}

export interface AuctionDetailItem {
  id: number;
  ownerId: number;
  title: string;
  description: string;
  categoryId: number;
  specs: Record<string, unknown> | null;
  budget: number;
  status: 'OPEN' | 'CLOSED' | 'CANCELLED';
  selectedBidId: number | null;
  bids: AuctionBidItem[];
  createdAt: string;
  updatedAt: string;
}

export interface CompareListItem {
  productId: number;
  name: string;
  categoryId: number;
  price: number;
  averageRating: number;
  reviewCount: number;
  sellerCount: number;
  salesCount: number;
  thumbnailUrl: string | null;
}

export interface CompareDetailResult {
  compareList: CompareListItem[];
  differences: {
    price: boolean;
    categoryId: boolean;
    averageRating: boolean;
    reviewCount: boolean;
    sellerCount: boolean;
    salesCount: boolean;
  };
}

export interface AdminAllowedExtensionsResult {
  extensions: string[];
}

export interface AdminUploadLimitsResult {
  image: number;
  video: number;
  audio: number;
}

export interface AdminReviewPolicyResult {
  maxImageCount: number;
  pointAmount: number;
}

export interface HealthCheckResult {
  status: 'up' | 'degraded' | 'down';
  checks: {
    database: { status: 'up' | 'down' | 'unknown'; message?: string; latencyMs?: number };
    redis: { status: 'up' | 'down' | 'unknown'; message?: string; latencyMs?: number };
    elasticsearch: { status: 'up' | 'down' | 'unknown'; message?: string; latencyMs?: number };
  };
  checkedAt: string;
}

export interface ResilienceCircuitSnapshot {
  name: string;
  status: 'CLOSED' | 'OPEN' | 'HALF_OPEN';
  failureCount: number;
  successCount: number;
  nextAttemptAt: string | null;
  lastFailureReason: string | null;
  options: {
    failureThreshold: number;
    openTimeoutMs: number;
    halfOpenSuccessThreshold: number;
  };
}

export interface ResiliencePolicyItem {
  name: string;
  options: {
    failureThreshold: number;
    openTimeoutMs: number;
    halfOpenSuccessThreshold: number;
  };
  stats: {
    success: number;
    failure: number;
    lastTunedAt: number;
  };
}

export interface ErrorCodeItem {
  key: string;
  code: string;
  message: string;
}

export interface QueueJobCounts {
  waiting: number;
  active: number;
  delayed: number;
  completed: number;
  failed: number;
}

export interface QueueStatsItem {
  queueName: string;
  paused: boolean;
  counts: QueueJobCounts;
}

export interface QueueStatsResult {
  items: QueueStatsItem[];
  total: number;
}

export interface QueueFailedJobItem {
  id: string;
  name: string;
  data: Record<string, unknown>;
  timestamp: number;
  processedOn: number | null;
  finishedOn: number | null;
  attemptsMade: number;
  failedReason: string | null;
  stacktrace: string[];
}

export interface QueueFailedJobsResult {
  items: QueueFailedJobItem[];
  meta: {
    totalItems: number;
    itemCount: number;
    itemsPerPage: number;
    totalPages: number;
    currentPage: number;
  };
}

export interface QueueRetryFailedResult {
  queueName: string;
  requested: number;
  requeuedCount: number;
  jobIds: string[];
}

export interface QueueAutoRetryResult {
  perQueueLimit: number;
  maxTotal: number;
  retriedTotal: number;
  items: Array<{
    queueName: string;
    candidateCount: number;
    retriedCount: number;
    jobIds: string[];
  }>;
}

export interface OpsDashboardSummary {
  checkedAt: string;
  overallStatus: 'up' | 'degraded' | 'down';
  health: HealthCheckResult | null;
  searchSync: {
    pending: number;
    processing: number;
    completed: number;
    failed: number;
  } | null;
  crawler: {
    totalRuns: number;
    queuedRuns: number;
    processingRuns: number;
    successRuns: number;
    failedRuns: number;
    successRate: number;
    latestRunAt: string | null;
    latestSuccessAt: string | null;
  } | null;
  queue: QueueStatsResult | null;
  errors: Record<string, string>;
  alerts: Array<{
    key: string;
    severity: 'warning' | 'critical';
    message: string;
  }>;
  alertCount: number;
}

export interface ObservabilityMetricsSummary {
  totalRequests: number;
  errorRequests: number;
  errorRate: number;
  avgLatencyMs: number;
  p95LatencyMs: number;
  p99LatencyMs: number;
  statusBuckets: {
    s2xx: number;
    s3xx: number;
    s4xx: number;
    s5xx: number;
  };
}

export interface ObservabilityTraceItem {
  requestId: string;
  method: string;
  path: string;
  statusCode: number;
  durationMs: number;
  ip?: string | null;
  userId?: number | null;
  timestamp: string;
}

export interface ObservabilityDashboard {
  checkedAt: string;
  process: {
    uptimeSec: number;
    memory: {
      rss: number;
      heapTotal: number;
      heapUsed: number;
      external: number;
      arrayBuffers: number;
    };
  };
  metrics: ObservabilityMetricsSummary;
  queue: QueueStatsResult;
  resilience: {
    circuits: ResilienceCircuitSnapshot[];
    adaptivePolicies: ResiliencePolicyItem[];
  };
  searchSync: {
    pending: number;
    processing: number;
    completed: number;
    failed: number;
  };
  crawler: {
    totalRuns: number;
    queuedRuns: number;
    processingRuns: number;
    successRuns: number;
    failedRuns: number;
    successRate: number;
    latestRunAt: string | null;
    latestSuccessAt: string | null;
  };
  opsSummary: OpsDashboardSummary;
}

export interface NewsItem {
  id: number;
  title: string;
  summary: string;
  category: string;
  thumbnailUrl: string | null;
  createdAt: string;
}

export interface DealItem {
  id: number;
  product: {
    id: number;
    name: string;
    thumbnailUrl: string | null;
    lowestPrice: number | null;
  } | null;
  title: string;
  description: string | null;
  productId: number;
  discountRate: number;
  startAt: string;
  endAt: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface BoardItem {
  id: number;
  slug?: string;
  name: string;
  description?: string | null;
}

export interface PostSummaryItem {
  id: number;
  title: string;
  viewCount: number;
  likeCount: number;
  commentCount: number;
  createdAt: string;
  author?: {
    id: number;
    name?: string;
    nickname?: string;
  } | null;
}

export interface PostDetailItem {
  id: number;
  boardId?: number;
  title: string;
  content: string;
  viewCount: number;
  likeCount: number;
  commentCount: number;
  createdAt: string;
  updatedAt?: string;
  author?: {
    id: number;
    name?: string;
    nickname?: string;
  } | null;
}

export interface PostCommentItem {
  id: number;
  postId?: number;
  content: string;
  createdAt: string;
  author?: {
    id: number;
    name?: string;
    nickname?: string;
  } | null;
}

export interface InquiryItem {
  id: number;
  productId: number;
  userId?: number;
  title: string;
  content: string;
  isSecret: boolean;
  status?: 'PENDING' | 'ANSWERED';
  createdAt: string;
  updatedAt?: string;
  user?: {
    id: number;
    name?: string;
    nickname?: string;
  } | null;
  answer?: {
    id?: number;
    content: string;
    createdAt?: string;
    responderId?: number;
    responderRole?: string;
  } | null;
}

export interface SupportTicketItem {
  id: number;
  category: string;
  title: string;
  content: string;
  attachmentUrl: string | null;
  status: 'OPEN' | 'ANSWERED';
  answer?: {
    content: string;
    answeredBy: number;
    answeredAt: string;
  } | null;
  createdAt: string;
  updatedAt?: string;
}

export interface FaqItem {
  id: number;
  category: string;
  question: string;
  answer: string;
  isActive: boolean;
  createdAt: string;
  updatedAt?: string;
}

export interface NoticeItem {
  id: number;
  title: string;
  content: string;
  isPublished: boolean;
  createdAt: string;
  updatedAt?: string;
}

export interface ActivityRecentProductItem {
  id: number;
  product: {
    id: number;
    name?: string;
    thumbnailUrl?: string | null;
    lowestPrice?: number | null;
  };
  viewedAt: string;
  createdAt: string;
  updatedAt?: string;
}

export interface ActivitySearchItem {
  id: number;
  keyword: string;
  createdAt: string;
  updatedAt?: string;
}

export interface ActivitySummary {
  recentProducts: ActivityRecentProductItem[];
  recentSearches: ActivitySearchItem[];
  orderSummary: {
    totalOrderCount: number;
  };
}

export interface ChatRoomItem {
  id: number;
  name: string;
  createdBy: number;
  isPrivate: boolean;
  createdAt: string;
  updatedAt?: string;
}

export interface ChatMessageItem {
  id: number;
  roomId: number;
  senderId: number;
  message: string;
  createdAt: string;
  updatedAt?: string;
}
