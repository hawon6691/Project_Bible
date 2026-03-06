import { useState } from 'react';
import { fetchSearchSyncOutboxSummaryAdmin, requeueSearchSyncFailedAdmin } from '@/lib/endpoints';
import type { SearchSyncOutboxSummary } from '@/lib/types';

export default function SearchSyncApiPage() {
  const [limit, setLimit] = useState(100);
  const [summary, setSummary] = useState<SearchSyncOutboxSummary | null>(null);
  const [result, setResult] = useState<unknown>(null);
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
      <h1>Search Sync API Step</h1>
      <p className="sub">remaining backend module - search index outbox integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>Outbox Summary</h2>
          <div className="button-row">
            <button
              type="button"
              onClick={() =>
                run(async () => {
                  const res = await fetchSearchSyncOutboxSummaryAdmin();
                  setSummary(res.data);
                  setMessage('GET /search/admin/index/outbox/summary success');
                })
              }
            >
              load summary
            </button>
          </div>

          {summary ? (
            <ul className="list mt-12">
              <li>pending: {summary.pending}</li>
              <li>processing: {summary.processing}</li>
              <li>completed: {summary.completed}</li>
              <li>failed: {summary.failed}</li>
            </ul>
          ) : null}
        </div>

        <div className="panel">
          <h2>Requeue Failed</h2>
          <div className="form-box">
            <label htmlFor="search-sync-limit">limit</label>
            <input
              id="search-sync-limit"
              type="number"
              min={1}
              value={limit}
              onChange={(e) => setLimit(Number(e.target.value))}
            />
            <button
              type="button"
              onClick={() =>
                run(async () => {
                  const res = await requeueSearchSyncFailedAdmin(limit);
                  setResult(res.data);
                  setMessage('POST /search/admin/index/outbox/requeue-failed success');
                })
              }
            >
              requeue failed
            </button>
          </div>
        </div>
      </div>

      {result ? <pre className="code-view mt-12">{JSON.stringify(result, null, 2)}</pre> : null}
    </section>
  );
}
