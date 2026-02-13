import apiClient from './client';
import type { ApiResponse, MessageResponse, PaginationParams } from '@/types/common.types';
import type { LoginRequest, LoginResponse, SignupRequest, SignupResponse, User } from '@/types/user.types';
import type { ProductSummary, ProductDetail, ProductQueryParams, Category, SpecDefinition, CompareResult, ScoredCompareResult } from '@/types/product.types';
import type { CartItem, Order, CreateOrderRequest, Address, Review, Coupon } from '@/types/order.types';

export const authApi = {
  signup: (data: SignupRequest) => apiClient.post<ApiResponse<SignupResponse>>('/auth/signup', data),
  login: (data: LoginRequest) => apiClient.post<ApiResponse<LoginResponse>>('/auth/login', data),
  logout: () => apiClient.post<ApiResponse<MessageResponse>>('/auth/logout'),
  refresh: (refreshToken: string) => apiClient.post<ApiResponse<LoginResponse>>('/auth/refresh', { refreshToken }),
  verifyEmail: (email: string, code: string) => apiClient.post<ApiResponse<{ message: string; verified: boolean }>>('/auth/verify-email', { email, code }),
  resendVerification: (email: string) => apiClient.post<ApiResponse<MessageResponse>>('/auth/resend-verification', { email }),
  passwordResetRequest: (email: string, phone: string) => apiClient.post<ApiResponse<MessageResponse>>('/auth/password-reset/request', { email, phone }),
  passwordResetVerify: (email: string, code: string) => apiClient.post<ApiResponse<{ resetToken: string }>>('/auth/password-reset/verify', { email, code }),
  passwordResetConfirm: (resetToken: string, newPassword: string) => apiClient.post<ApiResponse<MessageResponse>>('/auth/password-reset/confirm', { resetToken, newPassword }),
};

export const userApi = {
  getMe: () => apiClient.get<ApiResponse<User>>('/users/me'),
  updateMe: (data: Partial<{ name: string; phone: string; password: string }>) => apiClient.patch<ApiResponse<User>>('/users/me', data),
  deleteMe: () => apiClient.delete<ApiResponse<MessageResponse>>('/users/me'),
  getUsers: (params?: PaginationParams & { search?: string; status?: string; role?: string }) => apiClient.get<ApiResponse<User[]>>('/users', { params }),
  updateUserStatus: (id: number, status: string) => apiClient.patch<ApiResponse<User>>(`/users/${id}/status`, { status }),
};

export const categoryApi = {
  getAll: () => apiClient.get<ApiResponse<Category[]>>('/categories'),
  getOne: (id: number) => apiClient.get<ApiResponse<Category>>(`/categories/${id}`),
  create: (data: { name: string; parentId?: number }) => apiClient.post<ApiResponse<Category>>('/categories', data),
  update: (id: number, data: { name?: string; sortOrder?: number }) => apiClient.patch<ApiResponse<Category>>(`/categories/${id}`, data),
  delete: (id: number) => apiClient.delete<ApiResponse<MessageResponse>>(`/categories/${id}`),
};

export const productApi = {
  getAll: (params?: ProductQueryParams) => apiClient.get<ApiResponse<ProductSummary[]>>('/products', { params }),
  getOne: (id: number) => apiClient.get<ApiResponse<ProductDetail>>(`/products/${id}`),
  create: (data: Record<string, unknown>) => apiClient.post<ApiResponse<ProductDetail>>('/products', data),
  update: (id: number, data: Record<string, unknown>) => apiClient.patch<ApiResponse<ProductDetail>>(`/products/${id}`, data),
  delete: (id: number) => apiClient.delete<ApiResponse<MessageResponse>>(`/products/${id}`),
  addOption: (productId: number, data: { name: string; values: string[] }) => apiClient.post(`/products/${productId}/options`, data),
  updateOption: (productId: number, optionId: number, data: { name: string; values: string[] }) => apiClient.patch(`/products/${productId}/options/${optionId}`, data),
  deleteOption: (productId: number, optionId: number) => apiClient.delete(`/products/${productId}/options/${optionId}`),
  addImage: (productId: number, data: FormData) => apiClient.post(`/products/${productId}/images`, data, { headers: { 'Content-Type': 'multipart/form-data' } }),
  deleteImage: (productId: number, imageId: number) => apiClient.delete(`/products/${productId}/images/${imageId}`),
};

export const specApi = {
  getDefinitions: (categoryId?: number) => apiClient.get<ApiResponse<SpecDefinition[]>>('/specs/definitions', { params: { categoryId } }),
  createDefinition: (data: Record<string, unknown>) => apiClient.post<ApiResponse<SpecDefinition>>('/specs/definitions', data),
  updateDefinition: (id: number, data: Record<string, unknown>) => apiClient.patch<ApiResponse<SpecDefinition>>(`/specs/definitions/${id}`, data),
  deleteDefinition: (id: number) => apiClient.delete<ApiResponse<MessageResponse>>(`/specs/definitions/${id}`),
  getProductSpecs: (productId: number) => apiClient.get(`/products/${productId}/specs`),
  setProductSpecs: (productId: number, data: Record<string, unknown>) => apiClient.put(`/products/${productId}/specs`, data),
  compare: (productIds: number[]) => apiClient.post<ApiResponse<CompareResult>>('/specs/compare', { productIds }),
  scoredCompare: (productIds: number[], weights?: Record<string, number>) => apiClient.post<ApiResponse<ScoredCompareResult>>('/specs/compare/scored', { productIds, weights }),
};

export const cartApi = {
  getItems: () => apiClient.get<ApiResponse<CartItem[]>>('/cart'),
  addItem: (data: { productId: number; quantity: number; sellerId: number; selectedOptions?: string }) => apiClient.post<ApiResponse<CartItem>>('/cart', data),
  updateItem: (id: number, data: { quantity: number }) => apiClient.patch<ApiResponse<CartItem>>(`/cart/${id}`, data),
  removeItem: (id: number) => apiClient.delete<ApiResponse<MessageResponse>>(`/cart/${id}`),
  clear: () => apiClient.delete<ApiResponse<MessageResponse>>('/cart'),
};

export const orderApi = {
  create: (data: CreateOrderRequest) => apiClient.post<ApiResponse<Order>>('/orders', data),
  getAll: (params?: PaginationParams & { status?: string }) => apiClient.get<ApiResponse<Order[]>>('/orders', { params }),
  getOne: (id: number) => apiClient.get<ApiResponse<Order>>(`/orders/${id}`),
  cancel: (id: number) => apiClient.post<ApiResponse<MessageResponse>>(`/orders/${id}/cancel`),
  requestRefund: (id: number, data: { reason: string }) => apiClient.post<ApiResponse<MessageResponse>>(`/orders/${id}/refund`, data),
};

export const addressApi = {
  getAll: () => apiClient.get<ApiResponse<Address[]>>('/addresses'),
  create: (data: Omit<Address, 'id'>) => apiClient.post<ApiResponse<Address>>('/addresses', data),
  update: (id: number, data: Partial<Address>) => apiClient.patch<ApiResponse<Address>>(`/addresses/${id}`, data),
  delete: (id: number) => apiClient.delete<ApiResponse<MessageResponse>>(`/addresses/${id}`),
  setDefault: (id: number) => apiClient.patch<ApiResponse<Address>>(`/addresses/${id}/default`),
};

export const reviewApi = {
  getByProduct: (productId: number, params?: PaginationParams & { sort?: string }) => apiClient.get<ApiResponse<Review[]>>(`/products/${productId}/reviews`, { params }),
  create: (productId: number, data: FormData) => apiClient.post<ApiResponse<Review>>(`/products/${productId}/reviews`, data, { headers: { 'Content-Type': 'multipart/form-data' } }),
  update: (reviewId: number, data: Record<string, unknown>) => apiClient.patch<ApiResponse<Review>>(`/reviews/${reviewId}`, data),
  delete: (reviewId: number) => apiClient.delete<ApiResponse<MessageResponse>>(`/reviews/${reviewId}`),
  helpful: (reviewId: number) => apiClient.post<ApiResponse<MessageResponse>>(`/reviews/${reviewId}/helpful`),
  getMyReviews: (params?: PaginationParams) => apiClient.get<ApiResponse<Review[]>>('/reviews/me', { params }),
};

export const couponApi = {
  getMyCoupons: () => apiClient.get<ApiResponse<Coupon[]>>('/coupons/me'),
  claim: (code: string) => apiClient.post<ApiResponse<Coupon>>('/coupons/claim', { code }),
};

export const priceApi = {
  getHistory: (productId: number, params?: { days?: number }) => apiClient.get(`/products/${productId}/price-history`, { params }),
  setAlert: (productId: number, targetPrice: number) => apiClient.post(`/products/${productId}/price-alerts`, { targetPrice }),
  getMyAlerts: () => apiClient.get('/price-alerts/me'),
  deleteAlert: (id: number) => apiClient.delete(`/price-alerts/${id}`),
};

export const sellerApi = {
  register: (data: Record<string, unknown>) => apiClient.post('/sellers/register', data),
  getMyInfo: () => apiClient.get('/sellers/me'),
  updateMyInfo: (data: Record<string, unknown>) => apiClient.patch('/sellers/me', data),
  getMyPrices: (params?: PaginationParams) => apiClient.get('/sellers/me/prices', { params }),
  setPrice: (data: { productId: number; price: number; url: string; shipping?: string }) => apiClient.post('/sellers/me/prices', data),
  updatePrice: (priceId: number, data: { price: number; url?: string; shipping?: string }) => apiClient.patch(`/sellers/me/prices/${priceId}`, data),
  deletePrice: (priceId: number) => apiClient.delete(`/sellers/me/prices/${priceId}`),
};

export const adminApi = {
  getStats: () => apiClient.get('/admin/stats'),
  getSellers: (params?: PaginationParams & { status?: string }) => apiClient.get('/admin/sellers', { params }),
  approveSeller: (id: number) => apiClient.patch(`/admin/sellers/${id}/approve`),
  rejectSeller: (id: number, reason: string) => apiClient.patch(`/admin/sellers/${id}/reject`, { reason }),
  getOrders: (params?: PaginationParams & { status?: string }) => apiClient.get('/admin/orders', { params }),
  updateOrderStatus: (id: number, status: string) => apiClient.patch(`/admin/orders/${id}/status`, { status }),
  getReviews: (params?: PaginationParams & { reported?: boolean }) => apiClient.get('/admin/reviews', { params }),
  deleteReview: (id: number) => apiClient.delete(`/admin/reviews/${id}`),
  getCoupons: (params?: PaginationParams) => apiClient.get('/admin/coupons', { params }),
  createCoupon: (data: Record<string, unknown>) => apiClient.post('/admin/coupons', data),
  deleteCoupon: (id: number) => apiClient.delete(`/admin/coupons/${id}`),
};
