import { FormEvent, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { resendVerificationEmail, verifyEmail } from '@/lib/endpoints';

export default function VerifyEmailPage() {
  const [params] = useSearchParams();
  const initialEmail = useMemo(() => params.get('email') || '', [params]);

  const [email, setEmail] = useState(initialEmail);
  const [code, setCode] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const onVerify = async (e: FormEvent) => {
    e.preventDefault();
    setMessage('');
    setError('');
    setLoading(true);

    try {
      const res = await verifyEmail({ email, code });
      setMessage(res.data.message || 'verified');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'verify failed');
    } finally {
      setLoading(false);
    }
  };

  const onResend = async () => {
    setMessage('');
    setError('');

    try {
      const res = await resendVerificationEmail(email);
      setMessage(res.data.message || 'resent');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'resend failed');
    }
  };

  return (
    <section className="form-page">
      <h1>Verify Email API</h1>
      <form className="form-box" onSubmit={onVerify}>
        <label htmlFor="verifyEmail">email</label>
        <input id="verifyEmail" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />

        <label htmlFor="verifyCode">code (6 digits)</label>
        <input id="verifyCode" value={code} onChange={(e) => setCode(e.target.value)} required minLength={6} maxLength={6} />

        <button type="submit" disabled={loading}>{loading ? 'loading...' : 'Verify API'}</button>
        <button type="button" onClick={onResend}>Resend Verification API</button>

        {message ? <p className="sub">{message}</p> : null}
        {error ? <p className="error">{error}</p> : null}
      </form>
    </section>
  );
}
