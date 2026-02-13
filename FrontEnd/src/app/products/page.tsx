'use client';

import { useEffect, useState, useCallback } from 'react';
import { Row, Col, Card, Select, Input, Pagination, Spin, Empty, Typography, Space, InputNumber, Button } from 'antd';
import { FilterOutlined } from '@ant-design/icons';
import { useSearchParams } from 'next/navigation';
import { productApi, categoryApi } from '@/lib/api/endpoints';
import { SORT_OPTIONS, PAGE_SIZE } from '@/lib/utils/constants';
import ProductCard from '@/components/product/ProductCard';
import type { ProductSummary, Category, ProductQueryParams } from '@/types/product.types';
import type { PaginationMeta } from '@/types/common.types';

const { Title } = Typography;

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
    cats.flatMap((c) => [{ id: c.id, name: prefix + c.name }, ...flatCats(c.children || [], prefix + c.name + ' > ')]);

  return (
    <div className="page-container">
      <Title level={3}>상품 목록</Title>
      <Card style={{ marginBottom: 24 }}>
        <Row gutter={[16, 16]} align="middle">
          <Col xs={24} sm={8}><Input.Search placeholder="상품 검색" value={search} onChange={(e) => setSearch(e.target.value)} onSearch={() => { setPage(1); fetchProducts(); }} enterButton /></Col>
          <Col xs={12} sm={4}><Select style={{ width: '100%' }} placeholder="카테고리" allowClear value={categoryId} onChange={(v) => { setCategoryId(v); setPage(1); }} options={flatCats(categories).map((c) => ({ label: c.name, value: c.id }))} /></Col>
          <Col xs={12} sm={4}><Select style={{ width: '100%' }} value={sort} onChange={(v) => { setSort(v); setPage(1); }} options={SORT_OPTIONS.map((o) => ({ label: o.label, value: o.value }))} /></Col>
          <Col xs={24} sm={8}>
            <Space>
              <InputNumber placeholder="최소 가격" value={minPrice} onChange={(v) => setMinPrice(v || undefined)} style={{ width: 130 }} />
              <span>~</span>
              <InputNumber placeholder="최대 가격" value={maxPrice} onChange={(v) => setMaxPrice(v || undefined)} style={{ width: 130 }} />
              <Button icon={<FilterOutlined />} onClick={() => { setPage(1); fetchProducts(); }}>적용</Button>
            </Space>
          </Col>
        </Row>
      </Card>
      {loading ? <div style={{ textAlign: 'center', padding: 60 }}><Spin size="large" /></div>
       : products.length === 0 ? <Empty description="상품이 없습니다" />
       : (<>
          <Row gutter={[16, 16]}>{products.map((p) => (<Col key={p.id} xs={12} sm={8} md={6}><ProductCard product={p} /></Col>))}</Row>
          {meta && <div style={{ textAlign: 'center', marginTop: 24 }}><Pagination current={page} total={meta.totalCount} pageSize={PAGE_SIZE} onChange={setPage} showSizeChanger={false} /></div>}
        </>)}
    </div>
  );
}
