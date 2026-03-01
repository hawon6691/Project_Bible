'use client';

import { useEffect, useMemo, useState, useCallback } from 'react';
import Link from 'next/link';
import { useParams, useRouter } from 'next/navigation';
import { Select, Pagination, Spin, Empty, Typography, Checkbox, Rate } from 'antd';
import { categoryApi, productApi } from '@/lib/api/endpoints';
import { PAGE_SIZE, ROUTES, SORT_OPTIONS } from '@/lib/utils/constants';
import { formatPrice } from '@/lib/utils/format';
import type { Category, ProductSummary, ProductQueryParams } from '@/types/product.types';
import type { PaginationMeta } from '@/types/common.types';
import HomeStyleHeader from '@/components/layout/HomeStyleHeader';
import '../../products/products-list.css';

const { Title } = Typography;

interface CategoryNodeWithParent extends Category {
  parentId?: number;
}

export default function CategoryPage() {
  const params = useParams();
  const router = useRouter();
  const routeCategoryId = Number(params.id);
  const [categoryTree, setCategoryTree] = useState<Category[]>([]);
  const [selectedCategoryId, setSelectedCategoryId] = useState(routeCategoryId);
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [meta, setMeta] = useState<PaginationMeta | null>(null);
  const [sort, setSort] = useState('newest');
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setSelectedCategoryId(routeCategoryId);
    setPage(1);
  }, [routeCategoryId]);

  useEffect(() => {
    categoryApi.getAll().then((res) => setCategoryTree(res.data.data)).catch(() => {});
  }, []);

  const flatCategories: CategoryNodeWithParent[] = useMemo(() => {
    const walk = (items: Category[], parentId?: number): CategoryNodeWithParent[] =>
      items.flatMap((item) => [{ ...item, parentId }, ...walk(item.children || [], item.id)]);
    return walk(categoryTree);
  }, [categoryTree]);

  const selectedCategory = useMemo(
    () => flatCategories.find((cat) => cat.id === selectedCategoryId),
    [flatCategories, selectedCategoryId],
  );

  const topCategories = categoryTree;

  const childCategories = useMemo(() => {
    if (!selectedCategory) return [];
    return (selectedCategory.children || []).length > 0
      ? (selectedCategory.children || [])
      : flatCategories.filter((cat) => cat.parentId === selectedCategory.parentId && cat.id !== selectedCategory.id);
  }, [selectedCategory, flatCategories]);

  const fetchProducts = useCallback(async () => {
    if (!selectedCategoryId) return;
    setLoading(true);
    try {
      const params: ProductQueryParams = {
        categoryId: selectedCategoryId,
        sort: sort as any,
        page,
        limit: PAGE_SIZE,
      };
      const { data } = await productApi.getAll(params);
      setProducts(data.data);
      setMeta(data.meta || null);
    } catch {
      setProducts([]);
      setMeta(null);
    } finally {
      setLoading(false);
    }
  }, [selectedCategoryId, sort, page]);

  useEffect(() => {
    fetchProducts();
  }, [fetchProducts]);

  const moveCategory = (id: number) => {
    router.push(ROUTES.CATEGORY(id));
  };

  return (
    <div className="ns-list-page page-container">
      <HomeStyleHeader />
      <div className="ns-list-breadcrumb">
        <span>홈</span>
        <i className="bi bi-chevron-right" />
        <span>전체 카테고리</span>
        <i className="bi bi-chevron-right" />
        <span>{selectedCategory?.name || '카테고리'}</span>
      </div>

      <div className="ns-list-layout">
        <aside className="ns-left-category">
          <div className="ns-left-title">전체 카테고리</div>
          <ul>
            {topCategories.map((cat) => (
              <li key={cat.id}>
                <button
                  type="button"
                  className={selectedCategoryId === cat.id ? 'active' : ''}
                  onClick={() => moveCategory(cat.id)}
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
              <Title level={4} style={{ margin: 0 }}>{selectedCategory?.name || '카테고리'}</Title>
              <Select
                value={sort}
                style={{ width: 180 }}
                onChange={(v) => {
                  setSort(v);
                  setPage(1);
                }}
                options={SORT_OPTIONS.map((item) => ({ label: item.label, value: item.value }))}
              />
            </div>

            {childCategories.length > 0 ? (
              <div className="ns-filter-grid">
                <div className="ns-filter-label">세부 카테고리</div>
                <div className="ns-filter-control">
                  <Checkbox.Group
                    value={[selectedCategoryId]}
                    options={childCategories.map((cat) => ({ label: cat.name, value: cat.id }))}
                    onChange={(vals) => {
                      const nextId = Number(vals[vals.length - 1]);
                      if (nextId) moveCategory(nextId);
                    }}
                  />
                </div>
              </div>
            ) : null}
          </div>

          <div className="ns-result-tab">
            <span className="active">
              {selectedCategory?.name || '카테고리'} {meta ? `(${meta.totalCount.toLocaleString()})` : ''}
            </span>
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
              {meta ? (
                <div className="ns-list-pagination">
                  <Pagination
                    current={page}
                    total={meta.totalCount}
                    pageSize={PAGE_SIZE}
                    onChange={setPage}
                    showSizeChanger={false}
                  />
                </div>
              ) : null}
            </>
          )}
        </section>
      </div>
    </div>
  );
}
