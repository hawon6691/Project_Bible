import { useEffect, useMemo, useState } from 'react';
import { clearCart, createOrder, fetchAddresses, fetchCart, removeCartItem, updateCartQuantity } from '@/lib/endpoints';
import { getAccessToken } from '@/lib/auth';
import type { Address, CartItem } from '@/lib/types';

export default function CartPage() {
  const [items, setItems] = useState<CartItem[]>([]);
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [selectedAddressId, setSelectedAddressId] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const loadData = async () => {
    setLoading(true);
    try {
      const cartRes = await fetchCart();
      setItems(cartRes.data);

      if (getAccessToken()) {
        const addressRes = await fetchAddresses();
        setAddresses(addressRes.data);
        const defaultAddress = addressRes.data.find((item) => item.isDefault) || addressRes.data[0];
        if (defaultAddress) {
          setSelectedAddressId(String(defaultAddress.id));
        }
      } else {
        setAddresses([]);
      }
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'cart load failed';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const totalPrice = useMemo(() => {
    return items.reduce((sum, item) => {
      const unitPrice = Number(item.product?.lowestPrice ?? item.product?.price ?? 0);
      return sum + unitPrice * Number(item.quantity || 0);
    }, 0);
  }, [items]);

  return (
    <section>
      <h1>Cart</h1>
      {loading ? <p>loading...</p> : null}
      {error ? <p className="error">{error}</p> : null}
      {message ? <p className="sub">{message}</p> : null}

      <ul className="cart-list">
        {items.map((item) => (
          <li key={String(item.id)}>
            <div>
              <strong>{item.product?.name || 'deleted product'}</strong>
              <p>{item.seller?.name || 'no seller'}</p>
            </div>
            <div className="item-actions">
              <input
                type="number"
                min={1}
                value={item.quantity}
                onChange={(e) => {
                  const nextQuantity = Number(e.target.value || 1);
                  setItems((prev) => prev.map((row) => (row.id === item.id ? { ...row, quantity: nextQuantity } : row)));
                }}
              />
              <button
                type="button"
                onClick={async () => {
                  try {
                    await updateCartQuantity(item.id, { quantity: item.quantity });
                    setMessage('quantity updated');
                  } catch (err) {
                    const msg = err instanceof Error ? err.message : 'update failed';
                    setMessage(msg);
                  }
                }}
              >
                Update API
              </button>
              <button
                type="button"
                onClick={async () => {
                  try {
                    await removeCartItem(item.id);
                    await loadData();
                    setMessage('item removed');
                  } catch (err) {
                    const msg = err instanceof Error ? err.message : 'remove failed';
                    setMessage(msg);
                  }
                }}
              >
                Remove API
              </button>
              <span>
                {Number(item.product?.lowestPrice ?? item.product?.price ?? 0).toLocaleString()} KRW x {item.quantity}
              </span>
            </div>
          </li>
        ))}
      </ul>

      <p className="total">Total: {totalPrice.toLocaleString()} KRW</p>

      <div className="button-row">
        <button
          type="button"
          onClick={async () => {
            await clearCart();
            await loadData();
            setMessage('cart cleared');
          }}
        >
          Clear Cart API
        </button>

        <button
          type="button"
          disabled={!getAccessToken() || items.length === 0 || !selectedAddressId}
          onClick={async () => {
            try {
              await createOrder({
                addressId: Number(selectedAddressId),
                fromCart: true,
                cartItemIds: items.map((item) => Number(item.id)).filter(Number.isFinite),
                items: items
                  .filter((item) => item.product?.id && item.seller?.id)
                  .map((item) => ({
                    productId: Number(item.product?.id),
                    sellerId: Number(item.seller?.id),
                    quantity: item.quantity,
                    selectedOptions: item.selectedOptions || undefined,
                  })),
              });
              setMessage('order created from cart');
              await loadData();
            } catch (err) {
              const msg = err instanceof Error ? err.message : 'order failed';
              setMessage(msg);
            }
          }}
        >
          Create Order API
        </button>
      </div>

      {getAccessToken() ? (
        <div className="form-row mt-12">
          <label htmlFor="address">address</label>
          <select id="address" value={selectedAddressId} onChange={(e) => setSelectedAddressId(e.target.value)}>
            {addresses.map((address) => (
              <option key={address.id} value={String(address.id)}>
                {address.recipientName} / {address.address}
              </option>
            ))}
          </select>
        </div>
      ) : (
        <p className="sub">order API needs login + address API data</p>
      )}
    </section>
  );
}
