import { FormEvent, useState } from 'react';
import {
  addSearchHistory,
  clearSearchHistory,
  fetchActivitySummary,
  fetchRecentProducts,
  fetchSearchHistory,
  removeSearchHistory,
  trackRecentProduct,
} from '@/lib/endpoints';

export default function ActivityApiPage() {
  const [summary, setSummary] = useState<any | null>(null);
  const [recentProducts, setRecentProducts] = useState<any[]>([]);
  const [searchHistory, setSearchHistory] = useState<any[]>([]);

  const [recentPage, setRecentPage] = useState('1');
  const [recentLimit, setRecentLimit] = useState('20');
  const [trackProductId, setTrackProductId] = useState('');

  const [keyword, setKeyword] = useState('');
  const [removeSearchId, setRemoveSearchId] = useState('');

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
      <h1>Activity API Step</h1>
      <p className="sub">spec step 19 - activity API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>GET /activities</h2>
          <div className="button-row">
            <button
              type="button"
              onClick={() => run(async () => {
                const res = await fetchActivitySummary();
                setSummary(res.data);
                setMessage('GET /activities success');
              })}
            >
              load summary
            </button>
          </div>

          {summary ? (
            <pre className="code-view mt-12">{JSON.stringify(summary, null, 2)}</pre>
          ) : null}
        </div>

        <div className="panel">
          <h2>Recent Products</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await fetchRecentProducts({
                page: Number(recentPage),
                limit: Number(recentLimit),
              });
              const data = res.data as any;
              setRecentProducts(Array.isArray(data) ? data : (data?.items ?? []));
              setMessage('GET /activities/recent-products success');
            });
          }}>
            <label htmlFor="recentPage">page</label>
            <input id="recentPage" value={recentPage} onChange={(e) => setRecentPage(e.target.value)} required />
            <label htmlFor="recentLimit">limit</label>
            <input id="recentLimit" value={recentLimit} onChange={(e) => setRecentLimit(e.target.value)} required />
            <button type="submit">load recent products</button>
          </form>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await trackRecentProduct(Number(trackProductId));
              setMessage('POST /activities/recent-products/:productId success');
            });
          }}>
            <label htmlFor="trackProductId">productId</label>
            <input id="trackProductId" value={trackProductId} onChange={(e) => setTrackProductId(e.target.value)} required />
            <button type="submit">track recent product</button>
          </form>

          <ul className="list mt-12">
            {recentProducts.map((item) => (
              <li key={item.id}>
                {item.id} / {item.product?.id} / {item.product?.name || '-'} / viewedAt {item.viewedAt}
              </li>
            ))}
          </ul>
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>Search History</h2>
          <div className="button-row">
            <button
              type="button"
              onClick={() => run(async () => {
                const res = await fetchSearchHistory({ page: 1, limit: 30 });
                const data = res.data as any;
                setSearchHistory(Array.isArray(data) ? data : (data?.items ?? []));
                setMessage('GET /activities/searches success');
              })}
            >
              load search history
            </button>
            <button
              type="button"
              onClick={() => run(async () => {
                await clearSearchHistory();
                setMessage('DELETE /activities/searches success');
                setSearchHistory([]);
              })}
            >
              clear searches
            </button>
          </div>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await addSearchHistory(keyword);
              setMessage('POST /activities/searches success');
            });
          }}>
            <label htmlFor="keyword">keyword</label>
            <input id="keyword" value={keyword} onChange={(e) => setKeyword(e.target.value)} required />
            <button type="submit">add search history</button>
          </form>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await removeSearchHistory(Number(removeSearchId));
              setMessage('DELETE /activities/searches/:id success');
            });
          }}>
            <label htmlFor="removeSearchId">searchId</label>
            <input id="removeSearchId" value={removeSearchId} onChange={(e) => setRemoveSearchId(e.target.value)} required />
            <button type="submit">remove search history</button>
          </form>

          <ul className="list mt-12">
            {searchHistory.map((item) => (
              <li key={item.id}>
                {item.id} / {item.keyword} / {item.createdAt}
              </li>
            ))}
          </ul>
        </div>
      </div>
    </section>
  );
}
