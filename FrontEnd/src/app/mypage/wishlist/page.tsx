'use client';

import { useEffect, useState } from 'react';
import { Row, Col, Typography, Spin, Empty, Pagination } from 'antd';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/stores/authStore';
import { ROUTES, PAGE_SIZE } from '@/lib/utils/constants';
import ProductCard from '@/components/product/ProductCard';
import apiClient from '@/lib/api/client';
import type { ProductSummary } from '@/types/product.types';
import type { PaginationMeta } from '@/types/common.types';

const { Title } = Typography;

export default function WishlistPage() {
  const router = useRouter();
  const { isAuthenticated, isLoading: authLoading } = useAuthStore();
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [meta, setMeta] = useState<PaginationMeta | null>(null);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) router.push(ROUTES.LOGIN);
  }, [authLoading, isAuthenticated, router]);

  useEffect(() => {
    if (!isAuthenticated) return;
    setLoading(true);
    apiClient.get('/wishlist', { params: { page, limit: PAGE_SIZE } })
      .then((res) => { setProducts(res.data.data); setMeta(res.data.meta || null); })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [isAuthenticated, page]);

  return (
    <div className="page-container">
      <Title level={3}>찜한 상품</Title>
      {loading ? (
        <div style={{ textAlign: 'center', padding: 60 }}><Spin size="large" /></div>
      ) : products.length === 0 ? (
        <Empty description="찜한 상품이 없습니다" />
      ) : (
        <>
          <Row gutter={[16, 16]}>
            {products.map((product) => (
              <Col key={product.id} xs={12} sm={8} md={6}>
                <ProductCard product={product} />
              </Col>
            ))}
          </Row>
          {meta && (
            <div style={{ textAlign: 'center', marginTop: 24 }}>
              <Pagination current={page} total={meta.totalCount} pageSize={PAGE_SIZE} onChange={setPage} showSizeChanger={false} />
            </div>
          )}
        </>
      )}
    </div>
  );
}
