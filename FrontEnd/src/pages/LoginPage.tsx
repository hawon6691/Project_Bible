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
      navigate('/mypage');
    } catch (err) {
      const message = err instanceof Error ? err.message : 'login failed';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="form-page">
      <h1>Login API</h1>
      <form onSubmit={handleSubmit} className="form-box">
        <label htmlFor="email">email</label>
        <input id="email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />

        <label htmlFor="password">password</label>
        <input id="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />

        <button type="submit" disabled={loading}>{loading ? 'loading...' : 'Login API'}</button>

        <div className="auth-links">
          <Link to="/signup">회원가입</Link>
          <Link to="/auth/password-reset">비밀번호 재설정</Link>
        </div>

        {error ? <p className="error">{error}</p> : null}
      </form>
    </section>
  );
}
