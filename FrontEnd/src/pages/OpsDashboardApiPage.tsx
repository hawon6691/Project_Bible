import { useState } from 'react';
import { fetchOpsDashboardSummaryAdmin } from '@/lib/endpoints';
import type { OpsDashboardSummary } from '@/lib/types';

export default function OpsDashboardApiPage() {
  const [summary, setSummary] = useState<OpsDashboardSummary | null>(null);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const load = async () => {
    setMessage('');
    setError('');
    try {
      const res = await fetchOpsDashboardSummaryAdmin();
      setSummary(res.data);
      setMessage('GET /admin/ops-dashboard/summary success');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'request failed');
    }
  };

  return (
    <section>
      <h1>Ops Dashboard API Step</h1>
      <p className="sub">step 50 - ops dashboard API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel">
        <h2>GET /admin/ops-dashboard/summary</h2>
        <div className="button-row">
          <button type="button" onClick={load}>
            load summary
          </button>
        </div>
        {summary ? (
          <>
            <p className="sub mt-12">
              checkedAt: {summary.checkedAt} / overallStatus: {summary.overallStatus} / alerts: {summary.alertCount}
            </p>
            <ul className="list mt-12">
              {summary.alerts.map((alert, index) => (
                <li key={`${alert.key}-${index}`}>
                  [{alert.severity}] {alert.key} - {alert.message}
                </li>
              ))}
            </ul>
            <pre className="code-view mt-12">{JSON.stringify(summary, null, 2)}</pre>
          </>
        ) : (
          <p className="sub mt-12">조회 결과가 여기에 표시됩니다.</p>
        )}
      </div>
    </section>
  );
}
