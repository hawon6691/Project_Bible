import { FormEvent, useState } from 'react';
import {
  createDealAdmin,
  fetchDeals,
  removeDealAdmin,
  updateDealAdmin,
} from '@/lib/endpoints';
import type { DealItem } from '@/lib/types';

export default function DealApiPage() {
  const [limit, setLimit] = useState('20');
  const [deals, setDeals] = useState<DealItem[]>([]);

  const [createProductId, setCreateProductId] = useState('');
  const [createTitle, setCreateTitle] = useState('');
  const [createDescription, setCreateDescription] = useState('');
  const [createDiscountRate, setCreateDiscountRate] = useState('10');
  const [createStartAt, setCreateStartAt] = useState('');
  const [createEndAt, setCreateEndAt] = useState('');
  const [createIsActive, setCreateIsActive] = useState(true);

  const [updateId, setUpdateId] = useState('');
  const [updateTitle, setUpdateTitle] = useState('');
  const [updateDiscountRate, setUpdateDiscountRate] = useState('');
  const [updateIsActive, setUpdateIsActive] = useState('');

  const [deleteId, setDeleteId] = useState('');
  const [lastResult, setLastResult] = useState<unknown>(null);

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
      <h1>Deal API Step</h1>
      <p className="sub">spec step 23 - deal API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>GET /deals</h2>
          <form
            className="form-box"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await fetchDeals(parseLimit());
                setDeals(res.data);
                setMessage('GET /deals success');
              });
            }}
          >
            <label htmlFor="deal-limit">limit</label>
            <input
              id="deal-limit"
              value={limit}
              onChange={(e) => setLimit(e.target.value)}
              required
            />
            <button type="submit">load deals</button>
          </form>
          <ul className="list mt-12">
            {deals.map((item) => (
              <li key={item.id}>
                #{item.id} / {item.title} / product {item.productId} / {item.discountRate}% / active{' '}
                {String(item.isActive)}
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>POST /deals/admin</h2>
          <form
            className="form-box"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await createDealAdmin({
                  productId: Number(createProductId),
                  title: createTitle,
                  description: createDescription || undefined,
                  discountRate: Number(createDiscountRate),
                  startAt: createStartAt,
                  endAt: createEndAt,
                  isActive: createIsActive,
                });
                setLastResult(res.data);
                setMessage('POST /deals/admin success');
              });
            }}
          >
            <label htmlFor="create-product-id">productId</label>
            <input
              id="create-product-id"
              value={createProductId}
              onChange={(e) => setCreateProductId(e.target.value)}
              required
            />
            <label htmlFor="create-title">title</label>
            <input
              id="create-title"
              value={createTitle}
              onChange={(e) => setCreateTitle(e.target.value)}
              required
            />
            <label htmlFor="create-description">description</label>
            <input
              id="create-description"
              value={createDescription}
              onChange={(e) => setCreateDescription(e.target.value)}
            />
            <label htmlFor="create-discount-rate">discountRate</label>
            <input
              id="create-discount-rate"
              value={createDiscountRate}
              onChange={(e) => setCreateDiscountRate(e.target.value)}
              required
            />
            <label htmlFor="create-start-at">startAt (ISO)</label>
            <input
              id="create-start-at"
              value={createStartAt}
              onChange={(e) => setCreateStartAt(e.target.value)}
              placeholder="2026-03-05T00:00:00.000Z"
              required
            />
            <label htmlFor="create-end-at">endAt (ISO)</label>
            <input
              id="create-end-at"
              value={createEndAt}
              onChange={(e) => setCreateEndAt(e.target.value)}
              placeholder="2026-03-06T00:00:00.000Z"
              required
            />
            <label htmlFor="create-active">isActive (true/false)</label>
            <input
              id="create-active"
              value={String(createIsActive)}
              onChange={(e) => setCreateIsActive(e.target.value.toLowerCase() !== 'false')}
              required
            />
            <button type="submit">create deal</button>
          </form>
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>PATCH /deals/admin/:id</h2>
          <form
            className="form-box"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const payload: {
                  title?: string;
                  discountRate?: number;
                  isActive?: boolean;
                } = {};

                if (updateTitle.trim()) payload.title = updateTitle.trim();
                if (updateDiscountRate.trim()) payload.discountRate = Number(updateDiscountRate);
                if (updateIsActive.trim()) payload.isActive = updateIsActive.toLowerCase() === 'true';

                const res = await updateDealAdmin(Number(updateId), payload);
                setLastResult(res.data);
                setMessage('PATCH /deals/admin/:id success');
              });
            }}
          >
            <label htmlFor="update-id">dealId</label>
            <input
              id="update-id"
              value={updateId}
              onChange={(e) => setUpdateId(e.target.value)}
              required
            />
            <label htmlFor="update-title">title (optional)</label>
            <input
              id="update-title"
              value={updateTitle}
              onChange={(e) => setUpdateTitle(e.target.value)}
            />
            <label htmlFor="update-discount-rate">discountRate (optional)</label>
            <input
              id="update-discount-rate"
              value={updateDiscountRate}
              onChange={(e) => setUpdateDiscountRate(e.target.value)}
            />
            <label htmlFor="update-active">isActive (optional true/false)</label>
            <input
              id="update-active"
              value={updateIsActive}
              onChange={(e) => setUpdateIsActive(e.target.value)}
            />
            <button type="submit">update deal</button>
          </form>
        </div>

        <div className="panel">
          <h2>DELETE /deals/admin/:id</h2>
          <form
            className="form-box"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await removeDealAdmin(Number(deleteId));
                setLastResult(res.data);
                setMessage('DELETE /deals/admin/:id success');
              });
            }}
          >
            <label htmlFor="delete-id">dealId</label>
            <input
              id="delete-id"
              value={deleteId}
              onChange={(e) => setDeleteId(e.target.value)}
              required
            />
            <button type="submit">delete deal</button>
          </form>

          {lastResult ? (
            <pre className="code-view mt-12">{JSON.stringify(lastResult, null, 2)}</pre>
          ) : (
            <p className="sub mt-12">요청 결과가 여기에 표시됩니다.</p>
          )}
        </div>
      </div>
    </section>
  );
}
