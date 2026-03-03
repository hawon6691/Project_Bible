import { FormEvent, useEffect, useState } from 'react';
import {
  addProductImageAdmin,
  addProductOptionAdmin,
  createProductAdmin,
  fetchCategories,
  fetchProduct,
  fetchProducts,
  removeProductAdmin,
  removeProductImageAdmin,
  removeProductOptionAdmin,
  updateProductAdmin,
  updateProductOptionAdmin,
} from '@/lib/endpoints';
import type { Category, ProductDetail, ProductSummary } from '@/lib/types';

function flattenTree(items: Category[]): Array<{ id: number; name: string }> {
  const out: Array<{ id: number; name: string }> = [];
  const walk = (nodes: Category[]) => {
    nodes.forEach((n) => {
      out.push({ id: n.id, name: n.name });
      if (n.children?.length) walk(n.children);
    });
  };
  walk(items);
  return out;
}

export default function ProductApiPage() {
  const [categories, setCategories] = useState<Array<{ id: number; name: string }>>([]);
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [detailId, setDetailId] = useState('');
  const [detail, setDetail] = useState<ProductDetail | null>(null);

  const [createName, setCreateName] = useState('');
  const [createDesc, setCreateDesc] = useState('');
  const [createPrice, setCreatePrice] = useState('');
  const [createDiscount, setCreateDiscount] = useState('');
  const [createStock, setCreateStock] = useState('0');
  const [createCategoryId, setCreateCategoryId] = useState('');
  const [createStatus, setCreateStatus] = useState<'ON_SALE' | 'SOLD_OUT' | 'HIDDEN'>('ON_SALE');
  const [createThumb, setCreateThumb] = useState('');

  const [updateId, setUpdateId] = useState('');
  const [updateName, setUpdateName] = useState('');
  const [updatePrice, setUpdatePrice] = useState('');
  const [updateStock, setUpdateStock] = useState('');

  const [deleteId, setDeleteId] = useState('');

  const [optionProductId, setOptionProductId] = useState('');
  const [optionId, setOptionId] = useState('');
  const [optionName, setOptionName] = useState('');
  const [optionValuesCsv, setOptionValuesCsv] = useState('');

  const [imageProductId, setImageProductId] = useState('');
  const [imageId, setImageId] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [imageSortOrder, setImageSortOrder] = useState('0');
  const [imageIsMain, setImageIsMain] = useState(false);

  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const loadInitial = async () => {
    setLoading(true);
    setError('');
    try {
      const [categoryRes, productRes] = await Promise.all([
        fetchCategories(),
        fetchProducts({ page: 1, limit: 20, sort: 'newest' }),
      ]);
      setCategories(flattenTree(categoryRes.data));
      setProducts(productRes.data);
      if (!createCategoryId && categoryRes.data.length) {
        const first = flattenTree(categoryRes.data)[0];
        if (first) setCreateCategoryId(String(first.id));
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'load failed');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadInitial();
  }, []);

  const parseValues = (csv: string) =>
    csv
      .split(',')
      .map((v) => v.trim())
      .filter(Boolean);

  return (
    <section>
      <h1>Product API Step</h1>
      <p className="sub">spec step 4 - product API integration</p>
      {loading ? <p>loading...</p> : null}
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>GET /products</h2>
          <ul className="list">
            {products.map((p) => (
              <li key={p.id}>{p.id} / {p.name} / {Number(p.lowestPrice || 0).toLocaleString()} KRW</li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>GET /products/:id</h2>
          <form
            className="form-box"
            onSubmit={async (e: FormEvent) => {
              e.preventDefault();
              try {
                const res = await fetchProduct(Number(detailId));
                setDetail(res.data);
                setMessage('GET /products/:id success');
              } catch (err) {
                setError(err instanceof Error ? err.message : 'detail failed');
                setDetail(null);
              }
            }}
          >
            <label htmlFor="detailId">product id</label>
            <input id="detailId" value={detailId} onChange={(e) => setDetailId(e.target.value)} required />
            <button type="submit">find one</button>
          </form>
          {detail ? (
            <div className="mt-12">
              <p>name: {detail.name}</p>
              <p>price: {detail.price}</p>
              <p>stock: {detail.stock}</p>
              <p>options: {detail.options.length}</p>
              <p>images: {detail.images.length}</p>
            </div>
          ) : null}
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>POST /products (Admin)</h2>
          <form
            className="form-box"
            onSubmit={async (e: FormEvent) => {
              e.preventDefault();
              setError('');
              try {
                const res = await createProductAdmin({
                  name: createName,
                  description: createDesc,
                  price: Number(createPrice),
                  ...(createDiscount ? { discountPrice: Number(createDiscount) } : {}),
                  stock: Number(createStock),
                  categoryId: Number(createCategoryId),
                  status: createStatus,
                  ...(createThumb ? { thumbnailUrl: createThumb } : {}),
                });
                setMessage(`created product id=${res.data.id}`);
                setCreateName('');
                setCreateDesc('');
                setCreatePrice('');
                setCreateDiscount('');
                setCreateStock('0');
                setCreateThumb('');
                await loadInitial();
              } catch (err) {
                setError(err instanceof Error ? err.message : 'create failed');
              }
            }}
          >
            <label htmlFor="createName">name</label>
            <input id="createName" value={createName} onChange={(e) => setCreateName(e.target.value)} required />
            <label htmlFor="createDesc">description</label>
            <input id="createDesc" value={createDesc} onChange={(e) => setCreateDesc(e.target.value)} required />
            <label htmlFor="createPrice">price</label>
            <input id="createPrice" value={createPrice} onChange={(e) => setCreatePrice(e.target.value)} required />
            <label htmlFor="createDiscount">discountPrice(optional)</label>
            <input id="createDiscount" value={createDiscount} onChange={(e) => setCreateDiscount(e.target.value)} />
            <label htmlFor="createStock">stock</label>
            <input id="createStock" value={createStock} onChange={(e) => setCreateStock(e.target.value)} required />
            <label htmlFor="createCategoryId">categoryId</label>
            <select id="createCategoryId" value={createCategoryId} onChange={(e) => setCreateCategoryId(e.target.value)} required>
              <option value="">select category</option>
              {categories.map((c) => (
                <option key={c.id} value={String(c.id)}>{c.name} ({c.id})</option>
              ))}
            </select>
            <label htmlFor="createStatus">status</label>
            <select id="createStatus" value={createStatus} onChange={(e) => setCreateStatus(e.target.value as 'ON_SALE' | 'SOLD_OUT' | 'HIDDEN')}>
              <option value="ON_SALE">ON_SALE</option>
              <option value="SOLD_OUT">SOLD_OUT</option>
              <option value="HIDDEN">HIDDEN</option>
            </select>
            <label htmlFor="createThumb">thumbnailUrl(optional)</label>
            <input id="createThumb" value={createThumb} onChange={(e) => setCreateThumb(e.target.value)} />
            <button type="submit">create</button>
          </form>
        </div>

        <div className="panel">
          <h2>PATCH /products/:id (Admin)</h2>
          <form
            className="form-box"
            onSubmit={async (e: FormEvent) => {
              e.preventDefault();
              setError('');
              try {
                await updateProductAdmin(Number(updateId), {
                  ...(updateName ? { name: updateName } : {}),
                  ...(updatePrice ? { price: Number(updatePrice) } : {}),
                  ...(updateStock ? { stock: Number(updateStock) } : {}),
                });
                setMessage('updated product');
                await loadInitial();
              } catch (err) {
                setError(err instanceof Error ? err.message : 'update failed');
              }
            }}
          >
            <label htmlFor="updateId">id</label>
            <input id="updateId" value={updateId} onChange={(e) => setUpdateId(e.target.value)} required />
            <label htmlFor="updateName">name(optional)</label>
            <input id="updateName" value={updateName} onChange={(e) => setUpdateName(e.target.value)} />
            <label htmlFor="updatePrice">price(optional)</label>
            <input id="updatePrice" value={updatePrice} onChange={(e) => setUpdatePrice(e.target.value)} />
            <label htmlFor="updateStock">stock(optional)</label>
            <input id="updateStock" value={updateStock} onChange={(e) => setUpdateStock(e.target.value)} />
            <button type="submit">update</button>
          </form>

          <h2 className="mt-12">DELETE /products/:id (Admin)</h2>
          <form
            className="form-box"
            onSubmit={async (e: FormEvent) => {
              e.preventDefault();
              setError('');
              try {
                await removeProductAdmin(Number(deleteId));
                setMessage('deleted product');
                await loadInitial();
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

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>Options API (Admin)</h2>
          <form
            className="form-box"
            onSubmit={async (e: FormEvent) => {
              e.preventDefault();
              try {
                await addProductOptionAdmin(Number(optionProductId), {
                  name: optionName,
                  values: parseValues(optionValuesCsv),
                });
                setMessage('option added');
              } catch (err) {
                setError(err instanceof Error ? err.message : 'add option failed');
              }
            }}
          >
            <label htmlFor="optionProductId">productId</label>
            <input id="optionProductId" value={optionProductId} onChange={(e) => setOptionProductId(e.target.value)} required />
            <label htmlFor="optionName">option name</label>
            <input id="optionName" value={optionName} onChange={(e) => setOptionName(e.target.value)} required />
            <label htmlFor="optionValues">values(csv)</label>
            <input id="optionValues" value={optionValuesCsv} onChange={(e) => setOptionValuesCsv(e.target.value)} placeholder="실버,그라파이트" required />
            <button type="submit">POST /products/:id/options</button>
          </form>

          <form
            className="form-box mt-12"
            onSubmit={async (e: FormEvent) => {
              e.preventDefault();
              try {
                await updateProductOptionAdmin(Number(optionProductId), Number(optionId), {
                  name: optionName,
                  values: parseValues(optionValuesCsv),
                });
                setMessage('option updated');
              } catch (err) {
                setError(err instanceof Error ? err.message : 'update option failed');
              }
            }}
          >
            <label htmlFor="optionId">optionId</label>
            <input id="optionId" value={optionId} onChange={(e) => setOptionId(e.target.value)} required />
            <button type="submit">PATCH /products/:id/options/:optionId</button>
          </form>

          <form
            className="form-box mt-12"
            onSubmit={async (e: FormEvent) => {
              e.preventDefault();
              try {
                await removeProductOptionAdmin(Number(optionProductId), Number(optionId));
                setMessage('option removed');
              } catch (err) {
                setError(err instanceof Error ? err.message : 'remove option failed');
              }
            }}
          >
            <button type="submit">DELETE /products/:id/options/:optionId</button>
          </form>
        </div>

        <div className="panel">
          <h2>Images API (Admin)</h2>
          <form
            className="form-box"
            onSubmit={async (e: FormEvent) => {
              e.preventDefault();
              try {
                await addProductImageAdmin(Number(imageProductId), {
                  url: imageUrl,
                  isMain: imageIsMain,
                  sortOrder: Number(imageSortOrder || 0),
                });
                setMessage('image added');
              } catch (err) {
                setError(err instanceof Error ? err.message : 'add image failed');
              }
            }}
          >
            <label htmlFor="imageProductId">productId</label>
            <input id="imageProductId" value={imageProductId} onChange={(e) => setImageProductId(e.target.value)} required />
            <label htmlFor="imageUrl">image url</label>
            <input id="imageUrl" value={imageUrl} onChange={(e) => setImageUrl(e.target.value)} required />
            <label htmlFor="imageSort">sortOrder</label>
            <input id="imageSort" value={imageSortOrder} onChange={(e) => setImageSortOrder(e.target.value)} />
            <label htmlFor="imageMain">isMain</label>
            <input id="imageMain" type="checkbox" checked={imageIsMain} onChange={(e) => setImageIsMain(e.target.checked)} />
            <button type="submit">POST /products/:id/images</button>
          </form>

          <form
            className="form-box mt-12"
            onSubmit={async (e: FormEvent) => {
              e.preventDefault();
              try {
                await removeProductImageAdmin(Number(imageProductId), Number(imageId));
                setMessage('image removed');
              } catch (err) {
                setError(err instanceof Error ? err.message : 'remove image failed');
              }
            }}
          >
            <label htmlFor="imageId">imageId</label>
            <input id="imageId" value={imageId} onChange={(e) => setImageId(e.target.value)} required />
            <button type="submit">DELETE /products/:id/images/:imageId</button>
          </form>
        </div>
      </div>
    </section>
  );
}
