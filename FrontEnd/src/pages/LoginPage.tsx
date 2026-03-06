import { FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { login } from '@/lib/endpoints';
import { setAuthTokens } from '@/lib/auth';

export default function LoginPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const res = await login({ email, password });
      setAuthTokens(res.data.accessToken, res.data.refreshToken);
      navigate('/public/mypage');
    } catch (err) {
      const message = err instanceof Error ? err.message : 'login failed';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="danawa-auth-wrap">
      <div className="danawa-auth-card">
        <h1 className="danawa-auth-logo">PBShop</h1>

        <form onSubmit={handleSubmit} className="danawa-auth-form">
          <div className="d-flex justify-content-between align-items-center small text-secondary mb-2">
            <label className="d-flex align-items-center gap-1">
              <input type="checkbox" />
              로그인 상태 유지
            </label>
            <button type="button" className="btn btn-light btn-sm">비로그인 주문조회</button>
          </div>

          <input
            id="email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="아이디/이메일"
            required
          />
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="비밀번호"
            required
          />

          <button type="submit" className="btn btn-primary w-100" disabled={loading}>
            {loading ? '로그인 중...' : '로그인'}
          </button>

          <div className="danawa-auth-links">
            <Link to="/public/auth/password-reset">비밀번호 찾기</Link>
            <span>|</span>
            <Link to="/public/signup">회원가입</Link>
          </div>

          {error ? <p className="error mb-0">{error}</p> : null}
        </form>

        <div className="danawa-social-login">
          <button type="button" className="social naver">N</button>
          <button type="button" className="social kakao">K</button>
          <button type="button" className="social facebook">f</button>
          <button type="button" className="social normal">e</button>
        </div>
      </div>
    </section>
  );
}
