import { FormEvent, useState } from 'react';
import {
  fetchPushPreference,
  fetchPushSubscriptions,
  registerPushSubscription,
  unregisterPushSubscription,
  updatePushPreference,
} from '@/lib/endpoints';
import type { PushPreferenceItem, PushSubscriptionItem } from '@/lib/types';

export default function PushApiPage() {
  const [endpoint, setEndpoint] = useState('https://example.com/push/subscription');
  const [p256dhKey, setP256dhKey] = useState('sample-p256dh-key');
  const [authKey, setAuthKey] = useState('sample-auth-key');
  const [expirationTime, setExpirationTime] = useState('');
  const [preference, setPreference] = useState<PushPreferenceItem | null>(null);
  const [subscriptions, setSubscriptions] = useState<PushSubscriptionItem[]>([]);
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
      <h1>Push API Step</h1>
      <p className="sub">remaining backend module - push subscription and preference integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>Subscriptions</h2>
          <form
            className="form-box"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await registerPushSubscription({
                  endpoint,
                  p256dhKey,
                  authKey,
                  ...(expirationTime.trim() ? { expirationTime: expirationTime.trim() } : {}),
                });
                setResult(res.data);
                setMessage('POST /push/subscriptions success');
              });
            }}
          >
            <label htmlFor="push-endpoint">endpoint</label>
            <input id="push-endpoint" value={endpoint} onChange={(e) => setEndpoint(e.target.value)} required />
            <label htmlFor="push-p256">p256dhKey</label>
            <input id="push-p256" value={p256dhKey} onChange={(e) => setP256dhKey(e.target.value)} required />
            <label htmlFor="push-auth">authKey</label>
            <input id="push-auth" value={authKey} onChange={(e) => setAuthKey(e.target.value)} required />
            <label htmlFor="push-expiration">expirationTime</label>
            <input id="push-expiration" value={expirationTime} onChange={(e) => setExpirationTime(e.target.value)} placeholder="epoch ms string" />
            <div className="button-row">
              <button type="submit">register</button>
              <button
                type="button"
                onClick={() =>
                  run(async () => {
                    const res = await fetchPushSubscriptions();
                    setSubscriptions(res.data);
                    setMessage('GET /push/subscriptions success');
                  })
                }
              >
                load subscriptions
              </button>
              <button
                type="button"
                onClick={() =>
                  run(async () => {
                    const res = await unregisterPushSubscription(endpoint);
                    setResult(res.data);
                    setMessage('POST /push/subscriptions/unsubscribe success');
                  })
                }
              >
                unsubscribe
              </button>
            </div>
          </form>

          <ul className="list mt-12">
            {subscriptions.map((item) => (
              <li key={item.id}>
                {item.id} / {item.endpoint} / active {String(item.isActive)}
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>Preferences</h2>
          <div className="button-row">
            <button
              type="button"
              onClick={() =>
                run(async () => {
                  const res = await fetchPushPreference();
                  setPreference(res.data);
                  setMessage('GET /push/preferences success');
                })
              }
            >
              load preference
            </button>
            <button
              type="button"
              onClick={() =>
                run(async () => {
                  const base = preference ?? {
                    id: 0,
                    userId: 0,
                    priceAlertEnabled: true,
                    orderStatusEnabled: true,
                    chatMessageEnabled: true,
                    dealEnabled: true,
                    createdAt: '',
                    updatedAt: '',
                  };
                  const res = await updatePushPreference({
                    priceAlertEnabled: base.priceAlertEnabled,
                    orderStatusEnabled: base.orderStatusEnabled,
                    chatMessageEnabled: base.chatMessageEnabled,
                    dealEnabled: base.dealEnabled,
                  });
                  setPreference(res.data);
                  setResult(res.data);
                  setMessage('POST /push/preferences success');
                })
              }
            >
              save preference
            </button>
          </div>

          {preference ? (
            <form className="form-box mt-12">
              <label>
                <input
                  type="checkbox"
                  checked={preference.priceAlertEnabled}
                  onChange={(e) =>
                    setPreference((prev) => (prev ? { ...prev, priceAlertEnabled: e.target.checked } : prev))
                  }
                />
                priceAlertEnabled
              </label>
              <label>
                <input
                  type="checkbox"
                  checked={preference.orderStatusEnabled}
                  onChange={(e) =>
                    setPreference((prev) => (prev ? { ...prev, orderStatusEnabled: e.target.checked } : prev))
                  }
                />
                orderStatusEnabled
              </label>
              <label>
                <input
                  type="checkbox"
                  checked={preference.chatMessageEnabled}
                  onChange={(e) =>
                    setPreference((prev) => (prev ? { ...prev, chatMessageEnabled: e.target.checked } : prev))
                  }
                />
                chatMessageEnabled
              </label>
              <label>
                <input
                  type="checkbox"
                  checked={preference.dealEnabled}
                  onChange={(e) =>
                    setPreference((prev) => (prev ? { ...prev, dealEnabled: e.target.checked } : prev))
                  }
                />
                dealEnabled
              </label>
            </form>
          ) : null}
        </div>
      </div>

      {result ? <pre className="code-view mt-12">{JSON.stringify(result, null, 2)}</pre> : null}
    </section>
  );
}
