import { FormEvent, useState } from 'react';
import {
  fetchResiliencePoliciesAdmin,
  fetchResilienceSnapshotAdmin,
  fetchResilienceSnapshotsAdmin,
  resetResilienceCircuitAdmin,
} from '@/lib/endpoints';
import type { ResilienceCircuitSnapshot, ResiliencePolicyItem } from '@/lib/types';

export default function ResilienceApiPage() {
  const [name, setName] = useState('');
  const [snapshots, setSnapshots] = useState<ResilienceCircuitSnapshot[]>([]);
  const [policies, setPolicies] = useState<ResiliencePolicyItem[]>([]);
  const [result, setResult] = useState<unknown>(null);
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
      <h1>Resilience API Step</h1>
      <p className="sub">step 47 - resilience API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>List / Policies</h2>
          <div className="button-row">
            <button
              type="button"
              onClick={() =>
                run(async () => {
                  const res = await fetchResilienceSnapshotsAdmin();
                  setSnapshots(res.data.items);
                  setMessage('GET /resilience/circuit-breakers success');
                })
              }
            >
              load snapshots
            </button>
            <button
              type="button"
              onClick={() =>
                run(async () => {
                  const res = await fetchResiliencePoliciesAdmin();
                  setPolicies(res.data.items);
                  setMessage('GET /resilience/circuit-breakers/policies success');
                })
              }
            >
              load policies
            </button>
          </div>
          <ul className="list mt-12">
            {snapshots.map((item) => (
              <li key={item.name}>
                {item.name} / {item.status} / fail {item.failureCount} / success {item.successCount}
              </li>
            ))}
          </ul>
          <ul className="list mt-12">
            {policies.map((item) => (
              <li key={item.name}>
                {item.name} / threshold {item.options.failureThreshold} / tuned {item.stats.lastTunedAt}
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>Detail / Reset</h2>
          <form
            className="form-box"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await fetchResilienceSnapshotAdmin(name);
                setResult(res.data);
                setMessage('GET /resilience/circuit-breakers/:name success');
              });
            }}
          >
            <label htmlFor="resilience-name">circuit name</label>
            <input
              id="resilience-name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
            <div className="button-row">
              <button type="submit">load snapshot</button>
              <button
                type="button"
                onClick={() =>
                  run(async () => {
                    const res = await resetResilienceCircuitAdmin(name);
                    setResult(res.data);
                    setMessage('POST /resilience/circuit-breakers/:name/reset success');
                  })
                }
              >
                reset circuit
              </button>
            </div>
          </form>
        </div>
      </div>

      {result ? <pre className="code-view mt-12">{JSON.stringify(result, null, 2)}</pre> : null}
    </section>
  );
}
