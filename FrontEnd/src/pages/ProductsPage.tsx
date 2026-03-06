import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { fetchProducts } from '@/lib/endpoints';
import type { ProductSummary } from '@/lib/types';

const fallbackProducts = (keyword?: string): ProductSummary[] => {
  const base: ProductSummary[] = [
    {
      id: 101,
      name: 'LG 울트라 16 노트북 16Z90S',
      lowestPrice: 1525000,
      sellerCount: 83,
      thumbnailUrl: 'https://images.unsplash.com/photo-1517336714739-489689fd1ca8?w=420&q=80&auto=format&fit=crop',
      reviewCount: 126,
      averageRating: 4.8,
      createdAt: new Date().toISOString(),
    },
    {
      id: 102,
      name: 'MSI 크리에이터 M14',
      lowestPrice: 1383000,
      sellerCount: 57,
      thumbnailUrl: 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=420&q=80&auto=format&fit=crop',
      reviewCount: 84,
      averageRating: 4.6,
      createdAt: new Date().toISOString(),
    },
    {
      id: 103,
      name: 'ASUS 비보북 Pro 15 OLED',
      lowestPrice: 1189000,
      sellerCount: 71,
      thumbnailUrl: 'https://images.unsplash.com/photo-1593642634443-44adaa06623a?w=420&q=80&auto=format&fit=crop',
      reviewCount: 63,
      averageRating: 4.5,
      createdAt: new Date().toISOString(),
    },
    {
      id: 104,
      name: 'Apple MacBook Air 13 M4',
      lowestPrice: 1649000,
      sellerCount: 45,
      thumbnailUrl: 'https://images.unsplash.com/photo-1484788984921-03950022c9ef?w=420&q=80&auto=format&fit=crop',
      reviewCount: 201,
      averageRating: 4.9,
      createdAt: new Date().toISOString(),
    },
    {
      id: 105,
      name: 'Lenovo Slim 5 16',
      lowestPrice: 1099000,
      sellerCount: 39,
      thumbnailUrl: null,
      reviewCount: 41,
      averageRating: 4.3,
      createdAt: new Date().toISOString(),
    },
  ];

  if (!keyword) return base;
  const lowered = keyword.toLowerCase();
  return base.filter((item) => item.name.toLowerCase().includes(lowered));
};

const priceLadder = (lowestPrice: number | null) => {
  const safe = lowestPrice ?? 0;
  return [
    { label: '최저가', price: safe },
    { label: '카드할인', price: Math.max(0, safe - 23000) },
    { label: '무이자', price: Math.max(0, safe - 11000) },
    { label: '정품몰', price: safe + 76000 },
  ];
};

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
    setError('');
    fetchProducts({ page, limit: 20, categoryId, search, sort: 'newest' })
      .then((res) => {
        setProducts(res.data);
        setTotal(Number(res.meta?.totalCount || 0));
      })
      .catch((err: Error) => {
        const fallback = fallbackProducts(search);
        setProducts(fallback);
        setTotal(fallback.length);
        setError(err.message || '실시간 상품 목록을 불러오지 못해 샘플 데이터를 표시합니다.');
      })
      .finally(() => setLoading(false));
  }, [page, categoryId, search]);

  return (
    <section className="product-catalog-page">
      <div className="catalog-breadcrumb">
        <Link to="/public">홈</Link>
        <span>›</span>
        <span>가전/TV</span>
        <span>›</span>
        <span>{search || '노트북'}</span>
      </div>

      <div className="catalog-shell">
        <div className="catalog-main-column">
          <div className="catalog-upper">
            <aside className="catalog-category-rail">
              <h2>전체 카테고리</h2>
              <ul>
                {['TV', '냉장고', '세탁기', '노트북', '모니터', '태블릿', '스마트폰', 'PC주변기기'].map((item) => (
                  <li key={item} className={item === '노트북' ? 'active' : ''}>{item}</li>
                ))}
              </ul>
            </aside>

            <div className="catalog-filter-board">
              <div className="catalog-title-row">
                <h1>노트북 가격비교</h1>
                <span>총 {total.toLocaleString()}건</span>
              </div>

              <div className="catalog-subtabs">
                {['노트북', '게이밍 노트북', '사무용 노트북', '맥북', '2in1', '크리에이터'].map((tab) => (
                  <button key={tab} type="button" className={tab === '노트북' ? 'active' : ''}>{tab}</button>
                ))}
              </div>

              <form
                className="search-row catalog-search-row"
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
                <input name="keyword" defaultValue={search || ''} placeholder="브랜드, 모델명, CPU로 검색" />
                <button type="submit">검색</button>
              </form>

              <div className="catalog-filter-grid">
                <div className="filter-row">
                  <strong>제조사</strong>
                  <div>
                    {['LG전자', '삼성전자', 'MSI', 'ASUS', 'Apple', 'Lenovo'].map((name) => (
                      <label key={name}><input type="checkbox" /> {name}</label>
                    ))}
                  </div>
                </div>
                <div className="filter-row">
                  <strong>화면 크기</strong>
                  <div>
                    {['13인치', '14인치', '15인치', '16인치 이상'].map((name) => (
                      <label key={name}><input type="checkbox" /> {name}</label>
                    ))}
                  </div>
                </div>
                <div className="filter-row">
                  <strong>CPU</strong>
                  <div>
                    {['i5', 'i7', 'Ultra5', 'Ultra7', 'Ryzen5', 'Ryzen7'].map((name) => (
                      <label key={name}><input type="checkbox" /> {name}</label>
                    ))}
                  </div>
                </div>
                <div className="filter-row">
                  <strong>가격대</strong>
                  <div>
                    {['80만원 이하', '80~120만원', '120~160만원', '160만원 이상'].map((name) => (
                      <label key={name}><input type="checkbox" /> {name}</label>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="catalog-results">
            <div className="catalog-toolbar">
              <div className="catalog-sort-left">
                <button type="button" className="active">인기상품순</button>
                <button type="button">낮은가격순</button>
                <button type="button">높은가격순</button>
                <button type="button">리뷰많은순</button>
              </div>
              {loading ? <p className="sub">로딩중...</p> : null}
            </div>
            {error ? <p className="error">{error}</p> : null}

            <ul className="catalog-product-list">
              {products.map((p) => (
                <li key={p.id} className="catalog-product-row">
                  <Link to={`/public/products/${p.id}`} className="catalog-product-main">
                    <div className="thumb small catalog-product-thumb">
                      {p.thumbnailUrl ? <img src={p.thumbnailUrl} alt={p.name} /> : <span>NO IMAGE</span>}
                    </div>
                    <div className="catalog-product-desc">
                      <strong>{p.name}</strong>
                      <p className="sub">리뷰 {p.reviewCount.toLocaleString()} · 평점 {p.averageRating.toFixed(1)} · 판매처 {p.sellerCount}</p>
                      <p className="catalog-spec-line">인텔/AMD 최신 칩셋 · SSD 탑재 · 배터리 고효율 모델</p>
                    </div>
                  </Link>
                  <div className="catalog-price-ladder">
                    {priceLadder(p.lowestPrice).map((line) => (
                      <p key={line.label}>
                        <span>{line.label}</span>
                        <strong>{line.price.toLocaleString()}원</strong>
                      </p>
                    ))}
                  </div>
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
          </div>
        </div>

        <aside className="catalog-side-column">
          <div className="catalog-side-card">
            <h3>관련 콘텐츠</h3>
            <p>TV 잘 고르는 법</p>
          </div>
          <div className="catalog-side-card image">
            <img src="https://images.unsplash.com/photo-1593784991095-a205069470b6?w=420&q=80&auto=format&fit=crop" alt="추천 TV" />
          </div>
          <div className="catalog-side-card image">
            <img src="https://images.unsplash.com/photo-1571415060716-baff5f717c37?w=420&q=80&auto=format&fit=crop" alt="가이드" />
          </div>
          <div className="catalog-side-card">
            <h3>추천 가이드</h3>
            <p>2026 인기 TV 가이드</p>
          </div>
        </aside>
      </div>
    </section>
  );
}
