import { FormEvent, useState } from 'react';
import {
  fetchRankingKeywords,
  fetchRankingProducts,
  recalculateRankingAdmin,
} from '@/lib/endpoints';

export default function RankingApiPage() {
  const [limit, setLimit] = useState('20');
  const [products, setProducts] = useState<any[]>([]);
  const [keywords, setKeywords] = useState<any[]>([]);
  const [recalcResult, setRecalcResult] = useState<any | null>(null);

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
      <h1>Ranking API Step</h1>
      <p className="sub">spec step 21 - ranking API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>Public Ranking APIs</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const n = Number(limit);
              const [productRes, keywordRes] = await Promise.all([
                fetchRankingProducts(n),
                fetchRankingKeywords(n),
              ]);
              setProducts(productRes.data as any[]);
              setKeywords(keywordRes.data as any[]);
              setMessage('GET /rankings/products/popular, /rankings/keywords/popular success');
            });
          }}>
            <label htmlFor="limit">limit</label>
            <input id="limit" value={limit} onChange={(e) => setLimit(e.target.value)} required />
            <button type="submit">load rankings</button>
          </form>

          <h3 className="mt-12">Products</h3>
          <ul className="list">
            {products.map((item) => (
              <li key={item.productId}>
                #{item.rank} / {item.productId} / {item.name} / score {Number(item.popularityScore || 0).toLocaleString()}
              </li>
            ))}
          </ul>

          <h3 className="mt-12">Keywords</h3>
          <ul className="list">
            {keywords.map((item, idx) => (
              <li key={`${item.keyword}-${idx}`}>
                #{item.rank} / {item.keyword} / count {item.count}
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>POST /rankings/admin/recalculate</h2>
          <div className="button-row">
            <button
              type="button"
              onClick={() => run(async () => {
                const res = await recalculateRankingAdmin();
                setRecalcResult(res.data);
                setMessage('POST /rankings/admin/recalculate success');
              })}
            >
              recalculate popularity
            </button>
          </div>

          {recalcResult ? (
            <pre className="code-view mt-12">{JSON.stringify(recalcResult, null, 2)}</pre>
          ) : (
            <p className="sub mt-12">관리자 재계산 결과가 여기에 표시됩니다.</p>
          )}
        </div>
      </div>
    </section>
  );
}
