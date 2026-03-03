import { FormEvent, useState } from 'react';
import {
  fetchWishlist,
  removeWishlist,
  toggleWishlist,
} from '@/lib/endpoints';

export default function WishlistApiPage() {
  const [items, setItems] = useState<any[]>([]);
  const [toggleProductId, setToggleProductId] = useState('');
  const [deleteProductId, setDeleteProductId] = useState('');

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

  const loadWishlist = () => run(async () => {
    const res = await fetchWishlist(1, 30);
    setItems(res.data as any[]);
    setMessage('GET /wishlist success');
  });

  return (
    <section>
      <h1>Wishlist API Step</h1>
      <p className="sub">spec step 13 - wishlist API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>GET /wishlist</h2>
          <div className="button-row">
            <button type="button" onClick={loadWishlist}>load wishlist</button>
          </div>

          <ul className="list mt-12">
            {items.map((item) => (
              <li key={item.id}>
                {item.id} / product {item.product?.id} / {item.product?.name} / lowest {Number(item.product?.lowestPrice || 0).toLocaleString()}
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>POST /wishlist/:productId (toggle)</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await toggleWishlist(Number(toggleProductId));
              setMessage(`POST /wishlist/:productId success (wishlisted=${String(res.data.wishlisted)})`);
              await loadWishlist();
            });
          }}>
            <label htmlFor="toggleProductId">productId</label>
            <input id="toggleProductId" value={toggleProductId} onChange={(e) => setToggleProductId(e.target.value)} required />
            <button type="submit">toggle wishlist</button>
          </form>
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>DELETE /wishlist/:productId</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await removeWishlist(Number(deleteProductId));
              setMessage('DELETE /wishlist/:productId success');
              await loadWishlist();
            });
          }}>
            <label htmlFor="deleteProductId">productId</label>
            <input id="deleteProductId" value={deleteProductId} onChange={(e) => setDeleteProductId(e.target.value)} required />
            <button type="submit">remove wishlist</button>
          </form>
        </div>
      </div>
    </section>
  );
}
