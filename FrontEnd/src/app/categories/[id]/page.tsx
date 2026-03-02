'use client';

import { useEffect, useMemo, useState, useCallback, useRef } from 'react';
import Link from 'next/link';
import { useParams, useRouter } from 'next/navigation';
import { Pagination, Spin, Empty, Typography, Checkbox } from 'antd';
import { categoryApi, productApi } from '@/lib/api/endpoints';
import { PAGE_SIZE, ROUTES } from '@/lib/utils/constants';
import { formatPrice } from '@/lib/utils/format';
import type { Category, ProductSummary, ProductQueryParams } from '@/types/product.types';
import type { PaginationMeta } from '@/types/common.types';
import HomeStyleHeader from '@/components/layout/HomeStyleHeader';
import '../../products/products-list.css';
import './category-page.css';

const { Text } = Typography;

interface CategoryNodeWithParent extends Category {
  parentId?: number;
}

const detailFilters = [
  { label: '제조사별', options: ['MSI', 'LG전자', '레노버', '삼성전자', 'HP', 'ASUS', 'APPLE', 'DELL', 'Microsoft'] },
  { label: '브랜드별', options: ['갤럭시북6 프로', '2026 그램 프로16', '아이디어패드', '오멘', 'TUF Gaming'] },
  { label: '화면 크기대', options: ['14인치대', '15인치대', '16인치대', '17인치대', '18인치 이상'] },
  { label: 'CPU 종류', options: ['코어i5', '코어 울트라5', '코어 울트라7', '라이젠AI 5', '라이젠5(ZEN3)'] },
  { label: '램', options: ['8GB', '16GB', '32GB', '64GB', '128GB'] },
  { label: '저장 용량대', options: ['256~129GB', '512~257GB', '1TB~513GB', '3TB~1.1TB', '3TB 초과'] },
];

export default function CategoryPage() {
  const params = useParams();
  const router = useRouter();
  const routeCategoryId = Number(params.id);
  const [categoryTree, setCategoryTree] = useState<Category[]>([]);
  const [selectedCategoryId, setSelectedCategoryId] = useState(routeCategoryId);
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [meta, setMeta] = useState<PaginationMeta | null>(null);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [isCategoryPanelOpen, setIsCategoryPanelOpen] = useState(false);
  const [overlayTopCategoryId, setOverlayTopCategoryId] = useState<number | null>(null);
  const panelWrapRef = useRef<HTMLDivElement | null>(null);

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

  const categoryById = useMemo(
    () => new Map(flatCategories.map((item) => [item.id, item])),
    [flatCategories],
  );

  const selectedCategory = useMemo(
    () => flatCategories.find((cat) => cat.id === selectedCategoryId),
    [flatCategories, selectedCategoryId],
  );

  const getTopAncestor = useCallback((id?: number) => {
    if (!id) return null;
    let current = categoryById.get(id);
    while (current?.parentId) {
      current = categoryById.get(current.parentId);
    }
    return current || null;
  }, [categoryById]);

  const topCategories = categoryTree;
  const selectedTopCategory = getTopAncestor(selectedCategoryId);
  const overlayTopCategory = useMemo(
    () => topCategories.find((cat) => cat.id === overlayTopCategoryId) || null,
    [topCategories, overlayTopCategoryId],
  );

  useEffect(() => {
    if (selectedTopCategory && !overlayTopCategoryId) {
      setOverlayTopCategoryId(selectedTopCategory.id);
    }
  }, [selectedTopCategory, overlayTopCategoryId]);

  useEffect(() => {
    if (!isCategoryPanelOpen) return;
    const onClickOutside = (event: MouseEvent) => {
      if (!panelWrapRef.current) return;
      if (!panelWrapRef.current.contains(event.target as Node)) {
        setIsCategoryPanelOpen(false);
      }
    };
    document.addEventListener('mousedown', onClickOutside);
    return () => document.removeEventListener('mousedown', onClickOutside);
  }, [isCategoryPanelOpen]);

  const leftMenuCategories = useMemo(() => {
    if (!selectedTopCategory) return topCategories;
    return [selectedTopCategory, ...(selectedTopCategory.children || [])];
  }, [selectedTopCategory, topCategories]);

  const quickCategories = useMemo(() => {
    if (!selectedCategory) return [];
    if ((selectedCategory.children || []).length > 0) return selectedCategory.children || [];
    return flatCategories.filter((item) => item.parentId === selectedCategory.parentId && item.id !== selectedCategory.id);
  }, [selectedCategory, flatCategories]);

  const fetchProducts = useCallback(async () => {
    if (!selectedCategoryId) return;
    setLoading(true);
    try {
      const params: ProductQueryParams = {
        categoryId: selectedCategoryId,
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
  }, [selectedCategoryId, page]);

  useEffect(() => {
    fetchProducts();
  }, [fetchProducts]);

  const moveCategory = (id: number) => {
    setIsCategoryPanelOpen(false);
    router.push(ROUTES.CATEGORY(id));
  };

  return (
    <div className="ns-list-page page-container">
      <HomeStyleHeader />

      <div className="ns-cat-topbar">
        <div className="ns-cat-dropdown-wrap" ref={panelWrapRef}>
          <button
            type="button"
            className="ns-cat-all-btn"
            onClick={() => setIsCategoryPanelOpen((prev) => !prev)}
          >
            <i className="bi bi-list" />
            <span>전체 카테고리</span>
          </button>

          {isCategoryPanelOpen ? (
            <div className="ns-cat-dropdown-panel">
              <div className="ns-cat-dropdown-body">
                <aside className="ns-cat-dropdown-left">
                  {topCategories.map((cat) => (
                    <button
                      key={cat.id}
                      type="button"
                      className={overlayTopCategoryId === cat.id ? 'active' : ''}
                      onMouseEnter={() => setOverlayTopCategoryId(cat.id)}
                      onClick={() => setOverlayTopCategoryId(cat.id)}
                    >
                      {cat.name}
                    </button>
                  ))}
                </aside>

                <div className="ns-cat-dropdown-content">
                  {(overlayTopCategory?.children || []).map((group) => (
                    <div className="ns-cat-dropdown-col" key={group.id}>
                      <button type="button" className="ns-cat-dropdown-title" onClick={() => moveCategory(group.id)}>
                        {group.name}
                      </button>
                      <ul>
                        {(group.children || []).slice(0, 8).map((sub) => (
                          <li key={sub.id}>
                            <button type="button" onClick={() => moveCategory(sub.id)}>
                              {sub.name}
                            </button>
                          </li>
                        ))}
                      </ul>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          ) : null}
        </div>
        <div className="ns-cat-crumbs">
          <span>홈</span>
          <i className="bi bi-chevron-right" />
          <span>{selectedTopCategory?.name || '카테고리'}</span>
          <i className="bi bi-chevron-right" />
          <span className="active">{selectedCategory?.name || '카테고리'}</span>
        </div>
      </div>

      <div className="ns-list-layout">
        <aside className="ns-left-category ns-left-category-danawa">
          <ul>
            {leftMenuCategories.map((cat, idx) => (
              <li key={cat.id}>
                <button
                  type="button"
                  className={idx === 0 || selectedCategoryId === cat.id ? 'active' : ''}
                  onClick={() => moveCategory(cat.id)}
                >
                  <span>{cat.name}</span>
                  {idx !== 0 ? <i className="bi bi-chevron-right" /> : null}
                </button>
              </li>
            ))}
          </ul>
        </aside>

        <section className="ns-right-panel">
          <div className="ns-cat-quick-grid">
            {(quickCategories.length > 0 ? quickCategories : [selectedCategory]).filter(Boolean).slice(0, 12).map((cat) => (
              <button
                key={cat!.id}
                type="button"
                className={selectedCategoryId === cat!.id ? 'active' : ''}
                onClick={() => moveCategory(cat!.id)}
              >
                {cat!.name}
              </button>
            ))}
          </div>

          <div className="ns-cat-detail-box">
            <div className="ns-cat-detail-title">
              <h5>상세검색</h5>
              <Text type="secondary">CM 추천 옵션은 하이라이트로 강조했어요.</Text>
            </div>

            {detailFilters.map((row) => (
              <div className="ns-cat-detail-row" key={row.label}>
                <div className="ns-cat-detail-label">{row.label}</div>
                <div className="ns-cat-detail-options">
                  <Checkbox.Group options={row.options} />
                </div>
              </div>
            ))}
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
