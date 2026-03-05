import { FormEvent, useState } from 'react';
import {
  fetchObservabilityDashboardAdmin,
  fetchObservabilityMetricsAdmin,
  fetchObservabilityTracesAdmin,
} from '@/lib/endpoints';
import type {
  ObservabilityDashboard,
  ObservabilityMetricsSummary,
  ObservabilityTraceItem,
} from '@/lib/types';

export default function ObservabilityApiPage() {
  const [limit, setLimit] = useState(50);
  const [pathContains, setPathContains] = useState('');
  const [metrics, setMetrics] = useState<ObservabilityMetricsSummary | null>(null);
  const [traces, setTraces] = useState<ObservabilityTraceItem[]>([]);
  const [dashboard, setDashboard] = useState<ObservabilityDashboard | null>(null);
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
      <h1>Observability API Step</h1>
      <p className="sub">step 51 - observability API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>Metrics / Traces</h2>
          <div className="button-row">
            <button
              type="button"
              onClick={() =>
                run(async () => {
                  const res = await fetchObservabilityMetricsAdmin();
                  setMetrics(res.data);
                  setMessage('GET /admin/observability/metrics success');
                })
              }
            >
              load metrics
            </button>
          </div>

          {metrics ? <pre className="code-view mt-12">{JSON.stringify(metrics, null, 2)}</pre> : null}

          <form
            className="form-box mt-12"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await fetchObservabilityTracesAdmin({ limit, pathContains: pathContains || undefined });
                setTraces(res.data.items);
                setMessage('GET /admin/observability/traces success');
              });
            }}
          >
            <label htmlFor="trace-limit">limit</label>
            <input
              id="trace-limit"
              type="number"
              min={1}
              max={200}
              value={limit}
              onChange={(e) => setLimit(Number(e.target.value))}
            />
            <label htmlFor="trace-path">pathContains</label>
            <input
              id="trace-path"
              value={pathContains}
              onChange={(e) => setPathContains(e.target.value)}
              placeholder="/products"
            />
            <button type="submit">load traces</button>
          </form>

          <ul className="list mt-12">
            {traces.map((item) => (
              <li key={`${item.requestId}-${item.timestamp}`}>
                {item.method} {item.path} / {item.statusCode} / {item.durationMs}ms
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>Dashboard</h2>
          <div className="button-row">
            <button
              type="button"
              onClick={() =>
                run(async () => {
                  const res = await fetchObservabilityDashboardAdmin();
                  setDashboard(res.data);
                  setMessage('GET /admin/observability/dashboard success');
                })
              }
            >
              load dashboard
            </button>
          </div>

          {dashboard ? (
            <pre className="code-view mt-12">{JSON.stringify(dashboard, null, 2)}</pre>
          ) : (
            <p className="sub mt-12">조회 결과가 여기에 표시됩니다.</p>
          )}
        </div>
      </div>
    </section>
  );
}
