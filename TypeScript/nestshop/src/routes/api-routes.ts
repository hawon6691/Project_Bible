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
  },
  REVIEWS: {
    BASE: 'reviews',
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
  CHAT: {
    BASE: 'chat',
    ROOMS: 'rooms',
  },
  HEALTH: {
    BASE: 'health',
  },
} as const;
