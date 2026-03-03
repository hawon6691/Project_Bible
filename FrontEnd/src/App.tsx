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
        </Routes>
      </main>
    </div>
  );
}






