import { Link, Route, Routes, useNavigate } from 'react-router-dom';
import HomePage from '@/pages/HomePage';
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
import { clearAuth, getAccessToken } from '@/lib/auth';
import { logout } from '@/lib/endpoints';

function Header() {
  const navigate = useNavigate();
  const isLoggedIn = Boolean(getAccessToken());

  return (
    <header className="header">
      <div className="header-inner">
        <Link to="/" className="logo">NestShop</Link>
        <nav className="menu">
          <Link to="/products">Products</Link>
          <Link to="/cart">Cart</Link>
          <Link to="/user-api">UserAPI</Link>
          <Link to="/category-api">CategoryAPI</Link>
          <Link to="/product-api">ProductAPI</Link>
          <Link to="/spec-api">SpecAPI</Link>
          <Link to="/seller-api">SellerAPI</Link>
          <Link to="/price-api">PriceAPI</Link>
          <Link to="/cart-api">CartAPI</Link>
          <Link to="/address-api">AddressAPI</Link>
          <Link to="/order-api">OrderAPI</Link>
          <Link to="/payment-api">PaymentAPI</Link>
          <Link to="/review-api">ReviewAPI</Link>
          <Link to="/wishlist-api">WishlistAPI</Link>
          <Link to="/point-api">PointAPI</Link>
          <Link to="/community-api">CommunityAPI</Link>
          <Link to="/inquiry-api">InquiryAPI</Link>
          <Link to="/support-api">SupportAPI</Link>
          <Link to="/help-api">HelpAPI</Link>
          <Link to="/activity-api">ActivityAPI</Link>
          <Link to="/chat-api">ChatAPI</Link>
          <Link to="/ranking-api">RankingAPI</Link>
          <Link to="/recommendation-api">RecommendationAPI</Link>
          <Link to="/deal-api">DealAPI</Link>
          <Link to="/prediction-api">PredictionAPI</Link>
          <Link to="/fraud-api">FraudAPI</Link>
          <Link to="/trust-api">TrustAPI</Link>
          <Link to="/i18n-api">I18nAPI</Link>
          <Link to="/image-api">ImageAPI</Link>
          <Link to="/badge-api">BadgeAPI</Link>
          <Link to="/pc-builder-api">PcBuilderAPI</Link>
          <Link to="/friend-api">FriendAPI</Link>
          <Link to="/shortform-api">ShortformAPI</Link>
          <Link to="/media-api">MediaAPI</Link>
          <Link to="/news-api">NewsAPI</Link>
          <Link to="/matching-api">MatchingAPI</Link>
          {isLoggedIn ? <Link to="/mypage">My</Link> : <Link to="/signup">Signup</Link>}
          {isLoggedIn ? (
            <button
              type="button"
              className="link-btn"
              onClick={async () => {
                await logout().catch(() => null);
                clearAuth();
                navigate('/');
              }}
            >
              Logout
            </button>
          ) : (
            <Link to="/login">Login</Link>
          )}
        </nav>
      </div>
    </header>
  );
}

export default function App() {
  return (
    <div className="app-shell">
      <Header />
      <main className="main">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/products" element={<ProductsPage />} />
          <Route path="/products/:id" element={<ProductDetailPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route path="/auth/verify-email" element={<VerifyEmailPage />} />
          <Route path="/auth/password-reset" element={<PasswordResetPage />} />
          <Route path="/cart" element={<CartPage />} />
          <Route path="/mypage" element={<MyPage />} />
          <Route path="/user-api" element={<UserApiPage />} />
          <Route path="/category-api" element={<CategoryApiPage />} />
          <Route path="/product-api" element={<ProductApiPage />} />
          <Route path="/spec-api" element={<SpecApiPage />} />
          <Route path="/seller-api" element={<SellerApiPage />} />
          <Route path="/price-api" element={<PriceApiPage />} />
          <Route path="/cart-api" element={<CartApiPage />} />
          <Route path="/address-api" element={<AddressApiPage />} />
          <Route path="/order-api" element={<OrderApiPage />} />
          <Route path="/payment-api" element={<PaymentApiPage />} />
          <Route path="/review-api" element={<ReviewApiPage />} />
          <Route path="/wishlist-api" element={<WishlistApiPage />} />
          <Route path="/point-api" element={<PointApiPage />} />
          <Route path="/community-api" element={<CommunityApiPage />} />
          <Route path="/inquiry-api" element={<InquiryApiPage />} />
          <Route path="/support-api" element={<SupportApiPage />} />
          <Route path="/help-api" element={<HelpApiPage />} />
          <Route path="/activity-api" element={<ActivityApiPage />} />
          <Route path="/chat-api" element={<ChatApiPage />} />
          <Route path="/ranking-api" element={<RankingApiPage />} />
          <Route path="/recommendation-api" element={<RecommendationApiPage />} />
          <Route path="/deal-api" element={<DealApiPage />} />
          <Route path="/prediction-api" element={<PredictionApiPage />} />
          <Route path="/fraud-api" element={<FraudApiPage />} />
          <Route path="/trust-api" element={<TrustApiPage />} />
          <Route path="/i18n-api" element={<I18nApiPage />} />
          <Route path="/image-api" element={<ImageApiPage />} />
          <Route path="/badge-api" element={<BadgeApiPage />} />
          <Route path="/pc-builder-api" element={<PcBuilderApiPage />} />
          <Route path="/friend-api" element={<FriendApiPage />} />
          <Route path="/shortform-api" element={<ShortformApiPage />} />
          <Route path="/media-api" element={<MediaApiPage />} />
          <Route path="/news-api" element={<NewsApiPage />} />
          <Route path="/matching-api" element={<MatchingApiPage />} />
        </Routes>
      </main>
    </div>
  );
}






