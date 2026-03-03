import { FormEvent, useState } from 'react';
import { requestPasswordReset, resetPassword, verifyPasswordResetCode } from '@/lib/endpoints';

export default function PasswordResetPage() {
  const [step, setStep] = useState<1 | 2 | 3>(1);
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('010-');
  const [code, setCode] = useState('');
  const [resetToken, setResetToken] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const onRequest = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setMessage('');
    setError('');

    try {
      const res = await requestPasswordReset({ email, phone });
      setMessage(res.data.message || 'request sent');
      setStep(2);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'request failed');
    } finally {
      setLoading(false);
    }
  };

  const onVerifyCode = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setMessage('');
    setError('');

    try {
      const res = await verifyPasswordResetCode({ email, code });
      setResetToken(res.data.resetToken);
      setMessage('code verified');
      setStep(3);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'verify failed');
    } finally {
      setLoading(false);
    }
  };

  const onReset = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setMessage('');
    setError('');

    try {
      const res = await resetPassword({ resetToken, newPassword });
      setMessage(res.data.message || 'password changed');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'reset failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="form-page">
      <h1>Password Reset API</h1>

      {step === 1 ? (
        <form className="form-box" onSubmit={onRequest}>
          <label htmlFor="resetEmail">email</label>
          <input id="resetEmail" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          <label htmlFor="resetPhone">phone</label>
          <input id="resetPhone" value={phone} onChange={(e) => setPhone(e.target.value)} required />
          <button type="submit" disabled={loading}>{loading ? 'loading...' : 'Request Reset API'}</button>
        </form>
      ) : null}

      {step === 2 ? (
        <form className="form-box" onSubmit={onVerifyCode}>
          <label htmlFor="resetCode">verification code</label>
          <input id="resetCode" value={code} onChange={(e) => setCode(e.target.value)} required minLength={6} maxLength={6} />
          <button type="submit" disabled={loading}>{loading ? 'loading...' : 'Verify Code API'}</button>
        </form>
      ) : null}

      {step === 3 ? (
        <form className="form-box" onSubmit={onReset}>
          <label htmlFor="newPassword">new password</label>
          <input id="newPassword" type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} required />
          <button type="submit" disabled={loading}>{loading ? 'loading...' : 'Confirm Reset API'}</button>
        </form>
      ) : null}

      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}
    </section>
  );
}
