import { FormEvent, useState } from 'react';
import {
  createPriceAlert,
  createProductPrice,
  fetchPriceAlerts,
  fetchPriceHistory,
  fetchProductPrices,
  removePriceAlert,
  removePriceEntry,
  updatePriceEntry,
} from '@/lib/endpoints';

export default function PriceApiPage() {
  const [productId, setProductId] = useState('');
  const [priceSummary, setPriceSummary] = useState<any>(null);
  const [priceHistory, setPriceHistory] = useState<any>(null);
  const [historyPeriod, setHistoryPeriod] = useState<'1w' | '1m' | '3m' | '6m' | '1y'>('3m');
  const [historyType, setHistoryType] = useState<'daily' | 'weekly' | 'monthly'>('daily');

  const [createProductId, setCreateProductId] = useState('');
  const [createSellerId, setCreateSellerId] = useState('');
  const [createPriceValue, setCreatePriceValue] = useState('');
  const [createShippingCost, setCreateShippingCost] = useState('0');
  const [createShippingInfo, setCreateShippingInfo] = useState('');
  const [createProductUrl, setCreateProductUrl] = useState('');

  const [updatePriceId, setUpdatePriceId] = useState('');
  const [updatePriceValue, setUpdatePriceValue] = useState('');
  const [updateShippingCost, setUpdateShippingCost] = useState('');
  const [updateShippingInfo, setUpdateShippingInfo] = useState('');
  const [updateProductUrl, setUpdateProductUrl] = useState('');

  const [deletePriceId, setDeletePriceId] = useState('');

  const [alerts, setAlerts] = useState<any[]>([]);
  const [alertProductId, setAlertProductId] = useState('');
  const [alertTargetPrice, setAlertTargetPrice] = useState('');
  const [deleteAlertId, setDeleteAlertId] = useState('');

  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const run = async (fn: () => Promise<void>) => {
    setError('');
    setMessage('');
    try {
      await fn();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'request failed');
    }
  };

  return (
    <section>
      <h1>Price API Step</h1>
      <p className="sub">spec step 7 - price API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>GET /products/:id/prices</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await fetchProductPrices(Number(productId));
              setPriceSummary(res.data);
              setMessage('GET /products/:id/prices success');
            });
          }}>
            <label htmlFor="productId">productId</label>
            <input id="productId" value={productId} onChange={(e) => setProductId(e.target.value)} required />
            <button type="submit">load prices</button>
          </form>

          {priceSummary ? (
            <div className="mt-12">
              <p>lowest: {Number(priceSummary.lowestPrice || 0).toLocaleString()}</p>
              <p>average: {Number(priceSummary.averagePrice || 0).toLocaleString()}</p>
              <p>highest: {Number(priceSummary.highestPrice || 0).toLocaleString()}</p>
              <ul className="list mt-12">
                {(priceSummary.entries || []).map((entry: any) => (
                  <li key={entry.id}>
                    {entry.id} / {entry.seller?.name} / {Number(entry.price || 0).toLocaleString()} / ship {Number(entry.shippingCost || 0).toLocaleString()}
                  </li>
                ))}
              </ul>
            </div>
          ) : null}
        </div>

        <div className="panel">
          <h2>GET /products/:id/price-history</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await fetchPriceHistory(Number(productId), {
                period: historyPeriod,
                type: historyType,
              });
              setPriceHistory(res.data);
              setMessage('GET /products/:id/price-history success');
            });
          }}>
            <label htmlFor="historyPeriod">period</label>
            <select id="historyPeriod" value={historyPeriod} onChange={(e) => setHistoryPeriod(e.target.value as '1w' | '1m' | '3m' | '6m' | '1y')}>
              <option value="1w">1w</option>
              <option value="1m">1m</option>
              <option value="3m">3m</option>
              <option value="6m">6m</option>
              <option value="1y">1y</option>
            </select>
            <label htmlFor="historyType">type</label>
            <select id="historyType" value={historyType} onChange={(e) => setHistoryType(e.target.value as 'daily' | 'weekly' | 'monthly')}>
              <option value="daily">daily</option>
              <option value="weekly">weekly</option>
              <option value="monthly">monthly</option>
            </select>
            <button type="submit">load history</button>
          </form>

          {priceHistory ? (
            <div className="mt-12">
              <p>product: {priceHistory.productName}</p>
              <p>allTimeLowest: {Number(priceHistory.allTimeLowest || 0).toLocaleString()}</p>
              <p>allTimeHighest: {Number(priceHistory.allTimeHighest || 0).toLocaleString()}</p>
              <ul className="list mt-12">
                {(priceHistory.history || []).slice(0, 20).map((h: any, idx: number) => (
                  <li key={`h-${idx}`}>
                    {h.date} / lowest {Number(h.lowestPrice || 0).toLocaleString()} / avg {Number(h.averagePrice || 0).toLocaleString()}
                  </li>
                ))}
              </ul>
            </div>
          ) : null}
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>Price Entry Admin/Seller API</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await createProductPrice({
                productId: Number(createProductId),
                sellerId: Number(createSellerId),
                price: Number(createPriceValue),
                shippingCost: Number(createShippingCost),
                ...(createShippingInfo ? { shippingInfo: createShippingInfo } : {}),
                productUrl: createProductUrl,
              });
              setMessage('POST /products/:id/prices success');
            });
          }}>
            <label htmlFor="createProductId">productId</label>
            <input id="createProductId" value={createProductId} onChange={(e) => setCreateProductId(e.target.value)} required />
            <label htmlFor="createSellerId">sellerId</label>
            <input id="createSellerId" value={createSellerId} onChange={(e) => setCreateSellerId(e.target.value)} required />
            <label htmlFor="createPriceValue">price</label>
            <input id="createPriceValue" value={createPriceValue} onChange={(e) => setCreatePriceValue(e.target.value)} required />
            <label htmlFor="createShippingCost">shippingCost</label>
            <input id="createShippingCost" value={createShippingCost} onChange={(e) => setCreateShippingCost(e.target.value)} required />
            <label htmlFor="createShippingInfo">shippingInfo(optional)</label>
            <input id="createShippingInfo" value={createShippingInfo} onChange={(e) => setCreateShippingInfo(e.target.value)} />
            <label htmlFor="createProductUrl">productUrl</label>
            <input id="createProductUrl" value={createProductUrl} onChange={(e) => setCreateProductUrl(e.target.value)} required />
            <button type="submit">create price</button>
          </form>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await updatePriceEntry(Number(updatePriceId), {
                ...(updatePriceValue ? { price: Number(updatePriceValue) } : {}),
                ...(updateShippingCost ? { shippingCost: Number(updateShippingCost) } : {}),
                ...(updateShippingInfo ? { shippingInfo: updateShippingInfo } : {}),
                ...(updateProductUrl ? { productUrl: updateProductUrl } : {}),
              });
              setMessage('PATCH /prices/:id success');
            });
          }}>
            <label htmlFor="updatePriceId">priceId</label>
            <input id="updatePriceId" value={updatePriceId} onChange={(e) => setUpdatePriceId(e.target.value)} required />
            <label htmlFor="updatePriceValue">price(optional)</label>
            <input id="updatePriceValue" value={updatePriceValue} onChange={(e) => setUpdatePriceValue(e.target.value)} />
            <label htmlFor="updateShippingCost">shippingCost(optional)</label>
            <input id="updateShippingCost" value={updateShippingCost} onChange={(e) => setUpdateShippingCost(e.target.value)} />
            <label htmlFor="updateShippingInfo">shippingInfo(optional)</label>
            <input id="updateShippingInfo" value={updateShippingInfo} onChange={(e) => setUpdateShippingInfo(e.target.value)} />
            <label htmlFor="updateProductUrl">productUrl(optional)</label>
            <input id="updateProductUrl" value={updateProductUrl} onChange={(e) => setUpdateProductUrl(e.target.value)} />
            <button type="submit">update price</button>
          </form>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await removePriceEntry(Number(deletePriceId));
              setMessage('DELETE /prices/:id success');
            });
          }}>
            <label htmlFor="deletePriceId">priceId</label>
            <input id="deletePriceId" value={deletePriceId} onChange={(e) => setDeletePriceId(e.target.value)} required />
            <button type="submit">delete price</button>
          </form>
        </div>

        <div className="panel">
          <h2>Price Alerts API</h2>
          <div className="button-row">
            <button type="button" onClick={() => run(async () => {
              const res = await fetchPriceAlerts();
              setAlerts(res.data as any[]);
              setMessage('GET /price-alerts success');
            })}>GET /price-alerts</button>
          </div>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await createPriceAlert({ productId: Number(alertProductId), targetPrice: Number(alertTargetPrice) });
              setMessage('POST /price-alerts success');
            });
          }}>
            <label htmlFor="alertProductId">productId</label>
            <input id="alertProductId" value={alertProductId} onChange={(e) => setAlertProductId(e.target.value)} required />
            <label htmlFor="alertTargetPrice">targetPrice</label>
            <input id="alertTargetPrice" value={alertTargetPrice} onChange={(e) => setAlertTargetPrice(e.target.value)} required />
            <button type="submit">create alert</button>
          </form>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await removePriceAlert(Number(deleteAlertId));
              setMessage('DELETE /price-alerts/:id success');
            });
          }}>
            <label htmlFor="deleteAlertId">alertId</label>
            <input id="deleteAlertId" value={deleteAlertId} onChange={(e) => setDeleteAlertId(e.target.value)} required />
            <button type="submit">delete alert</button>
          </form>

          <ul className="list mt-12">
            {alerts.map((a) => (
              <li key={a.id}>
                {a.id} / product {a.productId} / target {Number(a.targetPrice || 0).toLocaleString()} / current {Number(a.currentLowestPrice || 0).toLocaleString()} / triggered {String(a.isTriggered)}
              </li>
            ))}
          </ul>
        </div>
      </div>
    </section>
  );
}
