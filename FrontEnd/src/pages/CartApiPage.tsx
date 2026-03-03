import { FormEvent, useState } from 'react';
import {
  addToCart,
  clearCart,
  fetchCart,
  removeCartItem,
  updateCartQuantity,
} from '@/lib/endpoints';

export default function CartApiPage() {
  const [items, setItems] = useState<any[]>([]);

  const [addProductId, setAddProductId] = useState('');
  const [addSellerId, setAddSellerId] = useState('');
  const [addQuantity, setAddQuantity] = useState('1');
  const [addOptions, setAddOptions] = useState('');

  const [updateItemId, setUpdateItemId] = useState('');
  const [updateQuantityValue, setUpdateQuantityValue] = useState('1');

  const [deleteItemId, setDeleteItemId] = useState('');

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

  const loadCart = () => run(async () => {
    const res = await fetchCart();
    setItems(res.data as any[]);
    setMessage('GET /cart success');
  });

  return (
    <section>
      <h1>Cart API Step</h1>
      <p className="sub">spec step 8 - cart API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>GET /cart</h2>
          <div className="button-row">
            <button type="button" onClick={loadCart}>load cart</button>
          </div>

          <ul className="list mt-12">
            {items.map((item) => (
              <li key={String(item.id)}>
                {String(item.id)} / {item.product?.name || 'deleted product'} / qty {item.quantity} / seller {item.seller?.name || '-'}
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>POST /cart</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await addToCart({
                productId: Number(addProductId),
                sellerId: Number(addSellerId),
                quantity: Number(addQuantity),
                ...(addOptions ? { selectedOptions: addOptions } : {}),
              });
              setMessage('POST /cart success');
              await loadCart();
            });
          }}>
            <label htmlFor="addProductId">productId</label>
            <input id="addProductId" value={addProductId} onChange={(e) => setAddProductId(e.target.value)} required />
            <label htmlFor="addSellerId">sellerId</label>
            <input id="addSellerId" value={addSellerId} onChange={(e) => setAddSellerId(e.target.value)} required />
            <label htmlFor="addQuantity">quantity</label>
            <input id="addQuantity" value={addQuantity} onChange={(e) => setAddQuantity(e.target.value)} required />
            <label htmlFor="addOptions">selectedOptions(optional)</label>
            <input id="addOptions" value={addOptions} onChange={(e) => setAddOptions(e.target.value)} />
            <button type="submit">add item</button>
          </form>
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>PATCH /cart/:itemId</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await updateCartQuantity(updateItemId, { quantity: Number(updateQuantityValue) });
              setMessage('PATCH /cart/:itemId success');
              await loadCart();
            });
          }}>
            <label htmlFor="updateItemId">itemId</label>
            <input id="updateItemId" value={updateItemId} onChange={(e) => setUpdateItemId(e.target.value)} required />
            <label htmlFor="updateQuantityValue">quantity</label>
            <input id="updateQuantityValue" value={updateQuantityValue} onChange={(e) => setUpdateQuantityValue(e.target.value)} required />
            <button type="submit">update quantity</button>
          </form>
        </div>

        <div className="panel">
          <h2>DELETE /cart/:itemId</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await removeCartItem(deleteItemId);
              setMessage('DELETE /cart/:itemId success');
              await loadCart();
            });
          }}>
            <label htmlFor="deleteItemId">itemId</label>
            <input id="deleteItemId" value={deleteItemId} onChange={(e) => setDeleteItemId(e.target.value)} required />
            <button type="submit">delete item</button>
          </form>

          <h2 className="mt-12">DELETE /cart</h2>
          <div className="button-row">
            <button
              type="button"
              onClick={() => run(async () => {
                await clearCart();
                setMessage('DELETE /cart success');
                await loadCart();
              })}
            >
              clear cart
            </button>
          </div>
        </div>
      </div>
    </section>
  );
}
