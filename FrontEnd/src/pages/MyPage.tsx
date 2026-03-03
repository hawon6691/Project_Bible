import { FormEvent, useEffect, useState } from 'react';
import { createAddress, fetchAddresses, fetchMe, fetchMyOrders, fetchWishlist, removeAddress, updateMe } from '@/lib/endpoints';
import { getAccessToken } from '@/lib/auth';
import type { Address, OrderSummary, UserProfile, WishlistItem } from '@/lib/types';

export default function MyPage() {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [wishlist, setWishlist] = useState<WishlistItem[]>([]);
  const [orders, setOrders] = useState<OrderSummary[]>([]);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');

  const [addrRecipientName, setAddrRecipientName] = useState('');
  const [addrPhone, setAddrPhone] = useState('');
  const [zipCode, setZipCode] = useState('');
  const [address, setAddress] = useState('');
  const [addressDetail, setAddressDetail] = useState('');

  const loadData = async () => {
    if (!getAccessToken()) {
      setError('login required');
      setLoading(false);
      return;
    }

    setLoading(true);
    setError('');

    try {
      const [meRes, addrRes, wishlistRes, orderRes] = await Promise.all([
        fetchMe(),
        fetchAddresses(),
        fetchWishlist(1, 10),
        fetchMyOrders(1, 10),
      ]);

      setProfile(meRes.data);
      setName(meRes.data.name);
      setPhone(meRes.data.phone);
      setAddresses(addrRes.data);
      setWishlist(wishlistRes.data);
      setOrders(orderRes.data);
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'load failed';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const onUpdateProfile = async (e: FormEvent) => {
    e.preventDefault();
    try {
      await updateMe({ name, phone });
      setMessage('profile updated');
      await loadData();
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'update failed';
      setMessage(msg);
    }
  };

  const onCreateAddress = async (e: FormEvent) => {
    e.preventDefault();
    try {
      await createAddress({
        recipientName: addrRecipientName,
        phone: addrPhone,
        zipCode,
        address,
        addressDetail: addressDetail || undefined,
      });
      setAddrRecipientName('');
      setAddrPhone('');
      setZipCode('');
      setAddress('');
      setAddressDetail('');
      setMessage('address created');
      await loadData();
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'address failed';
      setMessage(msg);
    }
  };

  if (loading) return <p>loading...</p>;

  return (
    <section>
      <h1>MyPage API Integration</h1>
      {error ? <p className="error">{error}</p> : null}
      {message ? <p className="sub">{message}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>User API</h2>
          {profile ? (
            <div>
              <p>email: {profile.email}</p>
              <p>point: {profile.point}</p>
              <form className="form-box" onSubmit={onUpdateProfile}>
                <label htmlFor="myName">name</label>
                <input id="myName" value={name} onChange={(e) => setName(e.target.value)} required />
                <label htmlFor="myPhone">phone</label>
                <input id="myPhone" value={phone} onChange={(e) => setPhone(e.target.value)} required />
                <button type="submit">Update User API</button>
              </form>
            </div>
          ) : null}
        </div>

        <div className="panel">
          <h2>Address API</h2>
          <ul className="list">
            {addresses.map((item) => (
              <li key={item.id}>
                {item.recipientName} / {item.address}
                <button
                  type="button"
                  className="mini-btn"
                  onClick={async () => {
                    await removeAddress(item.id);
                    await loadData();
                  }}
                >
                  delete
                </button>
              </li>
            ))}
          </ul>
          <form className="form-box" onSubmit={onCreateAddress}>
            <label htmlFor="addrRecipient">recipientName</label>
            <input id="addrRecipient" value={addrRecipientName} onChange={(e) => setAddrRecipientName(e.target.value)} required />
            <label htmlFor="addrPhone">phone</label>
            <input id="addrPhone" value={addrPhone} onChange={(e) => setAddrPhone(e.target.value)} required />
            <label htmlFor="addrZip">zipCode</label>
            <input id="addrZip" value={zipCode} onChange={(e) => setZipCode(e.target.value)} required />
            <label htmlFor="addrAddress">address</label>
            <input id="addrAddress" value={address} onChange={(e) => setAddress(e.target.value)} required />
            <label htmlFor="addrDetail">addressDetail</label>
            <input id="addrDetail" value={addressDetail} onChange={(e) => setAddressDetail(e.target.value)} />
            <button type="submit">Create Address API</button>
          </form>
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>Wishlist API</h2>
          <ul className="list">
            {wishlist.map((item) => (
              <li key={item.id}>{item.product.name}</li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>Order API</h2>
          <ul className="list">
            {orders.map((order) => (
              <li key={order.id}>{order.orderNumber} / {order.status}</li>
            ))}
          </ul>
        </div>
      </div>
    </section>
  );
}
