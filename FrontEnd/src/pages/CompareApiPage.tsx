import { FormEvent, useState } from 'react';
import {
  addCompareItem,
  fetchCompareDetail,
  fetchCompareList,
  removeCompareItem,
} from '@/lib/endpoints';
import type { CompareListItem } from '@/lib/types';

export default function CompareApiPage() {
  const [compareKey, setCompareKey] = useState('guest');
  const [productId, setProductId] = useState('');
  const [compareList, setCompareList] = useState<CompareListItem[]>([]);
  const [detail, setDetail] = useState<unknown>(null);
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
      <h1>Compare API Step</h1>
      <p className="sub">step 44 - compare bar API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>Add / Remove / List</h2>
          <form
            className="form-box"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await addCompareItem(Number(productId), compareKey || undefined);
                setCompareList(res.data.compareList);
                setMessage('POST /compare/add success');
              });
            }}
          >
            <label htmlFor="compare-key">x-compare-key</label>
            <input
              id="compare-key"
              value={compareKey}
              onChange={(e) => setCompareKey(e.target.value)}
            />
            <label htmlFor="compare-product-id">productId</label>
            <input
              id="compare-product-id"
              value={productId}
              onChange={(e) => setProductId(e.target.value)}
              required
            />
            <div className="button-row">
              <button type="submit">add compare item</button>
              <button
                type="button"
                onClick={() =>
                  run(async () => {
                    const res = await removeCompareItem(Number(productId), compareKey || undefined);
                    setCompareList(res.data.compareList);
                    setMessage('DELETE /compare/:productId success');
                  })
                }
              >
                remove compare item
              </button>
              <button
                type="button"
                onClick={() =>
                  run(async () => {
                    const res = await fetchCompareList(compareKey || undefined);
                    setCompareList(res.data.compareList);
                    setMessage('GET /compare success');
                  })
                }
              >
                load compare list
              </button>
            </div>
          </form>
        </div>

        <div className="panel">
          <h2>GET /compare/detail</h2>
          <div className="button-row">
            <button
              type="button"
              onClick={() =>
                run(async () => {
                  const res = await fetchCompareDetail(compareKey || undefined);
                  setDetail(res.data);
                  setMessage('GET /compare/detail success');
                })
              }
            >
              load compare detail
            </button>
          </div>
          {detail ? <pre className="code-view mt-12">{JSON.stringify(detail, null, 2)}</pre> : null}
        </div>
      </div>

      <div className="panel mt-12">
        <h2>Compare List</h2>
        <ul className="list">
          {compareList.map((item) => (
            <li key={item.productId}>
              {item.productId} / {item.name} / {item.price} / rating {item.averageRating}
            </li>
          ))}
        </ul>
      </div>
    </section>
  );
}
