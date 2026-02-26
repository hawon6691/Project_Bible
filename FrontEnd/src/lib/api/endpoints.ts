import apiClient from './client';
import type { AxiosResponse } from 'axios';
import type { ApiResponse, MessageResponse, PaginationParams } from '@/types/common.types';
import type { LoginRequest, LoginResponse, SignupRequest, SignupResponse, User } from '@/types/user.types';
import type {
  ProductSummary,
  ProductDetail,
  ProductQueryParams,
  Category,
  SpecDefinition,
  CompareResult,
  ScoredCompareResult,
} from '@/types/product.types';
import type { CartItem, Order, CreateOrderRequest, Address, Review, Coupon } from '@/types/order.types';

const unsupported = <T = never>(feature: string): Promise<T> =>
  Promise.reject(new Error(`${feature} is not supported by current backend API`));

const mapAddressFromBackend = (address: any): Address => ({
  id: address.id,
  name: address.recipientName ?? address.name ?? '',
  phone: address.phone ?? address.recipientPhone ?? '',
  zipCode: address.zipCode ?? '',
  address: address.address ?? '',
  addressDetail: address.addressDetail ?? '',
  isDefault: Boolean(address.isDefault),
});

const mapAddressToBackend = (address: Partial<Address>) => ({
  recipientName: address.name,
  phone: address.phone,
  zipCode: address.zipCode,
  address: address.address,
  addressDetail: address.addressDetail,
  isDefault: address.isDefault,
});

const mapCartItemFromBackend = (item: any): CartItem => ({
  id: item.id,
  productId: item.product?.id ?? item.productId,
  productName: item.product?.name ?? item.productName ?? '',
  thumbnailUrl: item.product?.thumbnailUrl ?? item.thumbnailUrl ?? '',
  price: item.product?.lowestPrice ?? item.product?.price ?? item.price ?? 0,
  quantity: item.quantity ?? 1,
  selectedOptions: item.selectedOptions ?? undefined,
  sellerId: item.seller?.id ?? item.sellerId,
  sellerName: item.seller?.name ?? item.sellerName ?? '',
});

const mapOrderItem = (item: any) => ({
  id: item.id,
  productId: item.productId,
  productName: item.productName ?? '',
  thumbnailUrl: item.thumbnailUrl ?? '',
  quantity: item.quantity ?? 0,
  unitPrice: item.unitPrice ?? 0,
  totalPrice: item.totalPrice ?? 0,
  selectedOptions: item.selectedOptions ?? undefined,
  sellerId: item.sellerId ?? 0,
  sellerName: item.sellerName ?? '',
});

const mapOrderSummary = (order: any): Order => ({
  id: order.id,
  orderNumber: order.orderNumber,
  status: order.status,
  totalAmount: order.totalAmount ?? 0,
  discountAmount: order.discountAmount ?? 0,
  shippingFee: order.shippingFee ?? 0,
  finalAmount: order.finalAmount ?? 0,
  items: (order.items ?? []).map((item: any) => mapOrderItem(item)),
  shippingAddress: mapAddressFromBackend(order.shippingAddress ?? {}),
  createdAt: order.createdAt,
  paidAt: order.paidAt,
});

const mapOrderDetail = (order: any): Order => ({
  ...mapOrderSummary(order),
  items: (order.items ?? []).map((item: any) => mapOrderItem(item)),
  shippingAddress: mapAddressFromBackend(order.shippingAddress ?? {}),
  shippingFee: order.shippingFee ?? 0,
  discountAmount: (order.pointUsed ?? 0) + (order.discountAmount ?? 0),
});

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
  updateMe: (data: Partial<{ name: string; phone: string; password: string }>) => apiClient.patch<ApiResponse<User>>('/users/me/profile', data),
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
  addImage: (productId: number, data: { url: string; isMain?: boolean; sortOrder?: number }) => apiClient.post(`/products/${productId}/images`, data),
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
  getItems: async () => {
    const res = await apiClient.get<ApiResponse<any[]>>('/cart');
    return {
      ...res,
      data: {
        ...res.data,
        data: (res.data.data || []).map((item: any) => mapCartItemFromBackend(item)),
      },
    };
  },
  addItem: (data: { productId: number; quantity: number; sellerId: number; selectedOptions?: string }) => apiClient.post<ApiResponse<CartItem>>('/cart', data),
  updateItem: (id: number, data: { quantity: number }) => apiClient.patch<ApiResponse<CartItem>>(`/cart/${id}`, data),
  removeItem: (id: number) => apiClient.delete<ApiResponse<MessageResponse>>(`/cart/${id}`),
  clear: () => apiClient.delete<ApiResponse<MessageResponse>>('/cart'),
};

export const orderApi = {
  create: async (data: CreateOrderRequest) => {
    const payload = {
      addressId: data.addressId,
      items: (data.items as any[]).map((item: any) => ({
        productId: item.productId,
        sellerId: item.sellerId,
        quantity: item.quantity,
        selectedOptions: item.selectedOptions,
      })),
      usePoint: (data as any).usePoints ?? 0,
      fromCart: true,
      cartItemIds: (data.items as any[]).map((item: any) => item.id).filter((id) => typeof id === 'number'),
    };
    const res = await apiClient.post<ApiResponse<any>>('/orders', payload);
    return { ...res, data: { ...res.data, data: mapOrderDetail(res.data.data) } };
  },
  getAll: async (params?: PaginationParams & { status?: string }) => {
    const res = await apiClient.get<ApiResponse<any[]>>('/orders', { params });
    const mapped = (res.data.data || []).map((order: any) => ({
      ...mapOrderSummary(order),
      items: order.items ? order.items.map((item: any) => mapOrderItem(item)) : [],
    }));
    return { ...res, data: { ...res.data, data: mapped } };
  },
  getOne: async (id: number) => {
    const res = await apiClient.get<ApiResponse<any>>(`/orders/${id}`);
    return { ...res, data: { ...res.data, data: mapOrderDetail(res.data.data) } };
  },
  cancel: async (id: number) => {
    const res = await apiClient.post<ApiResponse<any>>(`/orders/${id}/cancel`);
    return { ...res, data: { ...res.data, data: mapOrderDetail(res.data.data) } };
  },
  requestRefund: (id: number, data: { reason: string }) => apiClient.post<ApiResponse<MessageResponse>>(`/payments/${id}/refund`, data),
};

export const addressApi = {
  getAll: async () => {
    const res = await apiClient.get<ApiResponse<any[]>>('/addresses');
    return {
      ...res,
      data: {
        ...res.data,
        data: (res.data.data || []).map((address: any) => mapAddressFromBackend(address)),
      },
    };
  },
  create: async (data: Omit<Address, 'id'>) => {
    const res = await apiClient.post<ApiResponse<any>>('/addresses', mapAddressToBackend(data));
    return { ...res, data: { ...res.data, data: mapAddressFromBackend(res.data.data) } };
  },
  update: async (id: number, data: Partial<Address>) => {
    const res = await apiClient.patch<ApiResponse<any>>(`/addresses/${id}`, mapAddressToBackend(data));
    return { ...res, data: { ...res.data, data: mapAddressFromBackend(res.data.data) } };
  },
  delete: (id: number) => apiClient.delete<ApiResponse<MessageResponse>>(`/addresses/${id}`),
  setDefault: async (id: number) => {
    const res = await apiClient.patch<ApiResponse<any>>(`/addresses/${id}`, { isDefault: true });
    return { ...res, data: { ...res.data, data: mapAddressFromBackend(res.data.data) } };
  },
};

export const reviewApi = {
  getByProduct: (productId: number, params?: PaginationParams & { sort?: string }) => apiClient.get<ApiResponse<Review[]>>(`/products/${productId}/reviews`, { params }),
  create: (productId: number, data: FormData | Record<string, unknown>) => {
    if (data instanceof FormData) {
      const body = Object.fromEntries(data.entries());
      return apiClient.post<ApiResponse<Review>>(`/products/${productId}/reviews`, body);
    }
    return apiClient.post<ApiResponse<Review>>(`/products/${productId}/reviews`, data);
  },
  update: (reviewId: number, data: Record<string, unknown>) => apiClient.patch<ApiResponse<Review>>(`/reviews/${reviewId}`, data),
  delete: (reviewId: number) => apiClient.delete<ApiResponse<MessageResponse>>(`/reviews/${reviewId}`),
  helpful: (reviewId: number) => unsupported<AxiosResponse<ApiResponse<MessageResponse>>>(`review helpful (${reviewId})`),
  getMyReviews: (_params?: PaginationParams) => unsupported<AxiosResponse<ApiResponse<Review[]>>>('my review list'),
};

export const couponApi = {
  getMyCoupons: () => unsupported<AxiosResponse<ApiResponse<Coupon[]>>>('coupon module'),
  claim: (_code: string) => unsupported<AxiosResponse<ApiResponse<Coupon>>>('coupon module'),
};

export const priceApi = {
  getHistory: (productId: number, params?: { period?: string; days?: number }) =>
    apiClient.get(`/products/${productId}/price-history`, {
      params: { period: params?.period ?? (params?.days ? `${params.days}d` : undefined) },
    }),
  setAlert: (productId: number, targetPrice: number) => apiClient.post('/price-alerts', { productId, targetPrice }),
  getMyAlerts: () => apiClient.get('/price-alerts'),
  deleteAlert: (id: number) => apiClient.delete(`/price-alerts/${id}`),
};

export const sellerApi = {
  register: (_data: Record<string, unknown>) => unsupported<AxiosResponse<ApiResponse<MessageResponse>>>('seller self registration'),
  getMyInfo: () => unsupported<AxiosResponse<ApiResponse<any>>>('seller me profile'),
  updateMyInfo: (_data: Record<string, unknown>) => unsupported<AxiosResponse<ApiResponse<any>>>('seller me profile'),
  getMyPrices: (_params?: PaginationParams) => unsupported<AxiosResponse<ApiResponse<any[]>>>('seller my prices'),
  setPrice: (data: { productId: number; price: number; url: string; shipping?: string }) => apiClient.post(`/products/${data.productId}/prices`, data),
  updatePrice: (priceId: number, data: { price: number; url?: string; shipping?: string }) => apiClient.patch(`/prices/${priceId}`, data),
  deletePrice: (priceId: number) => apiClient.delete(`/prices/${priceId}`),
};

export const faqAdminApi = {
  getFaqs: (params?: PaginationParams & { category?: string; keyword?: string }) =>
    apiClient.get('/faq', { params }),
  createFaq: (data: { category: string; question: string; answer: string; isActive?: boolean }) =>
    apiClient.post('/admin/faq', data),
  updateFaq: (id: number, data: { category?: string; question?: string; answer?: string; isActive?: boolean }) =>
    apiClient.patch(`/admin/faq/${id}`, data),
  deleteFaq: (id: number) => apiClient.delete(`/admin/faq/${id}`),
};

export const noticeAdminApi = {
  getNotices: (params?: PaginationParams) => apiClient.get('/notices', { params }),
  createNotice: (data: { title: string; content: string; isPublished?: boolean }) =>
    apiClient.post('/admin/notices', data),
  updateNotice: (id: number, data: { title?: string; content?: string; isPublished?: boolean }) =>
    apiClient.patch(`/admin/notices/${id}`, data),
  deleteNotice: (id: number) => apiClient.delete(`/admin/notices/${id}`),
};

export const settingsApi = {
  getAllowedExtensions: () => apiClient.get('/admin/settings/extensions'),
  setAllowedExtensions: (extensions: string[]) => apiClient.post('/admin/settings/extensions', { extensions }),
  getUploadLimits: () => apiClient.get('/admin/settings/upload-limits'),
  updateUploadLimits: (data: { image?: number; video?: number; audio?: number }) =>
    apiClient.patch('/admin/settings/upload-limits', data),
  getReviewPolicy: () => apiClient.get('/admin/settings/review-policy'),
  updateReviewPolicy: (data: { maxImageCount: number; pointAmount: number }) =>
    apiClient.patch('/admin/settings/review-policy', data),
};

export const observabilityApi = {
  getMetrics: () => apiClient.get('/admin/observability/metrics'),
  getTraces: (params?: { limit?: number; pathContains?: string }) =>
    apiClient.get('/admin/observability/traces', { params }),
  getDashboard: () => apiClient.get('/admin/observability/dashboard'),
};

export const adminApi = {
  // 기존 admin 대시보드 화면 호환을 위해 ops dashboard 요약 응답을 변환한다.
  getStats: async () => {
    const res = await apiClient.get<ApiResponse<any>>('/admin/ops-dashboard/summary');
    const src = res.data.data || {};
    const transformed = {
      totalUsers: 0,
      totalProducts: 0,
      totalOrders: 0,
      totalSellers: 0,
      totalRevenue: 0,
      totalReviews: 0,
      todayOrders: 0,
      todayRevenue: 0,
      overallStatus: src.overallStatus,
      alertCount: src.alertCount ?? 0,
      alerts: src.alerts ?? [],
    };
    return { ...res, data: { ...res.data, data: transformed } };
  },
  getSellers: (params?: PaginationParams & { status?: string }) => apiClient.get('/sellers', { params }),
  approveSeller: (id: number) => unsupported<AxiosResponse<ApiResponse<MessageResponse>>>(`seller approve API (${id})`),
  rejectSeller: (id: number, _reason: string) => unsupported<AxiosResponse<ApiResponse<MessageResponse>>>(`seller reject API (${id})`),
  getOrders: (params?: PaginationParams & { status?: string }) => apiClient.get('/admin/orders', { params }),
  updateOrderStatus: (id: number, status: string) => apiClient.patch(`/admin/orders/${id}/status`, { status }),
  getReviews: (_params?: PaginationParams & { reported?: boolean }) => unsupported<AxiosResponse<ApiResponse<Review[]>>>('admin review list'),
  deleteReview: (id: number) => apiClient.delete(`/reviews/${id}`),
  getCoupons: (_params?: PaginationParams) => unsupported<AxiosResponse<ApiResponse<Coupon[]>>>('coupon module'),
  createCoupon: (_data: Record<string, unknown>) => unsupported<AxiosResponse<ApiResponse<Coupon>>>('coupon module'),
  deleteCoupon: (_id: number) => unsupported<AxiosResponse<ApiResponse<MessageResponse>>>('coupon module'),
};
