'use client';

import { useEffect, useState, useCallback } from 'react';
import Link from 'next/link';
import { Input, Select, Pagination, Spin, Empty, Typography, Space, InputNumber, Button, Checkbox, Rate } from 'antd';
import { SearchOutlined } from '@ant-design/icons';
import { useSearchParams } from 'next/navigation';
import { productApi, categoryApi } from '@/lib/api/endpoints';
import { SORT_OPTIONS, PAGE_SIZE, ROUTES } from '@/lib/utils/constants';
import { formatPrice } from '@/lib/utils/format';
import type { ProductSummary, Category, ProductQueryParams } from '@/types/product.types';
import type { PaginationMeta } from '@/types/common.types';
import './products-list.css';

const { Title, Text } = Typography;

export default function ProductListPage() {
  const searchParams = useSearchParams();
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [meta, setMeta] = useState<PaginationMeta | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState(searchParams.get('search') || '');
  const [categoryId, setCategoryId] = useState<number | undefined>(searchParams.get('categoryId') ? Number(searchParams.get('categoryId')) : undefined);
  const [sort, setSort] = useState(searchParams.get('sort') || 'newest');
  const [minPrice, setMinPrice] = useState<number | undefined>(undefined);
  const [maxPrice, setMaxPrice] = useState<number | undefined>(undefined);
  const [page, setPage] = useState(Number(searchParams.get('page')) || 1);
  const [selectedBrands, setSelectedBrands] = useState<string[]>([]);
  const [selectedSizes, setSelectedSizes] = useState<string[]>([]);

  const fetchProducts = useCallback(async () => {
    setLoading(true);
    try {
      const params: ProductQueryParams = { page, limit: PAGE_SIZE, sort: sort as any, search: search || undefined, categoryId, minPrice, maxPrice };
      const { data } = await productApi.getAll(params);
      setProducts(data.data); setMeta(data.meta || null);
    } catch {} finally { setLoading(false); }
  }, [page, sort, search, categoryId, minPrice, maxPrice]);

  useEffect(() => { categoryApi.getAll().then((r) => setCategories(r.data.data)).catch(() => {}); }, []);
  useEffect(() => { fetchProducts(); }, [fetchProducts]);

  const flatCats = (cats: Category[], prefix = ''): { id: number; name: string }[] =>
    cats.flatMap((c) => [{ id: c.id, name: `${prefix}${c.name}` }, ...flatCats(c.children || [], `${prefix}${c.name} > `)]);

  const flatCategories = flatCats(categories);
  const selectedCategoryName = flatCategories.find((cat) => cat.id === categoryId)?.name;
  const brandOptions = ['MSI', 'LG전자', '레노버', '삼성전자', 'HP', 'ASUS', 'APPLE', 'DELL'];
  const screenSizeOptions = ['14인치대', '15인치대', '16인치대', '17인치대', '18인치 이상'];

  return (
    <div className="ns-list-page page-container">
      <div className="ns-list-breadcrumb">
        <span>홈</span>
        <i className="bi bi-chevron-right" />
        <span>{selectedCategoryName || '상품'}</span>
      </div>

      <div className="ns-list-layout">
        <aside className="ns-left-category">
          <div className="ns-left-title">카테고리</div>
          <ul>
            <li>
              <button
                type="button"
                className={!categoryId ? 'active' : ''}
                onClick={() => {
                  setCategoryId(undefined);
                  setPage(1);
                }}
              >
                전체
              </button>
            </li>
            {categories.map((cat) => (
              <li key={cat.id}>
                <button
                  type="button"
                  className={categoryId === cat.id ? 'active' : ''}
                  onClick={() => {
                    setCategoryId(cat.id);
                    setPage(1);
                  }}
                >
                  {cat.name}
                </button>
              </li>
            ))}
          </ul>
        </aside>

        <section className="ns-right-panel">
          <div className="ns-filter-panel">
            <div className="ns-filter-head">
              <Title level={4} style={{ margin: 0 }}>상세검색</Title>
              <Text type="secondary">원하는 조건으로 빠르게 좁혀보세요.</Text>
            </div>

            <div className="ns-filter-grid">
              <div className="ns-filter-label">검색어</div>
              <div className="ns-filter-control">
                <Input
                  placeholder="상품명 검색"
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  onPressEnter={() => {
                    setPage(1);
                    fetchProducts();
                  }}
                  suffix={<SearchOutlined />}
                />
              </div>

              <div className="ns-filter-label">정렬</div>
              <div className="ns-filter-control">
                <Select
                  style={{ width: 220 }}
                  value={sort}
                  onChange={(v) => {
                    setSort(v);
                    setPage(1);
                  }}
                  options={SORT_OPTIONS.map((o) => ({ label: o.label, value: o.value }))}
                />
              </div>

              <div className="ns-filter-label">제조사</div>
              <div className="ns-filter-control">
                <Checkbox.Group options={brandOptions} value={selectedBrands} onChange={(vals) => setSelectedBrands(vals as string[])} />
              </div>

              <div className="ns-filter-label">화면 크기</div>
              <div className="ns-filter-control">
                <Checkbox.Group options={screenSizeOptions} value={selectedSizes} onChange={(vals) => setSelectedSizes(vals as string[])} />
              </div>

              <div className="ns-filter-label">가격대</div>
              <div className="ns-filter-control">
                <Space>
                  <InputNumber placeholder="최소" value={minPrice} onChange={(v) => setMinPrice(v || undefined)} style={{ width: 140 }} />
                  <span>~</span>
                  <InputNumber placeholder="최대" value={maxPrice} onChange={(v) => setMaxPrice(v || undefined)} style={{ width: 140 }} />
                  <Button type="primary" onClick={() => { setPage(1); fetchProducts(); }}>검색</Button>
                </Space>
              </div>
            </div>
          </div>

          <div className="ns-result-tab">
            <span className="active">가격비교 {meta ? `(${meta.totalCount.toLocaleString()})` : ''}</span>
            <span>검색상품</span>
            <span>중고장터</span>
          </div>

          {loading ? (
            <div className="ns-loading-wrap"><Spin size="large" /></div>
          ) : products.length === 0 ? (
            <div className="ns-empty-wrap"><Empty description="상품이 없습니다" /></div>
          ) : (
            <>
              <div className="ns-product-list">
                {products.map((p) => (
                  <Link href={ROUTES.PRODUCT_DETAIL(p.id)} key={p.id} className="ns-list-item">
                    <div className="ns-list-thumb">
                      {p.thumbnailUrl ? <img src={p.thumbnailUrl} alt={p.name} /> : <span>NO IMAGE</span>}
                    </div>
                    <div className="ns-list-body">
                      <p className="ns-item-title">{p.name}</p>
                      <div className="ns-item-meta">
                        <Rate disabled value={p.averageRating} style={{ fontSize: 14 }} />
                        <span>리뷰 {p.reviewCount}건</span>
                        <span>판매처 {p.sellerCount}곳</span>
                      </div>
                    </div>
                    <div className="ns-item-price">
                      <span className="ns-price-label">최저가</span>
                      <strong>{formatPrice(p.lowestPrice)}</strong>
                    </div>
                  </Link>
                ))}
              </div>
              {meta && (
                <div className="ns-list-pagination">
                  <Pagination current={page} total={meta.totalCount} pageSize={PAGE_SIZE} onChange={setPage} showSizeChanger={false} />
                </div>
              )}
            </>
          )}
        </section>
      </div>
    </div>
  );
}
