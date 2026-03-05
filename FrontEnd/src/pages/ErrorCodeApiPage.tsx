import { FormEvent, useState } from 'react';
import { fetchErrorCode, fetchErrorCodes } from '@/lib/endpoints';
import type { ErrorCodeItem } from '@/lib/types';

export default function ErrorCodeApiPage() {
  const [key, setKey] = useState('');
  const [codes, setCodes] = useState<ErrorCodeItem[]>([]);
  const [result, setResult] = useState<ErrorCodeItem | null>(null);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const run = async (fn: () => Promise<void>) => {
    setMessage('');
    setError('');
    try {
      await fn();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'request failed');
    }
  };

  return (
    <section>
      <h1>Error Code API Step</h1>
      <p className="sub">step 48 - error code catalog API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>GET /errors/codes</h2>
          <div className="button-row">
            <button
              type="button"
              onClick={() =>
                run(async () => {
                  const res = await fetchErrorCodes();
                  setCodes(res.data.items);
                  setMessage('GET /errors/codes success');
                })
              }
            >
              load error codes
            </button>
          </div>
          <ul className="list mt-12">
            {codes.map((item) => (
              <li key={item.key}>
                {item.key} / {item.code} / {item.message}
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>GET /errors/codes/:key</h2>
          <form
            className="form-box"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await fetchErrorCode(key);
                setResult(res.data);
                setMessage('GET /errors/codes/:key success');
              });
            }}
          >
            <label htmlFor="error-code-key">key</label>
            <input
              id="error-code-key"
              value={key}
              onChange={(e) => setKey(e.target.value)}
              placeholder="VALIDATION_FAILED"
              required
            />
            <button type="submit">load code by key</button>
          </form>
          {result ? (
            <pre className="code-view mt-12">{JSON.stringify(result, null, 2)}</pre>
          ) : (
            <p className="sub mt-12">조회 결과가 여기에 표시됩니다.</p>
          )}
        </div>
      </div>
    </section>
  );
}
