import { FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { signup } from '@/lib/endpoints';

export default function SignupPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('010-');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setMessage('');
    setLoading(true);

    try {
      const res = await signup({ email, password, name, phone });
      setMessage(res.data.message || 'signup success');
      setTimeout(() => navigate(`/public/auth/verify-email?email=${encodeURIComponent(email)}`), 900);
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'signup failed';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="danawa-auth-wrap">
      <div className="danawa-auth-card">
        <h1 className="danawa-auth-logo">PBShop 회원가입</h1>
        <p className="text-secondary small mb-3">간편 회원가입 또는 이메일 회원가입을 이용할 수 있어요.</p>

        <div className="danawa-signup-social">
          <button type="button" className="btn social-row kakao">카카오로 가입하기</button>
          <button type="button" className="btn social-row naver">네이버로 가입하기</button>
          <button type="button" className="btn social-row normal">휴대폰으로 가입하기</button>
          <button type="button" className="btn social-row purple">앱카드로 가입하기</button>
        </div>

        <form className="danawa-auth-form mt-3" onSubmit={onSubmit}>
          <input
            id="signupEmail"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="이메일"
            required
          />
          <input
            id="signupPassword"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="비밀번호"
            required
          />
          <input
            id="signupName"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="이름"
            required
          />
          <input
            id="signupPhone"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            placeholder="휴대전화"
            required
          />

          <button type="submit" className="btn btn-primary w-100" disabled={loading}>
            {loading ? '가입 중...' : '이메일로 가입하기'}
          </button>
          <div className="danawa-auth-links">
            <Link to="/public/login">로그인</Link>
            <span>|</span>
            <Link to="/public/auth/verify-email">이메일 인증</Link>
          </div>

          {message ? <p className="sub mb-0">{message}</p> : null}
          {error ? <p className="error mb-0">{error}</p> : null}
        </form>
      </div>
    </section>
  );
}
