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
      setTimeout(() => navigate(`/auth/verify-email?email=${encodeURIComponent(email)}`), 900);
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'signup failed';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="form-page">
      <h1>Signup API</h1>
      <form className="form-box" onSubmit={onSubmit}>
        <label htmlFor="signupEmail">email</label>
        <input id="signupEmail" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />

        <label htmlFor="signupPassword">password</label>
        <input id="signupPassword" type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />

        <label htmlFor="signupName">name</label>
        <input id="signupName" value={name} onChange={(e) => setName(e.target.value)} required />

        <label htmlFor="signupPhone">phone</label>
        <input id="signupPhone" value={phone} onChange={(e) => setPhone(e.target.value)} required />

        <button type="submit" disabled={loading}>{loading ? 'loading...' : 'Signup API'}</button>

        <div className="auth-links">
          <Link to="/auth/verify-email">이메일 인증</Link>
          <Link to="/login">로그인</Link>
        </div>

        {message ? <p className="sub">{message}</p> : null}
        {error ? <p className="error">{error}</p> : null}
      </form>
    </section>
  );
}
