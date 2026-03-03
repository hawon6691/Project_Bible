import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { fetchProducts } from '@/lib/endpoints';
import type { ProductSummary } from '@/lib/types';

export default function ProductsPage() {
  const [params, setParams] = useSearchParams();
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const page = Number(params.get('page') || 1);
  const categoryId = params.get('categoryId') ? Number(params.get('categoryId')) : undefined;
  const search = params.get('search') || undefined;

  useEffect(() => {
    setLoading(true);
    fetchProducts({ page, limit: 20, categoryId, search, sort: 'newest' })
      .then((res) => {
        setProducts(res.data);
        setTotal(Number(res.meta?.totalCount || 0));
      })
      .catch((err: Error) => {
        setError(err.message || '상품 목록을 불러오지 못했습니다.');
      })
      .finally(() => setLoading(false));
  }, [page, categoryId, search]);

  return (
    <section>
      <h1>상품 목록</h1>
      <form
        className="search-row"
        onSubmit={(e) => {
          e.preventDefault();
          const fd = new FormData(e.currentTarget);
          const keyword = String(fd.get('keyword') || '').trim();
          setParams((prev) => {
            const next = new URLSearchParams(prev);
            next.set('page', '1');
            if (keyword) next.set('search', keyword);
            else next.delete('search');
            return next;
          });
        }}
      >
        <input name="keyword" defaultValue={search || ''} placeholder="상품명을 검색하세요" />
        <button type="submit">검색</button>
      </form>

      {error ? <p className="error">{error}</p> : null}
      {loading ? <p>로딩중...</p> : null}

      <p className="sub">총 {total.toLocaleString()}건</p>

      <ul className="product-list">
        {products.map((p) => (
          <li key={p.id}>
            <Link to={`/products/${p.id}`}>
              <div className="thumb small">
                {p.thumbnailUrl ? <img src={p.thumbnailUrl} alt={p.name} /> : <span>NO IMAGE</span>}
              </div>
              <div>
                <strong>{p.name}</strong>
                <p>{Number(p.lowestPrice || 0).toLocaleString()}원</p>
              </div>
            </Link>
          </li>
        ))}
      </ul>

      <div className="pagination">
        <button
          type="button"
          disabled={page <= 1}
          onClick={() => setParams((prev) => {
            const next = new URLSearchParams(prev);
            next.set('page', String(page - 1));
            return next;
          })}
        >
          이전
        </button>
        <span>{page}</span>
        <button
          type="button"
          disabled={products.length < 20}
          onClick={() => setParams((prev) => {
            const next = new URLSearchParams(prev);
            next.set('page', String(page + 1));
            return next;
          })}
        >
          다음
        </button>
      </div>
    </section>
  );
}
