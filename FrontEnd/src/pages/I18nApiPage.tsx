import { FormEvent, useState } from 'react';
import {
  convertAmount,
  fetchExchangeRates,
  fetchTranslations,
  removeTranslationAdmin,
  upsertExchangeRateAdmin,
  upsertTranslationAdmin,
} from '@/lib/endpoints';
import type { ExchangeRateItem, TranslationItem } from '@/lib/types';

export default function I18nApiPage() {
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const [locale, setLocale] = useState('en');
  const [namespaceValue, setNamespaceValue] = useState('product');
  const [key, setKey] = useState('');
  const [translations, setTranslations] = useState<TranslationItem[]>([]);

  const [upsertLocale, setUpsertLocale] = useState('en');
  const [upsertNamespace, setUpsertNamespace] = useState('product');
  const [upsertKey, setUpsertKey] = useState('product.lowest_price');
  const [upsertValue, setUpsertValue] = useState('Lowest Price');
  const [translationId, setTranslationId] = useState('');

  const [exchangeRates, setExchangeRates] = useState<ExchangeRateItem[]>([]);
  const [baseCurrency, setBaseCurrency] = useState('KRW');
  const [targetCurrency, setTargetCurrency] = useState('USD');
  const [rate, setRate] = useState('0.00074');

  const [amount, setAmount] = useState('1590000');
  const [convertFrom, setConvertFrom] = useState('KRW');
  const [convertTo, setConvertTo] = useState('USD');
  const [converted, setConverted] = useState<unknown>(null);
  const [lastResult, setLastResult] = useState<unknown>(null);

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
      <h1>I18n API Step</h1>
      <p className="sub">next step - i18n API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>Translations</h2>
          <form
            className="form-box"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await fetchTranslations({
                  locale: locale || undefined,
                  namespace: namespaceValue || undefined,
                  key: key || undefined,
                });
                setTranslations(res.data);
                setMessage('GET /i18n/translations success');
              });
            }}
          >
            <label htmlFor="i18n-locale">locale</label>
            <input id="i18n-locale" value={locale} onChange={(e) => setLocale(e.target.value)} />
            <label htmlFor="i18n-namespace">namespace</label>
            <input
              id="i18n-namespace"
              value={namespaceValue}
              onChange={(e) => setNamespaceValue(e.target.value)}
            />
            <label htmlFor="i18n-key">key (optional)</label>
            <input id="i18n-key" value={key} onChange={(e) => setKey(e.target.value)} />
            <button type="submit">load translations</button>
          </form>

          <form
            className="form-box mt-12"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await upsertTranslationAdmin({
                  locale: upsertLocale,
                  namespace: upsertNamespace,
                  key: upsertKey,
                  value: upsertValue,
                });
                setLastResult(res.data);
                setMessage('POST /i18n/admin/translations success');
              });
            }}
          >
            <label htmlFor="upsert-locale">locale</label>
            <input
              id="upsert-locale"
              value={upsertLocale}
              onChange={(e) => setUpsertLocale(e.target.value)}
              required
            />
            <label htmlFor="upsert-namespace">namespace</label>
            <input
              id="upsert-namespace"
              value={upsertNamespace}
              onChange={(e) => setUpsertNamespace(e.target.value)}
              required
            />
            <label htmlFor="upsert-key">key</label>
            <input id="upsert-key" value={upsertKey} onChange={(e) => setUpsertKey(e.target.value)} required />
            <label htmlFor="upsert-value">value</label>
            <input
              id="upsert-value"
              value={upsertValue}
              onChange={(e) => setUpsertValue(e.target.value)}
              required
            />
            <button type="submit">upsert translation</button>
          </form>

          <form
            className="form-box mt-12"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await removeTranslationAdmin(Number(translationId));
                setLastResult(res.data);
                setMessage('DELETE /i18n/admin/translations/:id success');
              });
            }}
          >
            <label htmlFor="translation-id">translationId</label>
            <input
              id="translation-id"
              value={translationId}
              onChange={(e) => setTranslationId(e.target.value)}
              required
            />
            <button type="submit">delete translation</button>
          </form>

          <ul className="list mt-12">
            {translations.map((item) => (
              <li key={item.id}>
                #{item.id} / {item.locale} / {item.namespace} / {item.key} = {item.value}
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>Exchange Rates + Convert</h2>
          <div className="button-row">
            <button
              type="button"
              onClick={() =>
                run(async () => {
                  const res = await fetchExchangeRates();
                  setExchangeRates(res.data);
                  setMessage('GET /i18n/exchange-rates success');
                })
              }
            >
              load exchange rates
            </button>
          </div>

          <form
            className="form-box mt-12"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await upsertExchangeRateAdmin({
                  baseCurrency,
                  targetCurrency,
                  rate: Number(rate),
                });
                setLastResult(res.data);
                setMessage('POST /i18n/admin/exchange-rates success');
              });
            }}
          >
            <label htmlFor="base-currency">baseCurrency</label>
            <input
              id="base-currency"
              value={baseCurrency}
              onChange={(e) => setBaseCurrency(e.target.value.toUpperCase())}
              required
            />
            <label htmlFor="target-currency">targetCurrency</label>
            <input
              id="target-currency"
              value={targetCurrency}
              onChange={(e) => setTargetCurrency(e.target.value.toUpperCase())}
              required
            />
            <label htmlFor="exchange-rate">rate</label>
            <input id="exchange-rate" value={rate} onChange={(e) => setRate(e.target.value)} required />
            <button type="submit">upsert exchange rate</button>
          </form>

          <form
            className="form-box mt-12"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await convertAmount({
                  amount: Number(amount),
                  from: convertFrom,
                  to: convertTo,
                });
                setConverted(res.data);
                setMessage('GET /i18n/convert success');
              });
            }}
          >
            <label htmlFor="convert-amount">amount</label>
            <input id="convert-amount" value={amount} onChange={(e) => setAmount(e.target.value)} required />
            <label htmlFor="convert-from">from</label>
            <input
              id="convert-from"
              value={convertFrom}
              onChange={(e) => setConvertFrom(e.target.value.toUpperCase())}
              required
            />
            <label htmlFor="convert-to">to</label>
            <input
              id="convert-to"
              value={convertTo}
              onChange={(e) => setConvertTo(e.target.value.toUpperCase())}
              required
            />
            <button type="submit">convert amount</button>
          </form>

          <ul className="list mt-12">
            {exchangeRates.map((item) => (
              <li key={item.id}>
                {item.baseCurrency}/{item.targetCurrency} = {item.rate}
              </li>
            ))}
          </ul>

          {converted ? <pre className="code-view mt-12">{JSON.stringify(converted, null, 2)}</pre> : null}
          {lastResult ? <pre className="code-view mt-12">{JSON.stringify(lastResult, null, 2)}</pre> : null}
        </div>
      </div>
    </section>
  );
}
