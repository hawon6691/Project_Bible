import { FormEvent, useState } from 'react';
import {
  approveMapping,
  autoMatchMappings,
  fetchMappingStats,
  fetchPendingMappings,
  rejectMapping,
} from '@/lib/endpoints';
import type { ProductMappingItem } from '@/lib/types';

export default function MatchingApiPage() {
  const [mappings, setMappings] = useState<ProductMappingItem[]>([]);
  const [stats, setStats] = useState<{ pending: number; approved: number; rejected: number; total: number } | null>(null);
  const [result, setResult] = useState<unknown>(null);
  const [mappingId, setMappingId] = useState('');
  const [productId, setProductId] = useState('');
  const [reason, setReason] = useState('매칭 근거 부족');
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
      <h1>Matching API Step</h1>
      <p className="sub">step 38 - product matching API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>List / Stats / Auto-match</h2>
          <div className="button-row">
            <button type="button" onClick={() => run(async () => {
              const res = await fetchPendingMappings();
              setMappings(res.data);
              setMessage('GET /matching/pending success');
            })}>load pending mappings</button>
            <button type="button" onClick={() => run(async () => {
              const res = await fetchMappingStats();
              setStats(res.data);
              setMessage('GET /matching/stats success');
            })}>load stats</button>
            <button type="button" onClick={() => run(async () => {
              const res = await autoMatchMappings();
              setResult(res.data);
              setMessage('POST /matching/auto-match success');
            })}>run auto-match</button>
          </div>

          <h3 className="mt-12">Pending</h3>
          <ul className="list">
            {mappings.map((item) => (
              <li key={item.id}>
                #{item.id} / {item.sourceName} / {item.status} / confidence {item.confidence}
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>Approve / Reject</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await approveMapping(Number(mappingId), { productId: Number(productId) });
              setResult(res.data);
              setMessage('PATCH /matching/:id/approve success');
            });
          }}>
            <label htmlFor="map-id">mappingId</label>
            <input id="map-id" value={mappingId} onChange={(e) => setMappingId(e.target.value)} required />
            <label htmlFor="map-product-id">productId</label>
            <input id="map-product-id" value={productId} onChange={(e) => setProductId(e.target.value)} required />
            <button type="submit">approve mapping</button>
          </form>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await rejectMapping(Number(mappingId), { reason });
              setResult(res.data);
              setMessage('PATCH /matching/:id/reject success');
            });
          }}>
            <label htmlFor="map-reason">reason</label>
            <input id="map-reason" value={reason} onChange={(e) => setReason(e.target.value)} required />
            <button type="submit">reject mapping</button>
          </form>
        </div>
      </div>

      {stats ? <pre className="code-view mt-12">{JSON.stringify(stats, null, 2)}</pre> : null}
      {result ? <pre className="code-view mt-12">{JSON.stringify(result, null, 2)}</pre> : null}
    </section>
  );
}
