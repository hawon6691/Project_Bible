import { request } from '@/lib/apiClient';
import { clearAuth, getAccessToken, getRefreshToken } from '@/lib/auth';
import { getOrCreateGuestCartKey } from '@/lib/cartKey';
import type {
  AddCartItemPayload,
  Address,
  CartItem,
  Category,
  CreateAddressPayload,
  CreateOrderPayload,
  CreateReviewPayload,
  DealItem,
  LoginPayload,
  NewsItem,
  OrderDetail,
  OrderSummary,
  PopularKeywordItem,
  ProductDetail,
  ProductQuery,
  ProductSummary,
  RankingProductItem,
  RequestPasswordResetPayload,
  ResetPasswordPayload,
  ReviewItem,
  SearchResultItem,
  SignupPayload,
  TokenResponse,
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

export async function fetchNews(limit = 6) {
  return request<NewsItem[]>('/news', { query: { page: 1, limit } });
}

export async function fetchDeals(limit = 6) {
  return request<DealItem[]>('/deals', { query: { limit, activeOnly: true } });
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

export async function cancelOrder(id: number) {
  return request(`/orders/${id}/cancel`, {
    method: 'POST',
    token: requireToken(),
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
