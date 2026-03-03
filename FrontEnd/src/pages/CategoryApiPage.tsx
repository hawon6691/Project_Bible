import { FormEvent, useEffect, useMemo, useState } from 'react';
import {
  createCategoryAdmin,
  fetchCategories,
  fetchCategoryById,
  removeCategoryAdmin,
  updateCategoryAdmin,
} from '@/lib/endpoints';
import type { Category } from '@/lib/types';

interface CategoryDetail {
  id: number;
  name: string;
  parentId: number | null;
  sortOrder: number;
  children: Array<{ id: number; name: string; sortOrder: number }>;
  createdAt: string;
}

function flattenTree(items: Category[]): Array<{ id: number; name: string; depth: number }> {
  const result: Array<{ id: number; name: string; depth: number }> = [];

  const walk = (nodes: Category[], depth: number) => {
    nodes.forEach((node) => {
      result.push({ id: node.id, name: node.name, depth });
      if (node.children?.length) {
        walk(node.children, depth + 1);
      }
    });
  };

  walk(items, 0);
  return result;
}

export default function CategoryApiPage() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [detail, setDetail] = useState<CategoryDetail | null>(null);
  const [selectedId, setSelectedId] = useState('');

  const [createName, setCreateName] = useState('');
  const [createParentId, setCreateParentId] = useState('');

  const [updateId, setUpdateId] = useState('');
  const [updateName, setUpdateName] = useState('');
  const [updateSortOrder, setUpdateSortOrder] = useState('');

  const [removeId, setRemoveId] = useState('');

  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const flatCategories = useMemo(() => flattenTree(categories), [categories]);

  const loadCategories = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await fetchCategories();
      setCategories(res.data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'load failed');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCategories();
  }, []);

  const onFindOne = async (e: FormEvent) => {
    e.preventDefault();
    setMessage('');
    setError('');

    try {
      const res = await fetchCategoryById(Number(selectedId));
      setDetail(res.data);
      setMessage('GET /categories/:id success');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'detail failed');
      setDetail(null);
    }
  };

  const onCreate = async (e: FormEvent) => {
    e.preventDefault();
    setMessage('');
    setError('');

    try {
      await createCategoryAdmin({
        name: createName,
        ...(createParentId ? { parentId: Number(createParentId) } : {}),
      });
      setCreateName('');
      setCreateParentId('');
      setMessage('POST /categories success');
      await loadCategories();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'create failed');
    }
  };

  const onUpdate = async (e: FormEvent) => {
    e.preventDefault();
    setMessage('');
    setError('');

    try {
      await updateCategoryAdmin(Number(updateId), {
        ...(updateName ? { name: updateName } : {}),
        ...(updateSortOrder ? { sortOrder: Number(updateSortOrder) } : {}),
      });
      setMessage('PATCH /categories/:id success');
      await loadCategories();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'update failed');
    }
  };

  const onRemove = async (e: FormEvent) => {
    e.preventDefault();
    setMessage('');
    setError('');

    try {
      await removeCategoryAdmin(Number(removeId));
      setMessage('DELETE /categories/:id success');
      await loadCategories();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'delete failed');
    }
  };

  return (
    <section>
      <h1>Category API Step</h1>
      <p className="sub">spec step 3 - categories API integration</p>
      {loading ? <p>loading...</p> : null}
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>GET /categories</h2>
          <ul className="list">
            {flatCategories.map((item) => (
              <li key={item.id}>{'--'.repeat(item.depth)} {item.name} ({item.id})</li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>GET /categories/:id</h2>
          <form className="form-box" onSubmit={onFindOne}>
            <label htmlFor="catFindId">category id</label>
            <input id="catFindId" value={selectedId} onChange={(e) => setSelectedId(e.target.value)} required />
            <button type="submit">find one</button>
          </form>
          {detail ? (
            <div className="mt-12">
              <p>id: {detail.id}</p>
              <p>name: {detail.name}</p>
              <p>parentId: {detail.parentId ?? 'null'}</p>
              <p>children: {detail.children.length}</p>
            </div>
          ) : null}
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>POST /categories (Admin)</h2>
          <form className="form-box" onSubmit={onCreate}>
            <label htmlFor="catCreateName">name</label>
            <input id="catCreateName" value={createName} onChange={(e) => setCreateName(e.target.value)} required />
            <label htmlFor="catCreateParentId">parentId(optional)</label>
            <input id="catCreateParentId" value={createParentId} onChange={(e) => setCreateParentId(e.target.value)} />
            <button type="submit">create</button>
          </form>
        </div>

        <div className="panel">
          <h2>PATCH /categories/:id (Admin)</h2>
          <form className="form-box" onSubmit={onUpdate}>
            <label htmlFor="catUpdateId">id</label>
            <input id="catUpdateId" value={updateId} onChange={(e) => setUpdateId(e.target.value)} required />
            <label htmlFor="catUpdateName">name(optional)</label>
            <input id="catUpdateName" value={updateName} onChange={(e) => setUpdateName(e.target.value)} />
            <label htmlFor="catUpdateSort">sortOrder(optional)</label>
            <input id="catUpdateSort" value={updateSortOrder} onChange={(e) => setUpdateSortOrder(e.target.value)} />
            <button type="submit">update</button>
          </form>
        </div>

        <div className="panel">
          <h2>DELETE /categories/:id (Admin)</h2>
          <form className="form-box" onSubmit={onRemove}>
            <label htmlFor="catDeleteId">id</label>
            <input id="catDeleteId" value={removeId} onChange={(e) => setRemoveId(e.target.value)} required />
            <button type="submit">delete</button>
          </form>
        </div>
      </div>
    </section>
  );
}
