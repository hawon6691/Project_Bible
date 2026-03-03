import { FormEvent, useState } from 'react';
import {
  adminRefundPayment,
  fetchMyOrders,
  fetchPayment,
  refundPayment,
  requestPayment,
} from '@/lib/endpoints';

export default function PaymentApiPage() {
  const [orders, setOrders] = useState<any[]>([]);
  const [paymentDetail, setPaymentDetail] = useState<any | null>(null);

  const [createOrderId, setCreateOrderId] = useState('');
  const [createMethod, setCreateMethod] = useState('CARD');
  const [createAmount, setCreateAmount] = useState('');

  const [detailPaymentId, setDetailPaymentId] = useState('');

  const [refundPaymentId, setRefundPaymentId] = useState('');
  const [refundReason, setRefundReason] = useState('');

  const [adminRefundPaymentId, setAdminRefundPaymentId] = useState('');
  const [adminRefundReason, setAdminRefundReason] = useState('');

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
      <h1>Payment API Step</h1>
      <p className="sub">spec step 11 - payment API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>결제 대상 주문 확인</h2>
          <div className="button-row">
            <button
              type="button"
              onClick={() => run(async () => {
                const res = await fetchMyOrders(1, 20);
                setOrders(res.data as any[]);
                setMessage('GET /orders success');
              })}
            >
              GET /orders
            </button>
          </div>

          <ul className="list mt-12">
            {orders.map((order) => (
              <li key={order.id}>
                {order.id} / {order.orderNumber} / {order.status} / {Number(order.finalAmount || 0).toLocaleString()}
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>POST /payments</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await requestPayment({
                orderId: Number(createOrderId),
                method: createMethod,
                amount: Number(createAmount),
              });
              setPaymentDetail(res.data);
              setMessage('POST /payments success');
            });
          }}>
            <label htmlFor="createOrderId">orderId</label>
            <input id="createOrderId" value={createOrderId} onChange={(e) => setCreateOrderId(e.target.value)} required />
            <label htmlFor="createMethod">method</label>
            <input id="createMethod" value={createMethod} onChange={(e) => setCreateMethod(e.target.value)} required />
            <label htmlFor="createAmount">amount</label>
            <input id="createAmount" value={createAmount} onChange={(e) => setCreateAmount(e.target.value)} required />
            <button type="submit">request payment</button>
          </form>
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>GET /payments/:id</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await fetchPayment(Number(detailPaymentId));
              setPaymentDetail(res.data);
              setMessage('GET /payments/:id success');
            });
          }}>
            <label htmlFor="detailPaymentId">paymentId</label>
            <input id="detailPaymentId" value={detailPaymentId} onChange={(e) => setDetailPaymentId(e.target.value)} required />
            <button type="submit">get payment</button>
          </form>
        </div>

        <div className="panel">
          <h2>POST /payments/:id/refund</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await refundPayment(Number(refundPaymentId), refundReason ? { reason: refundReason } : undefined);
              setPaymentDetail(res.data);
              setMessage('POST /payments/:id/refund success');
            });
          }}>
            <label htmlFor="refundPaymentId">paymentId</label>
            <input id="refundPaymentId" value={refundPaymentId} onChange={(e) => setRefundPaymentId(e.target.value)} required />
            <label htmlFor="refundReason">reason(optional)</label>
            <input id="refundReason" value={refundReason} onChange={(e) => setRefundReason(e.target.value)} />
            <button type="submit">refund payment</button>
          </form>
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>POST /admin/payments/:id/refund (서버 구현 기준)</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await adminRefundPayment(
                Number(adminRefundPaymentId),
                adminRefundReason ? { reason: adminRefundReason } : undefined,
              );
              setPaymentDetail(res.data);
              setMessage('POST /admin/payments/:id/refund success');
            });
          }}>
            <label htmlFor="adminRefundPaymentId">paymentId</label>
            <input id="adminRefundPaymentId" value={adminRefundPaymentId} onChange={(e) => setAdminRefundPaymentId(e.target.value)} required />
            <label htmlFor="adminRefundReason">reason(optional)</label>
            <input id="adminRefundReason" value={adminRefundReason} onChange={(e) => setAdminRefundReason(e.target.value)} />
            <button type="submit">admin refund</button>
          </form>
        </div>

        <div className="panel">
          <h2>결제 응답</h2>
          {paymentDetail ? (
            <pre className="code-view">{JSON.stringify(paymentDetail, null, 2)}</pre>
          ) : (
            <p className="sub">결제 API 호출 결과가 여기에 표시됩니다.</p>
          )}
        </div>
      </div>
    </section>
  );
}
