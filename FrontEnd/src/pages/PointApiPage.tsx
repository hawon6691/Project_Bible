import { FormEvent, useState } from 'react';
import {
  adminGrantPoint,
  fetchPointBalance,
  fetchPointTransactions,
} from '@/lib/endpoints';

export default function PointApiPage() {
  const [balance, setBalance] = useState<any | null>(null);
  const [transactions, setTransactions] = useState<any[]>([]);

  const [page, setPage] = useState('1');
  const [limit, setLimit] = useState('20');
  const [type, setType] = useState('');

  const [grantUserId, setGrantUserId] = useState('');
  const [grantAmount, setGrantAmount] = useState('');
  const [grantDescription, setGrantDescription] = useState('');
  const [grantResult, setGrantResult] = useState<any | null>(null);

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
      <h1>Point API Step</h1>
      <p className="sub">spec step 14 - point API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>GET /points/balance</h2>
          <div className="button-row">
            <button
              type="button"
              onClick={() => run(async () => {
                const res = await fetchPointBalance();
                setBalance(res.data);
                setMessage('GET /points/balance success');
              })}
            >
              load balance
            </button>
          </div>

          {balance ? (
            <div className="mt-12">
              <p>balance: {Number(balance.balance || 0).toLocaleString()}</p>
              <p>expiringSoon: {Number(balance.expiringSoon || 0).toLocaleString()}</p>
              <p>expiringDate: {balance.expiringDate || '-'}</p>
            </div>
          ) : null}
        </div>

        <div className="panel">
          <h2>GET /points/transactions</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await fetchPointTransactions({
                page: Number(page),
                limit: Number(limit),
                ...(type ? { type } : {}),
              });
              setTransactions(res.data as any[]);
              setMessage('GET /points/transactions success');
            });
          }}>
            <label htmlFor="page">page</label>
            <input id="page" value={page} onChange={(e) => setPage(e.target.value)} required />
            <label htmlFor="limit">limit</label>
            <input id="limit" value={limit} onChange={(e) => setLimit(e.target.value)} required />
            <label htmlFor="type">type(optional)</label>
            <input id="type" value={type} onChange={(e) => setType(e.target.value)} placeholder="EARN/USE/REFUND/EXPIRE/ADMIN_GRANT" />
            <button type="submit">load transactions</button>
          </form>

          <ul className="list mt-12">
            {transactions.map((tx) => (
              <li key={tx.id}>
                {tx.id} / {tx.type} / {Number(tx.amount || 0).toLocaleString()} / {tx.description || '-'}
              </li>
            ))}
          </ul>
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>POST /admin/points/grant</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await adminGrantPoint({
                userId: Number(grantUserId),
                amount: Number(grantAmount),
                ...(grantDescription ? { description: grantDescription } : {}),
              });
              setGrantResult(res.data);
              setMessage('POST /admin/points/grant success');
            });
          }}>
            <label htmlFor="grantUserId">userId</label>
            <input id="grantUserId" value={grantUserId} onChange={(e) => setGrantUserId(e.target.value)} required />
            <label htmlFor="grantAmount">amount</label>
            <input id="grantAmount" value={grantAmount} onChange={(e) => setGrantAmount(e.target.value)} required />
            <label htmlFor="grantDescription">description(optional)</label>
            <input id="grantDescription" value={grantDescription} onChange={(e) => setGrantDescription(e.target.value)} />
            <button type="submit">grant point</button>
          </form>
        </div>

        <div className="panel">
          <h2>Grant Result</h2>
          {grantResult ? (
            <pre className="code-view">{JSON.stringify(grantResult, null, 2)}</pre>
          ) : (
            <p className="sub">관리자 포인트 지급 결과가 여기에 표시됩니다.</p>
          )}
        </div>
      </div>
    </section>
  );
}
