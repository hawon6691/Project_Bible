import { FormEvent, useState } from 'react';
import {
  fetchLowestEverAnalytics,
  fetchUnitPriceAnalytics,
} from '@/lib/endpoints';

export default function PriceAnalyticsApiPage() {
  const [productId, setProductId] = useState('');
  const [lowestEver, setLowestEver] = useState<unknown>(null);
  const [unitPrice, setUnitPrice] = useState<unknown>(null);
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
      <h1>Price Analytics API Step</h1>
      <p className="sub">step 40 - price analytics API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel">
        <h2>GET /analytics/products/:id/lowest-ever, /unit-price</h2>
        <form
          className="form-box"
          onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const id = Number(productId);
              const [lowestRes, unitRes] = await Promise.all([
                fetchLowestEverAnalytics(id),
                fetchUnitPriceAnalytics(id),
              ]);
              setLowestEver(lowestRes.data);
              setUnitPrice(unitRes.data);
              setMessage('price analytics APIs success');
            });
          }}
        >
          <label htmlFor="analytics-product-id">productId</label>
          <input
            id="analytics-product-id"
            value={productId}
            onChange={(e) => setProductId(e.target.value)}
            required
          />
          <button type="submit">load analytics</button>
        </form>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>Lowest Ever</h2>
          {lowestEver ? (
            <pre className="code-view">{JSON.stringify(lowestEver, null, 2)}</pre>
          ) : (
            <p className="sub">결과가 여기에 표시됩니다.</p>
          )}
        </div>
        <div className="panel">
          <h2>Unit Price</h2>
          {unitPrice ? (
            <pre className="code-view">{JSON.stringify(unitPrice, null, 2)}</pre>
          ) : (
            <p className="sub">결과가 여기에 표시됩니다.</p>
          )}
        </div>
      </div>
    </section>
  );
}
