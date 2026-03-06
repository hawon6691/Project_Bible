import { useMemo, useState } from 'react';
import { Link, Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom';
import HomePage from '@/pages/HomePage';
import PublicLandingPage from '@/pages/PublicLandingPage';
import ProductsPage from '@/pages/ProductsPage';
import ProductDetailPage from '@/pages/ProductDetailPage';
import LoginPage from '@/pages/LoginPage';
import SignupPage from '@/pages/SignupPage';
import VerifyEmailPage from '@/pages/VerifyEmailPage';
import PasswordResetPage from '@/pages/PasswordResetPage';
import CartPage from '@/pages/CartPage';
import MyPage from '@/pages/MyPage';
import UserApiPage from '@/pages/UserApiPage';
import CategoryApiPage from '@/pages/CategoryApiPage';
import ProductApiPage from '@/pages/ProductApiPage';
import SpecApiPage from '@/pages/SpecApiPage';
import SellerApiPage from '@/pages/SellerApiPage';
import PriceApiPage from '@/pages/PriceApiPage';
import CartApiPage from '@/pages/CartApiPage';
import AddressApiPage from '@/pages/AddressApiPage';
import OrderApiPage from '@/pages/OrderApiPage';
import PaymentApiPage from '@/pages/PaymentApiPage';
import ReviewApiPage from '@/pages/ReviewApiPage';
import WishlistApiPage from '@/pages/WishlistApiPage';
import PointApiPage from '@/pages/PointApiPage';
import CommunityApiPage from '@/pages/CommunityApiPage';
import InquiryApiPage from '@/pages/InquiryApiPage';
import SupportApiPage from '@/pages/SupportApiPage';
import HelpApiPage from '@/pages/HelpApiPage';
import ActivityApiPage from '@/pages/ActivityApiPage';
import ChatApiPage from '@/pages/ChatApiPage';
import PushApiPage from '@/pages/PushApiPage';
import RankingApiPage from '@/pages/RankingApiPage';
import RecommendationApiPage from '@/pages/RecommendationApiPage';
import DealApiPage from '@/pages/DealApiPage';
import PredictionApiPage from '@/pages/PredictionApiPage';
import FraudApiPage from '@/pages/FraudApiPage';
import TrustApiPage from '@/pages/TrustApiPage';
import I18nApiPage from '@/pages/I18nApiPage';
import ImageApiPage from '@/pages/ImageApiPage';
import BadgeApiPage from '@/pages/BadgeApiPage';
import PcBuilderApiPage from '@/pages/PcBuilderApiPage';
import FriendApiPage from '@/pages/FriendApiPage';
import ShortformApiPage from '@/pages/ShortformApiPage';
import MediaApiPage from '@/pages/MediaApiPage';
import NewsApiPage from '@/pages/NewsApiPage';
import MatchingApiPage from '@/pages/MatchingApiPage';
import PriceAnalyticsApiPage from '@/pages/PriceAnalyticsApiPage';
import UsedMarketApiPage from '@/pages/UsedMarketApiPage';
import AutoApiPage from '@/pages/AutoApiPage';
import AuctionApiPage from '@/pages/AuctionApiPage';
import CompareApiPage from '@/pages/CompareApiPage';
import AdminSettingsApiPage from '@/pages/AdminSettingsApiPage';
import HealthApiPage from '@/pages/HealthApiPage';
import CrawlerApiPage from '@/pages/CrawlerApiPage';
import QueryApiPage from '@/pages/QueryApiPage';
import SearchSyncApiPage from '@/pages/SearchSyncApiPage';
import ResilienceApiPage from '@/pages/ResilienceApiPage';
import ErrorCodeApiPage from '@/pages/ErrorCodeApiPage';
import QueueAdminApiPage from '@/pages/QueueAdminApiPage';
import OpsDashboardApiPage from '@/pages/OpsDashboardApiPage';
import ObservabilityApiPage from '@/pages/ObservabilityApiPage';
import { clearAuth, getAccessToken } from '@/lib/auth';
import { logout } from '@/lib/endpoints';

type UiMode = 'public' | 'developer';
type NavLinkItem = { to: string; label: string };

function ClockIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <rect x="3" y="3" width="18" height="18" rx="4" stroke="currentColor" strokeWidth="1.8" />
      <path d="M12 7.5V12H15.5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

function HeartIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M12 20.2s-6.8-4.4-8.7-8.1c-1.2-2.5-.1-5.5 2.4-6.7 2.1-1 4.6-.3 6.3 1.5 1.7-1.8 4.2-2.5 6.3-1.5 2.5 1.2 3.6 4.2 2.4 6.7-1.9 3.7-8.7 8.1-8.7 8.1z"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function UserIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="12" cy="8" r="3.5" stroke="currentColor" strokeWidth="1.8" />
      <path d="M5 20c0-3.2 3.1-5 7-5s7 1.8 7 5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
    </svg>
  );
}

function CartIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M3.5 5h2l1.4 8.2h10.2l1.5-6.1H7.2" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
      <circle cx="10" cy="18" r="1.6" fill="currentColor" />
      <circle cx="16.6" cy="18" r="1.6" fill="currentColor" />
    </svg>
  );
}

const coreLinks: NavLinkItem[] = [
  { to: '/public', label: '홈' },
  { to: '/public/products', label: '상품' },
  { to: '/public/cart', label: '장바구니' },
];

const publicTabs: NavLinkItem[] = [
  { to: '/public/products?search=자동차', label: '자동차' },
  { to: '/public/products?search=조립PC', label: '조립PC' },
  { to: '/public/products?search=PC견적', label: 'PC견적' },
  { to: '/public/products?search=쇼핑기획전', label: '쇼핑기획전' },
  { to: '/public/products?search=커뮤니티', label: '커뮤니티' },
  { to: '/public/products?search=이벤트', label: '이벤트/체험단' },
];

const developerLinks: NavLinkItem[] = [
  { to: '/developer/user-api', label: 'UserAPI' },
  { to: '/developer/category-api', label: 'CategoryAPI' },
  { to: '/developer/product-api', label: 'ProductAPI' },
  { to: '/developer/spec-api', label: 'SpecAPI' },
  { to: '/developer/seller-api', label: 'SellerAPI' },
  { to: '/developer/price-api', label: 'PriceAPI' },
  { to: '/developer/cart-api', label: 'CartAPI' },
  { to: '/developer/address-api', label: 'AddressAPI' },
  { to: '/developer/order-api', label: 'OrderAPI' },
  { to: '/developer/payment-api', label: 'PaymentAPI' },
  { to: '/developer/review-api', label: 'ReviewAPI' },
  { to: '/developer/wishlist-api', label: 'WishlistAPI' },
  { to: '/developer/point-api', label: 'PointAPI' },
  { to: '/developer/community-api', label: 'CommunityAPI' },
  { to: '/developer/inquiry-api', label: 'InquiryAPI' },
  { to: '/developer/support-api', label: 'SupportAPI' },
  { to: '/developer/help-api', label: 'HelpAPI' },
  { to: '/developer/activity-api', label: 'ActivityAPI' },
  { to: '/developer/chat-api', label: 'ChatAPI' },
  { to: '/developer/push-api', label: 'PushAPI' },
  { to: '/developer/ranking-api', label: 'RankingAPI' },
  { to: '/developer/recommendation-api', label: 'RecommendationAPI' },
  { to: '/developer/deal-api', label: 'DealAPI' },
  { to: '/developer/prediction-api', label: 'PredictionAPI' },
  { to: '/developer/fraud-api', label: 'FraudAPI' },
  { to: '/developer/trust-api', label: 'TrustAPI' },
  { to: '/developer/i18n-api', label: 'I18nAPI' },
  { to: '/developer/image-api', label: 'ImageAPI' },
  { to: '/developer/badge-api', label: 'BadgeAPI' },
  { to: '/developer/pc-builder-api', label: 'PcBuilderAPI' },
  { to: '/developer/friend-api', label: 'FriendAPI' },
  { to: '/developer/shortform-api', label: 'ShortformAPI' },
  { to: '/developer/media-api', label: 'MediaAPI' },
  { to: '/developer/news-api', label: 'NewsAPI' },
  { to: '/developer/matching-api', label: 'MatchingAPI' },
  { to: '/developer/price-analytics-api', label: 'PriceAnalyticsAPI' },
  { to: '/developer/used-market-api', label: 'UsedMarketAPI' },
  { to: '/developer/auto-api', label: 'AutoAPI' },
  { to: '/developer/auction-api', label: 'AuctionAPI' },
  { to: '/developer/compare-api', label: 'CompareAPI' },
  { to: '/developer/admin-settings-api', label: 'AdminSettingsAPI' },
  { to: '/developer/health-api', label: 'HealthAPI' },
  { to: '/developer/crawler-api', label: 'CrawlerAPI' },
  { to: '/developer/query-api', label: 'QueryAPI' },
  { to: '/developer/search-sync-api', label: 'SearchSyncAPI' },
  { to: '/developer/resilience-api', label: 'ResilienceAPI' },
  { to: '/developer/error-code-api', label: 'ErrorCodeAPI' },
  { to: '/developer/queue-admin-api', label: 'QueueAdminAPI' },
  { to: '/developer/ops-dashboard-api', label: 'OpsDashboardAPI' },
  { to: '/developer/observability-api', label: 'ObservabilityAPI' },
];

function Header({
  uiMode,
}: {
  uiMode: UiMode;
}) {
  const navigate = useNavigate();
  const isLoggedIn = Boolean(getAccessToken());
  const [search, setSearch] = useState('');
  const links = useMemo(
    () => (uiMode === 'developer' ? [...coreLinks, ...developerLinks] : coreLinks),
    [uiMode],
  );

  return (
    <header className="header">
      <div className="header-inner">
        <div className="header-top">
          <Link to="/public" className="logo">PBShop</Link>
          {uiMode === 'public' ? (
            <div className="public-tools">
              <form
                className="public-search"
                onSubmit={(e) => {
                  e.preventDefault();
                  const keyword = search.trim();
                  navigate(keyword ? `/public/products?search=${encodeURIComponent(keyword)}` : '/public/products');
                }}
              >
                <input
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  placeholder="상품 검색"
                />
                <button type="submit" className="btn btn-primary btn-sm public-search-btn" aria-label="검색">
                  🔍
                </button>
              </form>
              <div className="public-quick-links">
                <Link className="quick-icon-link" to="/public/products?search=최근" aria-label="최근">
                  <span className="quick-icon"><ClockIcon /></span>
                  <span className="quick-label">최근</span>
                </Link>
                <Link className="quick-icon-link" to="/public/mypage" aria-label="관심">
                  <span className="quick-icon"><HeartIcon /></span>
                  <span className="quick-label">관심</span>
                </Link>
                <Link className="quick-icon-link" to="/public/cart" aria-label="장바구니">
                  <span className="quick-icon"><CartIcon /></span>
                  <span className="quick-label">장바구니</span>
                </Link>
                <div className="login-popover quick-icon-link-wrap">
                  <Link className="quick-icon-link" to={isLoggedIn ? '/public/mypage' : '/public/login'} aria-label="사용자">
                    <span className="quick-icon"><UserIcon /></span>
                    <span className="quick-label">사용자</span>
                  </Link>
                  <div className="login-menu quick-login-menu">
                    <Link to="/public/login">로그인</Link>
                    <Link to="/public/signup">회원가입</Link>
                    <Link to="/public/mypage">마이페이지</Link>
                    <a href="#">쪽지</a>
                    <a href="#">고객센터</a>
                    {isLoggedIn ? (
                      <button
                        type="button"
                        className="link-btn"
                        onClick={async () => {
                          await logout().catch(() => null);
                          clearAuth();
                          navigate('/public');
                        }}
                      >
                        로그아웃
                      </button>
                    ) : null}
                  </div>
                </div>
              </div>
            </div>
          ) : null}
        </div>
        {uiMode === 'public' ? (
          <div className="public-menu-row">
            <Link to="/public#home-categories" className="all-category-btn">☰ 전체 카테고리</Link>
            <nav className="public-tab-menu">
              {publicTabs.map((item) => (
                <Link key={item.to} to={item.to}>{item.label}</Link>
              ))}
              <div className="more-popover">
                <a href="#">더보기</a>
                <div className="more-menu">
                  <a href="#">샵다나와</a>
                  <a href="#">중고마켓</a>
                  <a href="#">PC26</a>
                  <a href="#">장터</a>
                  <a href="#">브랜드로그</a>
                  <a href="#">동영상</a>
                  <a href="#">쇼핑가이드</a>
                  <a href="#">다나와AS</a>
                </div>
              </div>
            </nav>
          </div>
        ) : (
          <nav className="menu">
            {links.map((item) => (
              <Link key={item.to} to={item.to}>{item.label}</Link>
            ))}
            {isLoggedIn ? <Link to="/public/mypage">My</Link> : <Link to="/public/signup">Signup</Link>}
            {isLoggedIn ? (
              <button
                type="button"
                className="link-btn"
                onClick={async () => {
                  await logout().catch(() => null);
                  clearAuth();
                  navigate('/public');
                }}
              >
                Logout
              </button>
            ) : (
              <Link to="/public/login">Login</Link>
            )}
          </nav>
        )}
      </div>
    </header>
  );
}

export default function App() {
  const location = useLocation();
  const uiMode: UiMode = useMemo(() => {
    const path = location.pathname;
    if (path.startsWith('/developer')) {
      return 'developer';
    }
    return 'public';
  }, [location.pathname]);

  return (
    <div className="app-shell">
      <Header uiMode={uiMode} />
      <main className="main">
        <Routes>
          <Route path="/" element={<Navigate to="/public" replace />} />
          <Route path="/public" element={<PublicLandingPage />} />
          <Route path="/developer" element={<HomePage />} />
          <Route path="/public/products" element={<ProductsPage />} />
          <Route path="/public/products/:id" element={<ProductDetailPage />} />
          <Route path="/public/login" element={<LoginPage />} />
          <Route path="/public/signup" element={<SignupPage />} />
          <Route path="/public/auth/verify-email" element={<VerifyEmailPage />} />
          <Route path="/public/auth/password-reset" element={<PasswordResetPage />} />
          <Route path="/public/cart" element={<CartPage />} />
          <Route path="/public/mypage" element={<MyPage />} />

          <Route path="/products" element={<Navigate to="/public/products" replace />} />
          <Route path="/products/:id" element={<ProductDetailPage />} />
          <Route path="/login" element={<Navigate to="/public/login" replace />} />
          <Route path="/signup" element={<Navigate to="/public/signup" replace />} />
          <Route path="/auth/verify-email" element={<Navigate to="/public/auth/verify-email" replace />} />
          <Route path="/auth/password-reset" element={<Navigate to="/public/auth/password-reset" replace />} />
          <Route path="/cart" element={<Navigate to="/public/cart" replace />} />
          <Route path="/mypage" element={<Navigate to="/public/mypage" replace />} />

          <Route path="/developer/user-api" element={<UserApiPage />} />
          <Route path="/developer/category-api" element={<CategoryApiPage />} />
          <Route path="/developer/product-api" element={<ProductApiPage />} />
          <Route path="/developer/spec-api" element={<SpecApiPage />} />
          <Route path="/developer/seller-api" element={<SellerApiPage />} />
          <Route path="/developer/price-api" element={<PriceApiPage />} />
          <Route path="/developer/cart-api" element={<CartApiPage />} />
          <Route path="/developer/address-api" element={<AddressApiPage />} />
          <Route path="/developer/order-api" element={<OrderApiPage />} />
          <Route path="/developer/payment-api" element={<PaymentApiPage />} />
          <Route path="/developer/review-api" element={<ReviewApiPage />} />
          <Route path="/developer/wishlist-api" element={<WishlistApiPage />} />
          <Route path="/developer/point-api" element={<PointApiPage />} />
          <Route path="/developer/community-api" element={<CommunityApiPage />} />
          <Route path="/developer/inquiry-api" element={<InquiryApiPage />} />
          <Route path="/developer/support-api" element={<SupportApiPage />} />
          <Route path="/developer/help-api" element={<HelpApiPage />} />
          <Route path="/developer/activity-api" element={<ActivityApiPage />} />
          <Route path="/developer/chat-api" element={<ChatApiPage />} />
          <Route path="/developer/push-api" element={<PushApiPage />} />
          <Route path="/developer/ranking-api" element={<RankingApiPage />} />
          <Route path="/developer/recommendation-api" element={<RecommendationApiPage />} />
          <Route path="/developer/deal-api" element={<DealApiPage />} />
          <Route path="/developer/prediction-api" element={<PredictionApiPage />} />
          <Route path="/developer/fraud-api" element={<FraudApiPage />} />
          <Route path="/developer/trust-api" element={<TrustApiPage />} />
          <Route path="/developer/i18n-api" element={<I18nApiPage />} />
          <Route path="/developer/image-api" element={<ImageApiPage />} />
          <Route path="/developer/badge-api" element={<BadgeApiPage />} />
          <Route path="/developer/pc-builder-api" element={<PcBuilderApiPage />} />
          <Route path="/developer/friend-api" element={<FriendApiPage />} />
          <Route path="/developer/shortform-api" element={<ShortformApiPage />} />
          <Route path="/developer/media-api" element={<MediaApiPage />} />
          <Route path="/developer/news-api" element={<NewsApiPage />} />
          <Route path="/developer/matching-api" element={<MatchingApiPage />} />
          <Route path="/developer/price-analytics-api" element={<PriceAnalyticsApiPage />} />
          <Route path="/developer/used-market-api" element={<UsedMarketApiPage />} />
          <Route path="/developer/auto-api" element={<AutoApiPage />} />
          <Route path="/developer/auction-api" element={<AuctionApiPage />} />
          <Route path="/developer/compare-api" element={<CompareApiPage />} />
          <Route path="/developer/admin-settings-api" element={<AdminSettingsApiPage />} />
          <Route path="/developer/health-api" element={<HealthApiPage />} />
          <Route path="/developer/crawler-api" element={<CrawlerApiPage />} />
          <Route path="/developer/query-api" element={<QueryApiPage />} />
          <Route path="/developer/search-sync-api" element={<SearchSyncApiPage />} />
          <Route path="/developer/resilience-api" element={<ResilienceApiPage />} />
          <Route path="/developer/error-code-api" element={<ErrorCodeApiPage />} />
          <Route path="/developer/queue-admin-api" element={<QueueAdminApiPage />} />
          <Route path="/developer/ops-dashboard-api" element={<OpsDashboardApiPage />} />
          <Route path="/developer/observability-api" element={<ObservabilityApiPage />} />

          <Route path="/user-api" element={<Navigate to="/developer/user-api" replace />} />
          <Route path="/category-api" element={<Navigate to="/developer/category-api" replace />} />
          <Route path="/product-api" element={<Navigate to="/developer/product-api" replace />} />
          <Route path="/spec-api" element={<Navigate to="/developer/spec-api" replace />} />
          <Route path="/seller-api" element={<Navigate to="/developer/seller-api" replace />} />
          <Route path="/price-api" element={<Navigate to="/developer/price-api" replace />} />
          <Route path="/cart-api" element={<Navigate to="/developer/cart-api" replace />} />
          <Route path="/address-api" element={<Navigate to="/developer/address-api" replace />} />
          <Route path="/order-api" element={<Navigate to="/developer/order-api" replace />} />
          <Route path="/payment-api" element={<Navigate to="/developer/payment-api" replace />} />
          <Route path="/review-api" element={<Navigate to="/developer/review-api" replace />} />
          <Route path="/wishlist-api" element={<Navigate to="/developer/wishlist-api" replace />} />
          <Route path="/point-api" element={<Navigate to="/developer/point-api" replace />} />
          <Route path="/community-api" element={<Navigate to="/developer/community-api" replace />} />
          <Route path="/inquiry-api" element={<Navigate to="/developer/inquiry-api" replace />} />
          <Route path="/support-api" element={<Navigate to="/developer/support-api" replace />} />
          <Route path="/help-api" element={<Navigate to="/developer/help-api" replace />} />
          <Route path="/activity-api" element={<Navigate to="/developer/activity-api" replace />} />
          <Route path="/chat-api" element={<Navigate to="/developer/chat-api" replace />} />
          <Route path="/push-api" element={<Navigate to="/developer/push-api" replace />} />
          <Route path="/ranking-api" element={<Navigate to="/developer/ranking-api" replace />} />
          <Route path="/recommendation-api" element={<Navigate to="/developer/recommendation-api" replace />} />
          <Route path="/deal-api" element={<Navigate to="/developer/deal-api" replace />} />
          <Route path="/prediction-api" element={<Navigate to="/developer/prediction-api" replace />} />
          <Route path="/fraud-api" element={<Navigate to="/developer/fraud-api" replace />} />
          <Route path="/trust-api" element={<Navigate to="/developer/trust-api" replace />} />
          <Route path="/i18n-api" element={<Navigate to="/developer/i18n-api" replace />} />
          <Route path="/image-api" element={<Navigate to="/developer/image-api" replace />} />
          <Route path="/badge-api" element={<Navigate to="/developer/badge-api" replace />} />
          <Route path="/pc-builder-api" element={<Navigate to="/developer/pc-builder-api" replace />} />
          <Route path="/friend-api" element={<Navigate to="/developer/friend-api" replace />} />
          <Route path="/shortform-api" element={<Navigate to="/developer/shortform-api" replace />} />
          <Route path="/media-api" element={<Navigate to="/developer/media-api" replace />} />
          <Route path="/news-api" element={<Navigate to="/developer/news-api" replace />} />
          <Route path="/matching-api" element={<Navigate to="/developer/matching-api" replace />} />
          <Route path="/price-analytics-api" element={<Navigate to="/developer/price-analytics-api" replace />} />
          <Route path="/used-market-api" element={<Navigate to="/developer/used-market-api" replace />} />
          <Route path="/auto-api" element={<Navigate to="/developer/auto-api" replace />} />
          <Route path="/auction-api" element={<Navigate to="/developer/auction-api" replace />} />
          <Route path="/compare-api" element={<Navigate to="/developer/compare-api" replace />} />
          <Route path="/admin-settings-api" element={<Navigate to="/developer/admin-settings-api" replace />} />
          <Route path="/health-api" element={<Navigate to="/developer/health-api" replace />} />
          <Route path="/crawler-api" element={<Navigate to="/developer/crawler-api" replace />} />
          <Route path="/query-api" element={<Navigate to="/developer/query-api" replace />} />
          <Route path="/search-sync-api" element={<Navigate to="/developer/search-sync-api" replace />} />
          <Route path="/resilience-api" element={<Navigate to="/developer/resilience-api" replace />} />
          <Route path="/error-code-api" element={<Navigate to="/developer/error-code-api" replace />} />
          <Route path="/queue-admin-api" element={<Navigate to="/developer/queue-admin-api" replace />} />
          <Route path="/ops-dashboard-api" element={<Navigate to="/developer/ops-dashboard-api" replace />} />
          <Route path="/observability-api" element={<Navigate to="/developer/observability-api" replace />} />
        </Routes>
      </main>
    </div>
  );
}
