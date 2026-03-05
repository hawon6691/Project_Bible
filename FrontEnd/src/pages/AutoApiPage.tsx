import { FormEvent, useState } from 'react';
import {
  estimateAuto,
  fetchAutoLeaseOffers,
  fetchAutoModels,
  fetchAutoTrims,
} from '@/lib/endpoints';
import type { AutoLeaseOfferItem, AutoModelItem, AutoTrimItem } from '@/lib/types';

export default function AutoApiPage() {
  const [brand, setBrand] = useState('');
  const [type, setType] = useState('');
  const [modelId, setModelId] = useState('');
  const [trimId, setTrimId] = useState('');
  const [optionIdsText, setOptionIdsText] = useState('');
  const [models, setModels] = useState<AutoModelItem[]>([]);
  const [trims, setTrims] = useState<AutoTrimItem[]>([]);
  const [leaseOffers, setLeaseOffers] = useState<AutoLeaseOfferItem[]>([]);
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

  const parseOptionIds = () =>
    optionIdsText
      .split(',')
      .map((v) => Number(v.trim()))
      .filter((v) => Number.isFinite(v) && v > 0);

  return (
    <section>
      <h1>Auto API Step</h1>
      <p className="sub">step 42 - auto API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>Models / Trims / Lease Offers</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await fetchAutoModels({
                brand: brand || undefined,
                type: type || undefined,
              });
              setModels(res.data);
              setMessage('GET /auto/models success');
            });
          }}>
            <label htmlFor="auto-brand">brand(optional)</label>
            <input id="auto-brand" value={brand} onChange={(e) => setBrand(e.target.value)} />
            <label htmlFor="auto-type">type(optional)</label>
            <input id="auto-type" value={type} onChange={(e) => setType(e.target.value)} placeholder="SEDAN|SUV|EV" />
            <button type="submit">load models</button>
          </form>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await fetchAutoTrims(Number(modelId));
              setTrims(res.data);
              setMessage('GET /auto/models/:id/trims success');
            });
          }}>
            <label htmlFor="auto-model-id">modelId</label>
            <input id="auto-model-id" value={modelId} onChange={(e) => setModelId(e.target.value)} required />
            <div className="button-row">
              <button type="submit">load trims</button>
              <button
                type="button"
                onClick={() =>
                  run(async () => {
                    const res = await fetchAutoLeaseOffers(Number(modelId));
                    setLeaseOffers(res.data);
                    setMessage('GET /auto/models/:id/lease-offers success');
                  })
                }
              >
                load lease offers
              </button>
            </div>
          </form>
        </div>

        <div className="panel">
          <h2>POST /auto/estimate</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await estimateAuto({
                modelId: Number(modelId),
                trimId: Number(trimId),
                optionIds: parseOptionIds(),
              });
              setEstimateResult(res.data);
              setMessage('POST /auto/estimate success');
            });
          }}>
            <label htmlFor="auto-trim-id">trimId</label>
            <input id="auto-trim-id" value={trimId} onChange={(e) => setTrimId(e.target.value)} required />
            <label htmlFor="auto-option-ids">optionIds(csv)</label>
            <input id="auto-option-ids" value={optionIdsText} onChange={(e) => setOptionIdsText(e.target.value)} />
            <button type="submit">estimate auto</button>
          </form>
          {estimateResult ? <pre className="code-view mt-12">{JSON.stringify(estimateResult, null, 2)}</pre> : null}
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>Models</h2>
          <ul className="list">
            {models.map((item) => (
              <li key={item.id}>#{item.id} / {item.brand} {item.name} / {item.type} / {item.basePrice}</li>
            ))}
          </ul>
        </div>
        <div className="panel">
          <h2>Trims</h2>
          <ul className="list">
            {trims.map((item) => (
              <li key={item.id}>
                #{item.id} / {item.name} / +{item.priceDelta} / options {item.options.length}
              </li>
            ))}
          </ul>
        </div>
      </div>

      <div className="panel mt-12">
        <h2>Lease Offers</h2>
        <ul className="list">
          {leaseOffers.map((item, idx) => (
            <li key={`${item.provider}-${idx}`}>
              {item.provider} / {item.months}개월 / 월 {item.monthlyFee} / 선수금 {item.downPayment}
            </li>
          ))}
        </ul>
      </div>
    </section>
  );
}
