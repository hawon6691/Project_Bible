import { FormEvent, useState } from 'react';
import {
  fetchTrustCurrentScore,
  fetchTrustHistory,
  recalculateTrustScoreAdmin,
} from '@/lib/endpoints';
import type { TrustCurrentScore, TrustHistoryItem } from '@/lib/types';

export default function TrustApiPage() {
  const [sellerId, setSellerId] = useState('');
  const [historyLimit, setHistoryLimit] = useState('20');

  const [currentScore, setCurrentScore] = useState<TrustCurrentScore | null>(null);
  const [history, setHistory] = useState<TrustHistoryItem[]>([]);
  const [recalculateResult, setRecalculateResult] = useState<unknown>(null);

  const [deliveryAccuracy, setDeliveryAccuracy] = useState('90');
  const [priceAccuracy, setPriceAccuracy] = useState('90');
  const [customerRating, setCustomerRating] = useState('90');
  const [responseSpeed, setResponseSpeed] = useState('90');
  const [returnRate, setReturnRate] = useState('5');

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
      <h1>Trust API Step</h1>
      <p className="sub">next step - trust API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>Public Trust APIs</h2>
          <form
            className="form-box"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await fetchTrustCurrentScore(Number(sellerId));
                setCurrentScore(res.data);
                setMessage('GET /trust/sellers/:sellerId success');
              });
            }}
          >
            <label htmlFor="trust-seller-id">sellerId</label>
            <input
              id="trust-seller-id"
              value={sellerId}
              onChange={(e) => setSellerId(e.target.value)}
              required
            />
            <button type="submit">load current trust score</button>
          </form>

          <form
            className="form-box mt-12"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await fetchTrustHistory(Number(sellerId), Number(historyLimit));
                setHistory(res.data);
                setMessage('GET /trust/sellers/:sellerId/history success');
              });
            }}
          >
            <label htmlFor="trust-history-limit">history limit</label>
            <input
              id="trust-history-limit"
              value={historyLimit}
              onChange={(e) => setHistoryLimit(e.target.value)}
              required
            />
            <button type="submit">load trust history</button>
          </form>

          {currentScore ? (
            <pre className="code-view mt-12">{JSON.stringify(currentScore, null, 2)}</pre>
          ) : null}
          {history.length ? (
            <pre className="code-view mt-12">{JSON.stringify(history, null, 2)}</pre>
          ) : null}
        </div>

        <div className="panel">
          <h2>POST /trust/admin/sellers/:sellerId/recalculate</h2>
          <form
            className="form-box"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await recalculateTrustScoreAdmin(Number(sellerId), {
                  deliveryAccuracy: Number(deliveryAccuracy),
                  priceAccuracy: Number(priceAccuracy),
                  customerRating: Number(customerRating),
                  responseSpeed: Number(responseSpeed),
                  returnRate: Number(returnRate),
                });
                setRecalculateResult(res.data);
                setMessage('POST /trust/admin/sellers/:sellerId/recalculate success');
              });
            }}
          >
            <label htmlFor="trust-delivery-accuracy">deliveryAccuracy</label>
            <input
              id="trust-delivery-accuracy"
              value={deliveryAccuracy}
              onChange={(e) => setDeliveryAccuracy(e.target.value)}
              required
            />

            <label htmlFor="trust-price-accuracy">priceAccuracy</label>
            <input
              id="trust-price-accuracy"
              value={priceAccuracy}
              onChange={(e) => setPriceAccuracy(e.target.value)}
              required
            />

            <label htmlFor="trust-customer-rating">customerRating</label>
            <input
              id="trust-customer-rating"
              value={customerRating}
              onChange={(e) => setCustomerRating(e.target.value)}
              required
            />

            <label htmlFor="trust-response-speed">responseSpeed</label>
            <input
              id="trust-response-speed"
              value={responseSpeed}
              onChange={(e) => setResponseSpeed(e.target.value)}
              required
            />

            <label htmlFor="trust-return-rate">returnRate</label>
            <input
              id="trust-return-rate"
              value={returnRate}
              onChange={(e) => setReturnRate(e.target.value)}
              required
            />

            <button type="submit">recalculate trust score</button>
          </form>

          {recalculateResult ? (
            <pre className="code-view mt-12">{JSON.stringify(recalculateResult, null, 2)}</pre>
          ) : (
            <p className="sub mt-12">재산정 결과가 여기에 표시됩니다.</p>
          )}
        </div>
      </div>
    </section>
  );
}
