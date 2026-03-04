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
  title: string;
  productId: number;
  sellerId: number;
  originalPrice: number;
  dealPrice: number;
  discountRate: number;
  startsAt: string;
  endsAt: string;
  isActive: boolean;
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
