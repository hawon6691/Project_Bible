import { FormEvent, useState } from 'react';
import {
  fetchRecommendationPersonal,
  fetchRecommendationTrending,
} from '@/lib/endpoints';
import type { RecommendationItem } from '@/lib/types';

export default function RecommendationApiPage() {
  const [limit, setLimit] = useState('20');
  const [trending, setTrending] = useState<RecommendationItem[]>([]);
  const [personal, setPersonal] = useState<RecommendationItem[]>([]);
  const [trendingSource, setTrendingSource] = useState('');
  const [personalSource, setPersonalSource] = useState('');
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

  const parseLimit = () => {
    const n = Number(limit);
    if (!Number.isFinite(n) || n < 1) return 20;
    return n;
  };

  return (
    <section>
      <h1>Recommendation API Step</h1>
      <p className="sub">spec step 22 - recommendation API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>GET /recommendations/trending</h2>
          <form
            className="form-box"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await fetchRecommendationTrending(parseLimit());
                setTrending(res.data.items || []);
                setTrendingSource(res.data.source || '');
                setMessage('GET /recommendations/trending success');
              });
            }}
          >
            <label htmlFor="recommend-trending-limit">limit</label>
            <input
              id="recommend-trending-limit"
              value={limit}
              onChange={(e) => setLimit(e.target.value)}
              required
            />
            <button type="submit">load trending recommendations</button>
          </form>
          {trendingSource ? <p className="sub mt-12">source: {trendingSource}</p> : null}
          <ul className="list">
            {trending.map((item) => (
              <li key={`trending-${item.productId}`}>
                #{item.rank} / {item.productId} / {item.name} / {item.lowestPrice ?? '-'} KRW
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>GET /recommendations/personal</h2>
          <form
            className="form-box"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await fetchRecommendationPersonal(parseLimit());
                setPersonal(res.data.items || []);
                setPersonalSource(res.data.source || '');
                setMessage('GET /recommendations/personal success');
              });
            }}
          >
            <label htmlFor="recommend-personal-limit">limit</label>
            <input
              id="recommend-personal-limit"
              value={limit}
              onChange={(e) => setLimit(e.target.value)}
              required
            />
            <button type="submit">load personal recommendations</button>
          </form>
          {personalSource ? <p className="sub mt-12">source: {personalSource}</p> : null}
          <ul className="list">
            {personal.map((item) => (
              <li key={`personal-${item.productId}`}>
                #{item.rank} / {item.productId} / {item.name} / rating {item.averageRating}
              </li>
            ))}
          </ul>
        </div>
      </div>
    </section>
  );
}
