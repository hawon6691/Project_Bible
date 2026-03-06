import { FormEvent, useState } from 'react';
import {
  fetchQueryProductDetail,
  fetchQueryProducts,
  rebuildQueryProductsAdmin,
  syncQueryProductAdmin,
} from '@/lib/endpoints';
import type { ProductQueryViewItem, ProductQueryViewListResult } from '@/lib/types';

export default function QueryApiPage() {
  const [page, setPage] = useState(1);
  const [limit, setLimit] = useState(20);
  const [categoryId, setCategoryId] = useState('');
  const [keyword, setKeyword] = useState('');
  const [minPrice, setMinPrice] = useState('');
  const [maxPrice, setMaxPrice] = useState('');
  const [sort, setSort] = useState<'NEWEST' | 'PRICE_ASC' | 'PRICE_DESC' | 'POPULARITY' | 'RATING'>('NEWEST');
  const [productId, setProductId] = useState('');
  const [list, setList] = useState<ProductQueryViewListResult | null>(null);
  const [detail, setDetail] = useState<ProductQueryViewItem | null>(null);
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
      <h1>Query API Step</h1>
      <p className="sub">remaining backend module - query read model integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>Read Model List / Detail</h2>
          <form
            className="form-box"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await fetchQueryProducts({
                  page,
                  limit,
                  ...(categoryId ? { categoryId: Number(categoryId) } : {}),
                  ...(keyword ? { keyword } : {}),
                  ...(minPrice ? { minPrice: Number(minPrice) } : {}),
                  ...(maxPrice ? { maxPrice: Number(maxPrice) } : {}),
                  sort,
                });
                setList(res.data);
                setMessage('GET /query/products success');
              });
            }}
          >
            <label htmlFor="query-page">page</label>
            <input id="query-page" type="number" min={1} value={page} onChange={(e) => setPage(Number(e.target.value))} />
            <label htmlFor="query-limit">limit</label>
            <input id="query-limit" type="number" min={1} max={100} value={limit} onChange={(e) => setLimit(Number(e.target.value))} />
            <label htmlFor="query-category-id">categoryId</label>
            <input id="query-category-id" value={categoryId} onChange={(e) => setCategoryId(e.target.value)} />
            <label htmlFor="query-keyword">keyword</label>
            <input id="query-keyword" value={keyword} onChange={(e) => setKeyword(e.target.value)} />
            <label htmlFor="query-min-price">minPrice</label>
            <input id="query-min-price" value={minPrice} onChange={(e) => setMinPrice(e.target.value)} />
            <label htmlFor="query-max-price">maxPrice</label>
            <input id="query-max-price" value={maxPrice} onChange={(e) => setMaxPrice(e.target.value)} />
            <label htmlFor="query-sort">sort</label>
            <select id="query-sort" value={sort} onChange={(e) => setSort(e.target.value as typeof sort)}>
              <option value="NEWEST">NEWEST</option>
              <option value="PRICE_ASC">PRICE_ASC</option>
              <option value="PRICE_DESC">PRICE_DESC</option>
              <option value="POPULARITY">POPULARITY</option>
              <option value="RATING">RATING</option>
            </select>
            <button type="submit">load products</button>

            <label htmlFor="query-product-id">productId</label>
            <input id="query-product-id" value={productId} onChange={(e) => setProductId(e.target.value)} placeholder="1" />
            <div className="button-row">
              <button
                type="button"
                onClick={() =>
                  run(async () => {
                    const res = await fetchQueryProductDetail(Number(productId));
                    setDetail(res.data);
                    setMessage('GET /query/products/:productId success');
                  })
                }
              >
                load detail
              </button>
              <button
                type="button"
                onClick={() =>
                  run(async () => {
                    const res = await syncQueryProductAdmin(Number(productId));
                    setResult(res.data);
                    setMessage('POST /admin/query/products/:productId/sync success');
                  })
                }
              >
                sync product
              </button>
              <button
                type="button"
                onClick={() =>
                  run(async () => {
                    const res = await rebuildQueryProductsAdmin();
                    setResult(res.data);
                    setMessage('POST /admin/query/products/rebuild success');
                  })
                }
              >
                rebuild all
              </button>
            </div>
          </form>

          <ul className="list mt-12">
            {(list?.items ?? []).map((item) => (
              <li key={item.productId}>
                {item.productId} / {item.name} / lowest {item.lowestPrice ?? '-'} / sellers {item.sellerCount}
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>Selected Detail</h2>
          {detail ? <pre className="code-view">{JSON.stringify(detail, null, 2)}</pre> : <p className="sub">load one product detail to inspect the read model.</p>}
        </div>
      </div>

      {result ? <pre className="code-view mt-12">{JSON.stringify(result, null, 2)}</pre> : null}
    </section>
  );
}
