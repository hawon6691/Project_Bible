// API 경로 상수는 컨트롤러/클라이언트 간 경로 불일치를 줄이기 위해 중앙 관리한다.
export const API_ROUTES = {
  AUTH: {
    BASE: 'auth',
    SIGNUP: 'signup',
    LOGIN: 'login',
    LOGOUT: 'logout',
    REFRESH: 'refresh',
    VERIFY_EMAIL: 'verify-email',
    RESEND_VERIFICATION: 'resend-verification',
    PASSWORD_RESET: 'password-reset',
    SOCIAL: ':provider',
    SOCIAL_CALLBACK: ':provider/callback',
  },
  USERS: {
    BASE: 'users',
    ME: 'me',
    PROFILE: 'profile',
  },
  PRODUCTS: {
    BASE: 'products',
    COMPARE: 'compare',
  },
  CATEGORIES: {
    BASE: 'categories',
    TREE: 'tree',
  },
  ORDERS: {
    BASE: 'orders',
    CANCEL: ':id/cancel',
    RETURN_REQUEST: ':id/return-request',
  },
  PAYMENTS: {
    BASE: 'payments',
    REFUND: ':id/refund',
  },
  REVIEWS: {
    BASE: 'reviews',
  },
  POINTS: {
    BASE: 'points',
    BALANCE: 'balance',
    TRANSACTIONS: 'transactions',
    ADMIN_GRANT: 'admin/points/grant',
  },
  ADDRESSES: {
    BASE: 'addresses',
    DETAIL: ':id',
  },
  WISHLIST: {
    BASE: 'wishlist',
    PRODUCT: ':productId',
  },
  CART: {
    BASE: 'cart',
  },
  SEARCH: {
    BASE: 'search',
    AUTOCOMPLETE: 'autocomplete',
    POPULAR: 'popular',
  },
  PRICES: {
    BASE: 'prices',
    HISTORY: 'history',
    ALERTS: 'alerts',
  },
  COMMUNITY: {
    BASE: 'community',
    BOARDS: 'boards',
    POSTS: 'posts',
    COMMENTS: 'comments',
  },
  INQUIRIES: {
    BASE: 'inquiries',
    PRODUCT_INQUIRIES: 'products/:productId/inquiries',
    ANSWER: ':id/answer',
    ME: 'me',
  },
  SUPPORT: {
    BASE: 'support',
    TICKETS: 'tickets',
    MY_TICKETS: 'tickets/me',
    ADMIN_TICKETS: 'admin/support/tickets',
    ANSWER: ':id/answer',
  },
  FAQ: {
    BASE: 'faq',
    SEARCH: 'search',
    ADMIN_FAQ: 'admin/faq',
    NOTICES: 'notices',
    ADMIN_NOTICES: 'admin/notices',
  },
  ACTIVITY: {
    BASE: 'activities',
    RECENT_PRODUCTS: 'recent-products',
    SEARCHES: 'searches',
  },
  CHAT: {
    BASE: 'chat',
    ROOMS: 'rooms',
    JOIN_ROOM: 'rooms/:id/join',
    MESSAGES: 'rooms/:id/messages',
  },
  PUSH: {
    BASE: 'push',
    SUBSCRIPTIONS: 'subscriptions',
    UNSUBSCRIBE: 'subscriptions/unsubscribe',
    PREFERENCES: 'preferences',
  },
  HEALTH: {
    BASE: 'health',
  },
} as const;





