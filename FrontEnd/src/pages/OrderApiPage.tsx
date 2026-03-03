import { FormEvent, useState } from 'react';
import {
  cancelOrder,
  createOrder,
  fetchAdminOrders,
  fetchAddresses,
  fetchMyOrders,
  fetchOrderById,
  updateAdminOrderStatus,
} from '@/lib/endpoints';

type ParsedItem = {
  productId: number;
  sellerId: number;
  quantity: number;
  selectedOptions?: string;
};

function parseItems(input: string): ParsedItem[] {
  // format: productId:sellerId:quantity[:selectedOptions],...
  return input
    .split(',')
    .map((chunk) => chunk.trim())
    .filter(Boolean)
    .map((chunk) => {
      const parts = chunk.split(':');
      const productId = Number(parts[0]);
      const sellerId = Number(parts[1]);
      const quantity = Number(parts[2]);
      const selectedOptions = parts[3];
      return {
        productId,
        sellerId,
        quantity,
        ...(selectedOptions ? { selectedOptions } : {}),
      };
    })
    .filter((item) => Number.isFinite(item.productId) && Number.isFinite(item.sellerId) && Number.isFinite(item.quantity));
}

function parseIds(input: string): number[] {
  return input
    .split(',')
    .map((v) => Number(v.trim()))
    .filter((v) => Number.isFinite(v) && v > 0);
}

export default function OrderApiPage() {
  const [myOrders, setMyOrders] = useState<any[]>([]);
  const [adminOrders, setAdminOrders] = useState<any[]>([]);
  const [orderDetail, setOrderDetail] = useState<any | null>(null);
  const [addresses, setAddresses] = useState<any[]>([]);

  const [detailId, setDetailId] = useState('');
  const [cancelId, setCancelId] = useState('');

  const [createAddressId, setCreateAddressId] = useState('');
  const [createItemsInput, setCreateItemsInput] = useState('');
  const [createFromCart, setCreateFromCart] = useState(false);
  const [createCartItemIdsInput, setCreateCartItemIdsInput] = useState('');
  const [createUsePoint, setCreateUsePoint] = useState('');
  const [createMemo, setCreateMemo] = useState('');

  const [adminOrderId, setAdminOrderId] = useState('');
  const [adminStatus, setAdminStatus] = useState('ORDER_PLACED');

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
      <h1>Order API Step</h1>
      <p className="sub">spec step 10 - order API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>User Order APIs</h2>
          <div className="button-row">
            <button
              type="button"
              onClick={() => run(async () => {
                const res = await fetchMyOrders(1, 20);
                setMyOrders(res.data as any[]);
                setMessage('GET /orders success');
              })}
            >
              GET /orders
            </button>
            <button
              type="button"
              onClick={() => run(async () => {
                const res = await fetchAddresses();
                setAddresses(res.data as any[]);
                setMessage('GET /addresses success');
                if (!createAddressId && res.data[0]) {
                  setCreateAddressId(String(res.data[0].id));
                }
              })}
            >
              Load addresses
            </button>
          </div>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await fetchOrderById(Number(detailId));
              setOrderDetail(res.data);
              setMessage('GET /orders/:id success');
            });
          }}>
            <label htmlFor="detailId">orderId</label>
            <input id="detailId" value={detailId} onChange={(e) => setDetailId(e.target.value)} required />
            <button type="submit">GET /orders/:id</button>
          </form>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await cancelOrder(Number(cancelId));
              setOrderDetail(res.data);
              setMessage('POST /orders/:id/cancel success');
            });
          }}>
            <label htmlFor="cancelId">orderId</label>
            <input id="cancelId" value={cancelId} onChange={(e) => setCancelId(e.target.value)} required />
            <button type="submit">POST /orders/:id/cancel</button>
          </form>

          <ul className="list mt-12">
            {myOrders.map((order) => (
              <li key={order.id}>
                {order.id} / {order.orderNumber} / {order.status} / {Number(order.finalAmount || 0).toLocaleString()}
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>POST /orders</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await createOrder({
                addressId: Number(createAddressId),
                items: parseItems(createItemsInput),
                fromCart: createFromCart,
                ...(createCartItemIdsInput ? { cartItemIds: parseIds(createCartItemIdsInput) } : {}),
                ...(createUsePoint ? { usePoint: Number(createUsePoint) } : {}),
                ...(createMemo ? { memo: createMemo } : {}),
              });
              setOrderDetail(res.data);
              setMessage('POST /orders success');
            });
          }}>
            <label htmlFor="createAddressId">addressId</label>
            <input id="createAddressId" value={createAddressId} onChange={(e) => setCreateAddressId(e.target.value)} required />
            <label htmlFor="createItemsInput">items</label>
            <input
              id="createItemsInput"
              value={createItemsInput}
              onChange={(e) => setCreateItemsInput(e.target.value)}
              placeholder="1:1:2:실버,2:1:1"
              required
            />
            <label htmlFor="createFromCart">fromCart</label>
            <input id="createFromCart" type="checkbox" checked={createFromCart} onChange={(e) => setCreateFromCart(e.target.checked)} />
            <label htmlFor="createCartItemIdsInput">cartItemIds(optional)</label>
            <input id="createCartItemIdsInput" value={createCartItemIdsInput} onChange={(e) => setCreateCartItemIdsInput(e.target.value)} placeholder="1,2,3" />
            <label htmlFor="createUsePoint">usePoint(optional)</label>
            <input id="createUsePoint" value={createUsePoint} onChange={(e) => setCreateUsePoint(e.target.value)} />
            <label htmlFor="createMemo">memo(optional)</label>
            <input id="createMemo" value={createMemo} onChange={(e) => setCreateMemo(e.target.value)} />
            <button type="submit">create order</button>
          </form>

          {addresses.length > 0 ? (
            <ul className="list mt-12">
              {addresses.map((addr) => (
                <li key={addr.id}>
                  address {addr.id} / {addr.recipientName} / {addr.address}
                </li>
              ))}
            </ul>
          ) : null}

          {orderDetail ? (
            <pre className="code-view mt-12">{JSON.stringify(orderDetail, null, 2)}</pre>
          ) : null}
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>Admin Order APIs</h2>
          <div className="button-row">
            <button
              type="button"
              onClick={() => run(async () => {
                const res = await fetchAdminOrders(1, 30);
                setAdminOrders(res.data as any[]);
                setMessage('GET /admin/orders success');
              })}
            >
              GET /admin/orders
            </button>
          </div>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await updateAdminOrderStatus(Number(adminOrderId), adminStatus);
              setOrderDetail(res.data);
              setMessage('PATCH /admin/orders/:id/status success');
            });
          }}>
            <label htmlFor="adminOrderId">orderId</label>
            <input id="adminOrderId" value={adminOrderId} onChange={(e) => setAdminOrderId(e.target.value)} required />
            <label htmlFor="adminStatus">status</label>
            <input id="adminStatus" value={adminStatus} onChange={(e) => setAdminStatus(e.target.value)} required />
            <button type="submit">PATCH /admin/orders/:id/status</button>
          </form>
        </div>

        <div className="panel">
          <h2>Admin Order List</h2>
          <ul className="list">
            {adminOrders.map((order) => (
              <li key={order.id}>
                {order.id} / {order.orderNumber} / {order.status} / {Number(order.finalAmount || 0).toLocaleString()}
              </li>
            ))}
          </ul>
        </div>
      </div>
    </section>
  );
}
