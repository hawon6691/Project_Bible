import { FormEvent, useEffect, useState } from 'react';
import {
  createSellerAdmin,
  fetchSellerById,
  fetchSellers,
  removeSellerAdmin,
  updateSellerAdmin,
} from '@/lib/endpoints';

type SellerItem = {
  id: number;
  name: string;
  url: string;
  logoUrl: string | null;
  description: string | null;
  trustScore?: number;
  createdAt?: string;
};

export default function SellerApiPage() {
  const [sellers, setSellers] = useState<SellerItem[]>([]);
  const [detailId, setDetailId] = useState('');
  const [detail, setDetail] = useState<SellerItem | null>(null);

  const [createName, setCreateName] = useState('');
  const [createUrl, setCreateUrl] = useState('');
  const [createLogoUrl, setCreateLogoUrl] = useState('');
  const [createDescription, setCreateDescription] = useState('');

  const [updateId, setUpdateId] = useState('');
  const [updateName, setUpdateName] = useState('');
  const [updateUrl, setUpdateUrl] = useState('');
  const [updateLogoUrl, setUpdateLogoUrl] = useState('');
  const [updateDescription, setUpdateDescription] = useState('');

  const [deleteId, setDeleteId] = useState('');

  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const loadSellers = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await fetchSellers(1, 50);
      setSellers(res.data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'load failed');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSellers();
  }, []);

  return (
    <section>
      <h1>Seller API Step</h1>
      <p className="sub">spec step 6 - seller API integration</p>
      {loading ? <p>loading...</p> : null}
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>GET /sellers</h2>
          <ul className="list">
            {sellers.map((s) => (
              <li key={s.id}>
                {s.id} / {s.name} / {s.url}
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>GET /sellers/:id</h2>
          <form
            className="form-box"
            onSubmit={async (e: FormEvent) => {
              e.preventDefault();
              setError('');
              try {
                const res = await fetchSellerById(Number(detailId));
                setDetail(res.data as SellerItem);
                setMessage('GET /sellers/:id success');
              } catch (err) {
                setError(err instanceof Error ? err.message : 'detail failed');
                setDetail(null);
              }
            }}
          >
            <label htmlFor="detailId">seller id</label>
            <input id="detailId" value={detailId} onChange={(e) => setDetailId(e.target.value)} required />
            <button type="submit">find one</button>
          </form>

          {detail ? (
            <div className="mt-12">
              <p>name: {detail.name}</p>
              <p>url: {detail.url}</p>
              <p>description: {detail.description || '-'}</p>
              <p>trustScore: {detail.trustScore ?? '-'}</p>
            </div>
          ) : null}
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>POST /sellers (Admin)</h2>
          <form
            className="form-box"
            onSubmit={async (e: FormEvent) => {
              e.preventDefault();
              setError('');
              try {
                await createSellerAdmin({
                  name: createName,
                  url: createUrl,
                  ...(createLogoUrl ? { logoUrl: createLogoUrl } : {}),
                  ...(createDescription ? { description: createDescription } : {}),
                });
                setMessage('created seller');
                setCreateName('');
                setCreateUrl('');
                setCreateLogoUrl('');
                setCreateDescription('');
                await loadSellers();
              } catch (err) {
                setError(err instanceof Error ? err.message : 'create failed');
              }
            }}
          >
            <label htmlFor="createName">name</label>
            <input id="createName" value={createName} onChange={(e) => setCreateName(e.target.value)} required />
            <label htmlFor="createUrl">url</label>
            <input id="createUrl" value={createUrl} onChange={(e) => setCreateUrl(e.target.value)} required />
            <label htmlFor="createLogoUrl">logoUrl(optional)</label>
            <input id="createLogoUrl" value={createLogoUrl} onChange={(e) => setCreateLogoUrl(e.target.value)} />
            <label htmlFor="createDescription">description(optional)</label>
            <input id="createDescription" value={createDescription} onChange={(e) => setCreateDescription(e.target.value)} />
            <button type="submit">create</button>
          </form>
        </div>

        <div className="panel">
          <h2>PATCH /sellers/:id (Admin)</h2>
          <form
            className="form-box"
            onSubmit={async (e: FormEvent) => {
              e.preventDefault();
              setError('');
              try {
                await updateSellerAdmin(Number(updateId), {
                  ...(updateName ? { name: updateName } : {}),
                  ...(updateUrl ? { url: updateUrl } : {}),
                  ...(updateLogoUrl ? { logoUrl: updateLogoUrl } : {}),
                  ...(updateDescription ? { description: updateDescription } : {}),
                });
                setMessage('updated seller');
                await loadSellers();
              } catch (err) {
                setError(err instanceof Error ? err.message : 'update failed');
              }
            }}
          >
            <label htmlFor="updateId">id</label>
            <input id="updateId" value={updateId} onChange={(e) => setUpdateId(e.target.value)} required />
            <label htmlFor="updateName">name(optional)</label>
            <input id="updateName" value={updateName} onChange={(e) => setUpdateName(e.target.value)} />
            <label htmlFor="updateUrl">url(optional)</label>
            <input id="updateUrl" value={updateUrl} onChange={(e) => setUpdateUrl(e.target.value)} />
            <label htmlFor="updateLogoUrl">logoUrl(optional)</label>
            <input id="updateLogoUrl" value={updateLogoUrl} onChange={(e) => setUpdateLogoUrl(e.target.value)} />
            <label htmlFor="updateDescription">description(optional)</label>
            <input id="updateDescription" value={updateDescription} onChange={(e) => setUpdateDescription(e.target.value)} />
            <button type="submit">update</button>
          </form>

          <h2 className="mt-12">DELETE /sellers/:id (Admin)</h2>
          <form
            className="form-box"
            onSubmit={async (e: FormEvent) => {
              e.preventDefault();
              setError('');
              try {
                await removeSellerAdmin(Number(deleteId));
                setMessage('deleted seller');
                setDeleteId('');
                await loadSellers();
              } catch (err) {
                setError(err instanceof Error ? err.message : 'delete failed');
              }
            }}
          >
            <label htmlFor="deleteId">id</label>
            <input id="deleteId" value={deleteId} onChange={(e) => setDeleteId(e.target.value)} required />
            <button type="submit">delete</button>
          </form>
        </div>
      </div>
    </section>
  );
}
