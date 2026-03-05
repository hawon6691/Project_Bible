import { FormEvent, useState } from 'react';
import {
  approveFraudAlertAdmin,
  detectFraudAnomalies,
  fetchEffectivePrices,
  fetchFraudAlertsAdmin,
  fetchFraudFlagsAdmin,
  fetchProductRealPrice,
  rejectFraudAlertAdmin,
  scanFraudAnomaliesAdmin,
} from '@/lib/endpoints';
import type { FraudAlertItem } from '@/lib/types';

export default function FraudApiPage() {
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const [productId, setProductId] = useState('');
  const [sellerId, setSellerId] = useState('');
  const [realPrice, setRealPrice] = useState<unknown>(null);
  const [effectivePrices, setEffectivePrices] = useState<unknown>(null);
  const [anomalies, setAnomalies] = useState<unknown>(null);
  const [scanResult, setScanResult] = useState<unknown>(null);
  const [flags, setFlags] = useState<FraudAlertItem[]>([]);

  const [page, setPage] = useState('1');
  const [limit, setLimit] = useState('20');
  const [status, setStatus] = useState('');
  const [alerts, setAlerts] = useState<FraudAlertItem[]>([]);
  const [alertId, setAlertId] = useState('');
  const [alertActionResult, setAlertActionResult] = useState<unknown>(null);

  const run = async (fn: () => Promise<void>) => {
    setMessage('');
    setError('');
    try {
      await fn();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'request failed');
    }
  };

  const parsedProductId = Number(productId);

  return (
    <section>
      <h1>Fraud API Step</h1>
      <p className="sub">next step - fraud API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>Public Fraud APIs</h2>
          <form
            className="form-box"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const realRes = await fetchProductRealPrice(
                  parsedProductId,
                  sellerId.trim() ? Number(sellerId) : undefined,
                );
                setRealPrice(realRes.data);
                setMessage('GET /products/:id/real-price success');
              });
            }}
          >
            <label htmlFor="fraud-product-id">productId</label>
            <input
              id="fraud-product-id"
              value={productId}
              onChange={(e) => setProductId(e.target.value)}
              required
            />
            <label htmlFor="fraud-seller-id">sellerId (optional)</label>
            <input
              id="fraud-seller-id"
              value={sellerId}
              onChange={(e) => setSellerId(e.target.value)}
            />
            <button type="submit">load real price</button>
          </form>

          <div className="button-row">
            <button
              type="button"
              onClick={() =>
                run(async () => {
                  const res = await fetchEffectivePrices(parsedProductId);
                  setEffectivePrices(res.data);
                  setMessage('GET /fraud/products/:productId/effective-prices success');
                })
              }
            >
              load effective prices
            </button>
            <button
              type="button"
              onClick={() =>
                run(async () => {
                  const res = await detectFraudAnomalies(parsedProductId);
                  setAnomalies(res.data);
                  setMessage('GET /fraud/products/:productId/anomalies success');
                })
              }
            >
              detect anomalies
            </button>
          </div>

          {realPrice ? <pre className="code-view mt-12">{JSON.stringify(realPrice, null, 2)}</pre> : null}
          {effectivePrices ? (
            <pre className="code-view mt-12">{JSON.stringify(effectivePrices, null, 2)}</pre>
          ) : null}
          {anomalies ? <pre className="code-view mt-12">{JSON.stringify(anomalies, null, 2)}</pre> : null}
        </div>

        <div className="panel">
          <h2>Admin Fraud APIs</h2>
          <form
            className="form-box"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await fetchFraudAlertsAdmin({
                  page: Number(page),
                  limit: Number(limit),
                  status: status.trim()
                    ? (status.trim().toUpperCase() as 'PENDING' | 'APPROVED' | 'REJECTED')
                    : undefined,
                });
                setAlerts(res.data);
                setMessage('GET /fraud/alerts success');
              });
            }}
          >
            <label htmlFor="fraud-page">page</label>
            <input id="fraud-page" value={page} onChange={(e) => setPage(e.target.value)} />
            <label htmlFor="fraud-limit">limit</label>
            <input id="fraud-limit" value={limit} onChange={(e) => setLimit(e.target.value)} />
            <label htmlFor="fraud-status">status (optional)</label>
            <input
              id="fraud-status"
              value={status}
              onChange={(e) => setStatus(e.target.value)}
              placeholder="PENDING | APPROVED | REJECTED"
            />
            <button type="submit">load alerts</button>
          </form>

          <div className="button-row">
            <button
              type="button"
              onClick={() =>
                run(async () => {
                  const res = await scanFraudAnomaliesAdmin(parsedProductId);
                  setScanResult(res.data);
                  setMessage('POST /fraud/admin/products/:productId/scan success');
                })
              }
            >
              scan + persist anomalies
            </button>
            <button
              type="button"
              onClick={() =>
                run(async () => {
                  const res = await fetchFraudFlagsAdmin(parsedProductId);
                  setFlags(res.data);
                  setMessage('GET /fraud/admin/products/:productId/flags success');
                })
              }
            >
              load saved flags
            </button>
          </div>

          <form
            className="form-box mt-12"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await approveFraudAlertAdmin(Number(alertId));
                setAlertActionResult(res.data);
                setMessage('PATCH /fraud/alerts/:id/approve success');
              });
            }}
          >
            <label htmlFor="fraud-alert-id">alertId</label>
            <input
              id="fraud-alert-id"
              value={alertId}
              onChange={(e) => setAlertId(e.target.value)}
              required
            />
            <div className="button-row">
              <button type="submit">approve alert</button>
              <button
                type="button"
                onClick={() =>
                  run(async () => {
                    const res = await rejectFraudAlertAdmin(Number(alertId));
                    setAlertActionResult(res.data);
                    setMessage('PATCH /fraud/alerts/:id/reject success');
                  })
                }
              >
                reject alert
              </button>
            </div>
          </form>

          <h3 className="mt-12">Alerts</h3>
          <ul className="list">
            {alerts.map((item) => (
              <li key={item.id}>
                #{item.id} / product {item.productId} / {item.severity} / {item.status}
              </li>
            ))}
          </ul>

          {scanResult ? <pre className="code-view mt-12">{JSON.stringify(scanResult, null, 2)}</pre> : null}
          {flags.length ? <pre className="code-view mt-12">{JSON.stringify(flags, null, 2)}</pre> : null}
          {alertActionResult ? (
            <pre className="code-view mt-12">{JSON.stringify(alertActionResult, null, 2)}</pre>
          ) : null}
        </div>
      </div>
    </section>
  );
}
