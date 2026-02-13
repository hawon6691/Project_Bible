export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  SIGNUP: '/signup',
  PRODUCTS: '/products',
  PRODUCT_DETAIL: (id: number) => `/products/${id}`,
  COMPARE: '/compare',
  CATEGORY: (id: number) => `/categories/${id}`,
  CART: '/cart',
  CHECKOUT: '/orders/checkout',
  ORDERS: '/orders',
  ORDER_DETAIL: (id: number) => `/orders/${id}`,
  MYPAGE: '/mypage',
  MYPAGE_PROFILE: '/mypage/profile',
  MYPAGE_REVIEWS: '/mypage/reviews',
  MYPAGE_WISHLIST: '/mypage/wishlist',
  MYPAGE_POINTS: '/mypage/points',
  MYPAGE_COUPONS: '/mypage/coupons',
  MYPAGE_ADDRESSES: '/mypage/addresses',
  SELLER: '/seller',
  SELLER_PRODUCTS: '/seller/products',
  ADMIN: '/admin',
  ADMIN_PRODUCTS: '/admin/products',
  ADMIN_CATEGORIES: '/admin/categories',
  ADMIN_USERS: '/admin/users',
  ADMIN_ORDERS: '/admin/orders',
  ADMIN_SELLERS: '/admin/sellers',
  ADMIN_REVIEWS: '/admin/reviews',
  ADMIN_COUPONS: '/admin/coupons',
  ADMIN_SPECS: '/admin/specs',
  ADMIN_BANNERS: '/admin/banners',
  ADMIN_FAQS: '/admin/faqs',
  ADMIN_NOTICES: '/admin/notices',
  ADMIN_REPORTS: '/admin/reports',
  ADMIN_SETTINGS: '/admin/settings',
  ADMIN_STATS: '/admin/stats',
} as const;

export const TOKEN_KEY = 'nestshop_access_token';
export const REFRESH_TOKEN_KEY = 'nestshop_refresh_token';

export const PAGE_SIZE = 20;

export const SORT_OPTIONS = [
  { label: '최신순', value: 'newest' },
  { label: '인기순', value: 'popularity' },
  { label: '낮은 가격순', value: 'price_asc' },
  { label: '높은 가격순', value: 'price_desc' },
  { label: '평점 높은순', value: 'rating_desc' },
  { label: '평점 낮은순', value: 'rating_asc' },
] as const;

export const ORDER_STATUS_LABELS: Record<string, string> = {
  PENDING: '주문 대기',
  PAID: '결제 완료',
  PREPARING: '상품 준비중',
  SHIPPING: '배송중',
  DELIVERED: '배송 완료',
  CANCELLED: '주문 취소',
  REFUND_REQUESTED: '환불 요청',
  REFUNDED: '환불 완료',
};

export const ORDER_STATUS_COLORS: Record<string, string> = {
  PENDING: 'default',
  PAID: 'processing',
  PREPARING: 'processing',
  SHIPPING: 'warning',
  DELIVERED: 'success',
  CANCELLED: 'error',
  REFUND_REQUESTED: 'warning',
  REFUNDED: 'default',
};
