'use client';

import { useEffect, useState, useCallback } from 'react';
import { Row, Col, Typography, Select, Pagination, Spin, Empty } from 'antd';
import { useParams } from 'next/navigation';
import { productApi, categoryApi } from '@/lib/api/endpoints';
import { SORT_OPTIONS, PAGE_SIZE } from '@/lib/utils/constants';
import ProductCard from '@/components/product/ProductCard';
import type { ProductSummary, Category } from '@/types/product.types';
import type { PaginationMeta } from '@/types/common.types';

const { Title } = Typography;

export default function CategoryPage() {
  const params = useParams();
  const categoryId = Number(params.id);
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [meta, setMeta] = useState<PaginationMeta | null>(null);
  const [category, setCategory] = useState<Category | null>(null);
  const [sort, setSort] = useState('newest');
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(true);

  useEffect(() => { categoryApi.getOne(categoryId).then((r) => setCategory(r.data.data)).catch(() => {}); }, [categoryId]);

  const fetchProducts = useCallback(async () => {
    setLoading(true);
    try { const { data } = await productApi.getAll({ categoryId, sort: sort as any, page, limit: PAGE_SIZE }); setProducts(data.data); setMeta(data.meta || null); }
    catch {} finally { setLoading(false); }
  }, [categoryId, sort, page]);

  useEffect(() => { fetchProducts(); }, [fetchProducts]);

  return (
    <div className="page-container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <Title level={3} style={{ margin: 0 }}>{category?.name || '카테고리'}</Title>
        <Select value={sort} onChange={(v) => { setSort(v); setPage(1); }} options={SORT_OPTIONS.map((o) => ({ label: o.label, value: o.value }))} style={{ width: 150 }} />
      </div>
      {loading ? <div style={{ textAlign: 'center', padding: 60 }}><Spin size="large" /></div>
       : products.length === 0 ? <Empty description="상품이 없습니다" />
       : (<><Row gutter={[16, 16]}>{products.map((p) => (<Col key={p.id} xs={12} sm={8} md={6}><ProductCard product={p} /></Col>))}</Row>
          {meta && <div style={{ textAlign: 'center', marginTop: 24 }}><Pagination current={page} total={meta.totalCount} pageSize={PAGE_SIZE} onChange={setPage} showSizeChanger={false} /></div>}</>)}
    </div>
  );
}
