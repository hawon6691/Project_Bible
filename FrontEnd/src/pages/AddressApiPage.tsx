import { FormEvent, useState } from 'react';
import {
  createAddress,
  fetchAddresses,
  removeAddress,
  updateAddress,
} from '@/lib/endpoints';

export default function AddressApiPage() {
  const [addresses, setAddresses] = useState<any[]>([]);

  const [createRecipientName, setCreateRecipientName] = useState('');
  const [createPhone, setCreatePhone] = useState('');
  const [createZipCode, setCreateZipCode] = useState('');
  const [createAddressValue, setCreateAddressValue] = useState('');
  const [createAddressDetail, setCreateAddressDetail] = useState('');
  const [createIsDefault, setCreateIsDefault] = useState(false);

  const [updateId, setUpdateId] = useState('');
  const [updateRecipientName, setUpdateRecipientName] = useState('');
  const [updatePhone, setUpdatePhone] = useState('');
  const [updateZipCode, setUpdateZipCode] = useState('');
  const [updateAddressValue, setUpdateAddressValue] = useState('');
  const [updateAddressDetail, setUpdateAddressDetail] = useState('');
  const [updateIsDefault, setUpdateIsDefault] = useState(false);

  const [deleteId, setDeleteId] = useState('');

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

  const loadAddresses = () => run(async () => {
    const res = await fetchAddresses();
    setAddresses(res.data as any[]);
    setMessage('GET /addresses success');
  });

  return (
    <section>
      <h1>Address API Step</h1>
      <p className="sub">spec step 9 - address API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>GET /addresses</h2>
          <div className="button-row">
            <button type="button" onClick={loadAddresses}>load addresses</button>
          </div>

          <ul className="list mt-12">
            {addresses.map((a) => (
              <li key={a.id}>
                {a.id} / {a.recipientName} / {a.phone} / {a.address}
                {a.addressDetail ? ` ${a.addressDetail}` : ''} / default {String(a.isDefault)}
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>POST /addresses</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await createAddress({
                recipientName: createRecipientName,
                phone: createPhone,
                zipCode: createZipCode,
                address: createAddressValue,
                ...(createAddressDetail ? { addressDetail: createAddressDetail } : {}),
                isDefault: createIsDefault,
              });
              setMessage('POST /addresses success');
              await loadAddresses();
            });
          }}>
            <label htmlFor="createRecipientName">recipientName</label>
            <input id="createRecipientName" value={createRecipientName} onChange={(e) => setCreateRecipientName(e.target.value)} required />
            <label htmlFor="createPhone">phone</label>
            <input id="createPhone" value={createPhone} onChange={(e) => setCreatePhone(e.target.value)} required />
            <label htmlFor="createZipCode">zipCode</label>
            <input id="createZipCode" value={createZipCode} onChange={(e) => setCreateZipCode(e.target.value)} required />
            <label htmlFor="createAddressValue">address</label>
            <input id="createAddressValue" value={createAddressValue} onChange={(e) => setCreateAddressValue(e.target.value)} required />
            <label htmlFor="createAddressDetail">addressDetail(optional)</label>
            <input id="createAddressDetail" value={createAddressDetail} onChange={(e) => setCreateAddressDetail(e.target.value)} />
            <label htmlFor="createIsDefault">isDefault</label>
            <input id="createIsDefault" type="checkbox" checked={createIsDefault} onChange={(e) => setCreateIsDefault(e.target.checked)} />
            <button type="submit">create address</button>
          </form>
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>PATCH /addresses/:id</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await updateAddress(Number(updateId), {
                ...(updateRecipientName ? { recipientName: updateRecipientName } : {}),
                ...(updatePhone ? { phone: updatePhone } : {}),
                ...(updateZipCode ? { zipCode: updateZipCode } : {}),
                ...(updateAddressValue ? { address: updateAddressValue } : {}),
                ...(updateAddressDetail ? { addressDetail: updateAddressDetail } : {}),
                ...(updateIsDefault ? { isDefault: updateIsDefault } : {}),
              });
              setMessage('PATCH /addresses/:id success');
              await loadAddresses();
            });
          }}>
            <label htmlFor="updateId">id</label>
            <input id="updateId" value={updateId} onChange={(e) => setUpdateId(e.target.value)} required />
            <label htmlFor="updateRecipientName">recipientName(optional)</label>
            <input id="updateRecipientName" value={updateRecipientName} onChange={(e) => setUpdateRecipientName(e.target.value)} />
            <label htmlFor="updatePhone">phone(optional)</label>
            <input id="updatePhone" value={updatePhone} onChange={(e) => setUpdatePhone(e.target.value)} />
            <label htmlFor="updateZipCode">zipCode(optional)</label>
            <input id="updateZipCode" value={updateZipCode} onChange={(e) => setUpdateZipCode(e.target.value)} />
            <label htmlFor="updateAddressValue">address(optional)</label>
            <input id="updateAddressValue" value={updateAddressValue} onChange={(e) => setUpdateAddressValue(e.target.value)} />
            <label htmlFor="updateAddressDetail">addressDetail(optional)</label>
            <input id="updateAddressDetail" value={updateAddressDetail} onChange={(e) => setUpdateAddressDetail(e.target.value)} />
            <label htmlFor="updateIsDefault">isDefault</label>
            <input id="updateIsDefault" type="checkbox" checked={updateIsDefault} onChange={(e) => setUpdateIsDefault(e.target.checked)} />
            <button type="submit">update address</button>
          </form>
        </div>

        <div className="panel">
          <h2>DELETE /addresses/:id</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await removeAddress(Number(deleteId));
              setMessage('DELETE /addresses/:id success');
              await loadAddresses();
            });
          }}>
            <label htmlFor="deleteId">id</label>
            <input id="deleteId" value={deleteId} onChange={(e) => setDeleteId(e.target.value)} required />
            <button type="submit">delete address</button>
          </form>
        </div>
      </div>
    </section>
  );
}
