import { useState } from 'react';
import { fetchHealthCheck } from '@/lib/endpoints';

export default function HealthApiPage() {
  const [result, setResult] = useState<unknown>(null);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const run = async () => {
    setMessage('');
    setError('');
    try {
      const res = await fetchHealthCheck();
      setResult(res.data);
      setMessage('GET /health success');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'request failed');
    }
  };

  return (
    <section>
      <h1>Health API Step</h1>
      <p className="sub">step 46 - health API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel">
        <h2>GET /health</h2>
        <div className="button-row">
          <button type="button" onClick={run}>check health</button>
        </div>
      </div>

      {result ? <pre className="code-view mt-12">{JSON.stringify(result, null, 2)}</pre> : null}
    </section>
  );
}
