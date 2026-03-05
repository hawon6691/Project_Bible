import { FormEvent, useState } from 'react';
import {
  estimateUsedPcBuildPrice,
  fetchUsedCategoryPrices,
  fetchUsedProductPrice,
} from '@/lib/endpoints';
import type { UsedCategoryPriceItem } from '@/lib/types';

export default function UsedMarketApiPage() {
  const [productId, setProductId] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [buildId, setBuildId] = useState('');
  const [productPrice, setProductPrice] = useState<unknown>(null);
  const [categoryPrices, setCategoryPrices] = useState<UsedCategoryPriceItem[]>([]);
  const [estimateResult, setEstimateResult] = useState<unknown>(null);
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
      <h1>Used Market API Step</h1>
      <p className="sub">step 41 - used market API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>GET /used-market/products/:id/price</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await fetchUsedProductPrice(Number(productId));
              setProductPrice(res.data);
              setMessage('GET /used-market/products/:id/price success');
            });
          }}>
            <label htmlFor="used-product-id">productId</label>
            <input
              id="used-product-id"
              value={productId}
              onChange={(e) => setProductId(e.target.value)}
              required
            />
            <button type="submit">load used price</button>
          </form>
          {productPrice ? <pre className="code-view mt-12">{JSON.stringify(productPrice, null, 2)}</pre> : null}
        </div>

        <div className="panel">
          <h2>GET /used-market/categories/:id/prices</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await fetchUsedCategoryPrices(Number(categoryId));
              setCategoryPrices(res.data);
              setMessage('GET /used-market/categories/:id/prices success');
            });
          }}>
            <label htmlFor="used-category-id">categoryId</label>
            <input
              id="used-category-id"
              value={categoryId}
              onChange={(e) => setCategoryId(e.target.value)}
              required
            />
            <button type="submit">load category used prices</button>
          </form>
          <ul className="list mt-12">
            {categoryPrices.map((item) => (
              <li key={item.productId}>
                {item.productName} / avg {item.averagePrice} / min {item.minPrice} / max {item.maxPrice} / {item.trend}
              </li>
            ))}
          </ul>
        </div>
      </div>

      <div className="panel mt-12">
        <h2>POST /used-market/pc-builds/:buildId/estimate</h2>
        <form className="form-box" onSubmit={(e: FormEvent) => {
          e.preventDefault();
          run(async () => {
            const res = await estimateUsedPcBuildPrice(Number(buildId));
            setEstimateResult(res.data);
            setMessage('POST /used-market/pc-builds/:buildId/estimate success');
          });
        }}>
          <label htmlFor="used-build-id">buildId</label>
          <input id="used-build-id" value={buildId} onChange={(e) => setBuildId(e.target.value)} required />
          <button type="submit">estimate build used price</button>
        </form>
        {estimateResult ? <pre className="code-view mt-12">{JSON.stringify(estimateResult, null, 2)}</pre> : null}
      </div>
    </section>
  );
}
