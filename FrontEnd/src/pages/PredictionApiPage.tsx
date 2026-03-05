import { FormEvent, useState } from 'react';
import { fetchPricePrediction } from '@/lib/endpoints';
import type { PricePredictionResult } from '@/lib/types';

export default function PredictionApiPage() {
  const [productId, setProductId] = useState('');
  const [horizonDays, setHorizonDays] = useState('7');
  const [lookbackDays, setLookbackDays] = useState('30');
  const [result, setResult] = useState<PricePredictionResult | null>(null);
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
      <h1>Prediction API Step</h1>
      <p className="sub">spec step 24 - prediction API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel">
        <h2>GET /predictions/products/:productId/price-trend</h2>
        <form
          className="form-box"
          onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await fetchPricePrediction(Number(productId), {
                horizonDays: Number(horizonDays),
                lookbackDays: Number(lookbackDays),
              });
              setResult(res.data);
              setMessage('GET /predictions/products/:productId/price-trend success');
            });
          }}
        >
          <label htmlFor="prediction-product-id">productId</label>
          <input
            id="prediction-product-id"
            value={productId}
            onChange={(e) => setProductId(e.target.value)}
            required
          />

          <label htmlFor="prediction-horizon-days">horizonDays (1~30)</label>
          <input
            id="prediction-horizon-days"
            value={horizonDays}
            onChange={(e) => setHorizonDays(e.target.value)}
            required
          />

          <label htmlFor="prediction-lookback-days">lookbackDays (7~180)</label>
          <input
            id="prediction-lookback-days"
            value={lookbackDays}
            onChange={(e) => setLookbackDays(e.target.value)}
            required
          />

          <button type="submit">predict price trend</button>
        </form>

        {result ? (
          <pre className="code-view mt-12">{JSON.stringify(result, null, 2)}</pre>
        ) : (
          <p className="sub mt-12">예측 결과가 여기에 표시됩니다.</p>
        )}
      </div>
    </section>
  );
}
