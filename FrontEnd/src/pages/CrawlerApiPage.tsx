import { FormEvent, useState } from 'react';
import {
  createCrawlerJobAdmin,
  fetchCrawlerJobsAdmin,
  fetchCrawlerMonitoringAdmin,
  fetchCrawlerRunsAdmin,
  removeCrawlerJobAdmin,
  triggerCrawlerJobAdmin,
  triggerCrawlerManualAdmin,
  updateCrawlerJobAdmin,
} from '@/lib/endpoints';
import type { CrawlerJobListResult, CrawlerMonitoringSummary, CrawlerRunListResult } from '@/lib/types';

export default function CrawlerApiPage() {
  const [page, setPage] = useState(1);
  const [limit, setLimit] = useState(20);
  const [sellerId, setSellerId] = useState('');
  const [isActive, setIsActive] = useState('');
  const [jobId, setJobId] = useState('');
  const [jobName, setJobName] = useState('price-monitor');
  const [cronExpression, setCronExpression] = useState('0 */2 * * *');
  const [collectPrice, setCollectPrice] = useState(true);
  const [collectSpec, setCollectSpec] = useState(true);
  const [detectAnomaly, setDetectAnomaly] = useState(true);
  const [runStatus, setRunStatus] = useState('');
  const [productId, setProductId] = useState('');
  const [jobs, setJobs] = useState<CrawlerJobListResult | null>(null);
  const [runs, setRuns] = useState<CrawlerRunListResult | null>(null);
  const [monitoring, setMonitoring] = useState<CrawlerMonitoringSummary | null>(null);
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
      <h1>Crawler API Step</h1>
      <p className="sub">remaining backend module - crawler admin integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>Jobs</h2>
          <form
            className="form-box"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await fetchCrawlerJobsAdmin({
                  page,
                  limit,
                  ...(sellerId ? { sellerId: Number(sellerId) } : {}),
                  ...(isActive ? { isActive: isActive === 'true' } : {}),
                });
                setJobs(res.data);
                setMessage('GET /crawler/admin/jobs success');
              });
            }}
          >
            <label htmlFor="crawler-page">page</label>
            <input id="crawler-page" type="number" min={1} value={page} onChange={(e) => setPage(Number(e.target.value))} />
            <label htmlFor="crawler-limit">limit</label>
            <input id="crawler-limit" type="number" min={1} max={100} value={limit} onChange={(e) => setLimit(Number(e.target.value))} />
            <label htmlFor="crawler-seller-filter">sellerId</label>
            <input id="crawler-seller-filter" value={sellerId} onChange={(e) => setSellerId(e.target.value)} placeholder="1" />
            <label htmlFor="crawler-active-filter">isActive</label>
            <select id="crawler-active-filter" value={isActive} onChange={(e) => setIsActive(e.target.value)}>
              <option value="">all</option>
              <option value="true">true</option>
              <option value="false">false</option>
            </select>
            <div className="button-row">
              <button type="submit">load jobs</button>
              <button
                type="button"
                onClick={() =>
                  run(async () => {
                    const res = await createCrawlerJobAdmin({
                      sellerId: Number(sellerId || '1'),
                      name: jobName,
                      cronExpression,
                      collectPrice,
                      collectSpec,
                      detectAnomaly,
                      isActive: true,
                    });
                    setResult(res.data);
                    setMessage('POST /crawler/admin/jobs success');
                  })
                }
              >
                create job
              </button>
            </div>

            <label htmlFor="crawler-job-id">job id</label>
            <input id="crawler-job-id" value={jobId} onChange={(e) => setJobId(e.target.value)} placeholder="1" />
            <label htmlFor="crawler-job-name">job name</label>
            <input id="crawler-job-name" value={jobName} onChange={(e) => setJobName(e.target.value)} />
            <label htmlFor="crawler-cron">cronExpression</label>
            <input id="crawler-cron" value={cronExpression} onChange={(e) => setCronExpression(e.target.value)} />
            <label>
              <input type="checkbox" checked={collectPrice} onChange={(e) => setCollectPrice(e.target.checked)} />
              collectPrice
            </label>
            <label>
              <input type="checkbox" checked={collectSpec} onChange={(e) => setCollectSpec(e.target.checked)} />
              collectSpec
            </label>
            <label>
              <input type="checkbox" checked={detectAnomaly} onChange={(e) => setDetectAnomaly(e.target.checked)} />
              detectAnomaly
            </label>
            <div className="button-row">
              <button
                type="button"
                onClick={() =>
                  run(async () => {
                    const res = await updateCrawlerJobAdmin(Number(jobId), {
                      ...(sellerId ? { sellerId: Number(sellerId) } : {}),
                      name: jobName,
                      cronExpression,
                      collectPrice,
                      collectSpec,
                      detectAnomaly,
                    });
                    setResult(res.data);
                    setMessage('PATCH /crawler/admin/jobs/:id success');
                  })
                }
              >
                update job
              </button>
              <button
                type="button"
                onClick={() =>
                  run(async () => {
                    const res = await triggerCrawlerJobAdmin(Number(jobId));
                    setResult(res.data);
                    setMessage('POST /crawler/admin/jobs/:id/run success');
                  })
                }
              >
                trigger job
              </button>
              <button
                type="button"
                onClick={() =>
                  run(async () => {
                    const res = await removeCrawlerJobAdmin(Number(jobId));
                    setResult(res.data);
                    setMessage('DELETE /crawler/admin/jobs/:id success');
                  })
                }
              >
                delete job
              </button>
            </div>
          </form>

          <ul className="list mt-12">
            {(jobs?.items ?? []).map((item) => (
              <li key={item.id}>
                {item.id} / seller {item.sellerId} / {item.name} / active {String(item.isActive)}
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>Runs / Monitoring</h2>
          <form
            className="form-box"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await fetchCrawlerRunsAdmin({
                  page,
                  limit,
                  ...(jobId ? { jobId: Number(jobId) } : {}),
                  ...(sellerId ? { sellerId: Number(sellerId) } : {}),
                  ...(runStatus ? { status: runStatus } : {}),
                });
                setRuns(res.data);
                setMessage('GET /crawler/admin/runs success');
              });
            }}
          >
            <label htmlFor="crawler-run-status">status</label>
            <input id="crawler-run-status" value={runStatus} onChange={(e) => setRunStatus(e.target.value)} placeholder="SUCCESS" />
            <label htmlFor="crawler-product-id">productId</label>
            <input id="crawler-product-id" value={productId} onChange={(e) => setProductId(e.target.value)} placeholder="optional" />
            <div className="button-row">
              <button type="submit">load runs</button>
              <button
                type="button"
                onClick={() =>
                  run(async () => {
                    const res = await fetchCrawlerMonitoringAdmin();
                    setMonitoring(res.data);
                    setMessage('GET /crawler/admin/monitoring success');
                  })
                }
              >
                load monitoring
              </button>
              <button
                type="button"
                onClick={() =>
                  run(async () => {
                    const res = await triggerCrawlerManualAdmin({
                      sellerId: Number(sellerId || '1'),
                      ...(productId ? { productId: Number(productId) } : {}),
                      collectPrice,
                      collectSpec,
                      detectAnomaly,
                    });
                    setResult(res.data);
                    setMessage('POST /crawler/admin/triggers success');
                  })
                }
              >
                trigger manual
              </button>
            </div>
          </form>

          {monitoring ? (
            <ul className="list mt-12">
              <li>totalRuns: {monitoring.totalRuns}</li>
              <li>queuedRuns: {monitoring.queuedRuns}</li>
              <li>processingRuns: {monitoring.processingRuns}</li>
              <li>successRuns: {monitoring.successRuns}</li>
              <li>failedRuns: {monitoring.failedRuns}</li>
              <li>successRate: {monitoring.successRate}</li>
            </ul>
          ) : null}

          <ul className="list mt-12">
            {(runs?.items ?? []).map((item) => (
              <li key={item.id}>
                {item.id} / job {item.jobId ?? '-'} / seller {item.sellerId} / status {item.status}
              </li>
            ))}
          </ul>
        </div>
      </div>

      {result ? <pre className="code-view mt-12">{JSON.stringify(result, null, 2)}</pre> : null}
    </section>
  );
}
