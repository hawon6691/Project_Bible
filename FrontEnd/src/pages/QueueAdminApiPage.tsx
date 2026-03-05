import { FormEvent, useState } from 'react';
import {
  autoRetryQueuesAdmin,
  fetchQueueFailedJobsAdmin,
  fetchQueueStatsAdmin,
  fetchSupportedQueuesAdmin,
  removeQueueJobAdmin,
  retryQueueFailedJobsAdmin,
  retryQueueJobAdmin,
} from '@/lib/endpoints';
import type { QueueFailedJobsResult, QueueStatsResult } from '@/lib/types';

export default function QueueAdminApiPage() {
  const [queueName, setQueueName] = useState('');
  const [page, setPage] = useState(1);
  const [limit, setLimit] = useState(20);
  const [newestFirst, setNewestFirst] = useState(true);
  const [jobId, setJobId] = useState('');
  const [supported, setSupported] = useState<string[]>([]);
  const [stats, setStats] = useState<QueueStatsResult | null>(null);
  const [failed, setFailed] = useState<QueueFailedJobsResult | null>(null);
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
      <h1>Queue Admin API Step</h1>
      <p className="sub">step 49 - queue admin API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>Supported / Stats / Auto Retry</h2>
          <div className="button-row">
            <button
              type="button"
              onClick={() =>
                run(async () => {
                  const res = await fetchSupportedQueuesAdmin();
                  setSupported(res.data.items);
                  setMessage('GET /admin/queues/supported success');
                })
              }
            >
              load supported
            </button>
            <button
              type="button"
              onClick={() =>
                run(async () => {
                  const res = await fetchQueueStatsAdmin();
                  setStats(res.data);
                  setMessage('GET /admin/queues/stats success');
                })
              }
            >
              load stats
            </button>
            <button
              type="button"
              onClick={() =>
                run(async () => {
                  const res = await autoRetryQueuesAdmin({ perQueueLimit: 20, maxTotal: 100 });
                  setResult(res.data);
                  setMessage('POST /admin/queues/auto-retry success');
                })
              }
            >
              auto retry
            </button>
          </div>
          <ul className="list mt-12">
            {supported.map((name) => (
              <li key={name}>{name}</li>
            ))}
          </ul>
          <ul className="list mt-12">
            {(stats?.items ?? []).map((item) => (
              <li key={item.queueName}>
                {item.queueName} / failed {item.counts.failed} / active {item.counts.active} / paused{' '}
                {String(item.paused)}
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>Failed / Retry / Remove</h2>
          <form
            className="form-box"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await fetchQueueFailedJobsAdmin(queueName, { page, limit, newestFirst });
                setFailed(res.data);
                setMessage('GET /admin/queues/:queueName/failed success');
              });
            }}
          >
            <label htmlFor="queue-name">queue name</label>
            <input
              id="queue-name"
              value={queueName}
              onChange={(e) => setQueueName(e.target.value)}
              placeholder="activity-log"
              required
            />
            <label htmlFor="queue-page">page</label>
            <input
              id="queue-page"
              type="number"
              min={1}
              value={page}
              onChange={(e) => setPage(Number(e.target.value))}
            />
            <label htmlFor="queue-limit">limit</label>
            <input
              id="queue-limit"
              type="number"
              min={1}
              max={100}
              value={limit}
              onChange={(e) => setLimit(Number(e.target.value))}
            />
            <label>
              <input
                type="checkbox"
                checked={newestFirst}
                onChange={(e) => setNewestFirst(e.target.checked)}
              />
              newest first
            </label>
            <div className="button-row">
              <button type="submit">load failed jobs</button>
              <button
                type="button"
                onClick={() =>
                  run(async () => {
                    const res = await retryQueueFailedJobsAdmin(queueName, 50);
                    setResult(res.data);
                    setMessage('POST /admin/queues/:queueName/failed/retry success');
                  })
                }
              >
                retry failed batch
              </button>
            </div>

            <label htmlFor="queue-job-id">job id</label>
            <input
              id="queue-job-id"
              value={jobId}
              onChange={(e) => setJobId(e.target.value)}
              placeholder="12345"
            />
            <div className="button-row">
              <button
                type="button"
                onClick={() =>
                  run(async () => {
                    const res = await retryQueueJobAdmin(queueName, jobId);
                    setResult(res.data);
                    setMessage('POST /admin/queues/:queueName/jobs/:jobId/retry success');
                  })
                }
              >
                retry one job
              </button>
              <button
                type="button"
                onClick={() =>
                  run(async () => {
                    const res = await removeQueueJobAdmin(queueName, jobId);
                    setResult(res.data);
                    setMessage('DELETE /admin/queues/:queueName/jobs/:jobId success');
                  })
                }
              >
                remove one job
              </button>
            </div>
          </form>

          <ul className="list mt-12">
            {(failed?.items ?? []).map((item) => (
              <li key={item.id}>
                {item.id} / {item.name} / attempts {item.attemptsMade} / reason {item.failedReason ?? '-'}
              </li>
            ))}
          </ul>
          {failed?.meta ? (
            <p className="sub mt-12">
              page {failed.meta.currentPage} / {failed.meta.totalPages}, total {failed.meta.totalItems}
            </p>
          ) : null}
        </div>
      </div>

      {result ? <pre className="code-view mt-12">{JSON.stringify(result, null, 2)}</pre> : null}
    </section>
  );
}
