'use client';

import { useEffect, useState } from 'react';
import { Row, Col, Card, Typography, Spin, Tag } from 'antd';
import { RightOutlined } from '@ant-design/icons';
import Link from 'next/link';
import { productApi, categoryApi } from '@/lib/api/endpoints';
import { ROUTES } from '@/lib/utils/constants';
import ProductCard from '@/components/product/ProductCard';
import type { ProductSummary, Category } from '@/types/product.types';

const { Title, Text } = Typography;

export default function HomePage() {
  const [popularProducts, setPopularProducts] = useState<ProductSummary[]>([]);
  const [newProducts, setNewProducts] = useState<ProductSummary[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      productApi.getAll({ sort: 'popularity', limit: 8 }),
      productApi.getAll({ sort: 'newest', limit: 8 }),
      categoryApi.getAll(),
    ]).then(([popRes, newRes, catRes]) => {
      setPopularProducts(popRes.data.data);
      setNewProducts(newRes.data.data);
      setCategories(catRes.data.data);
    }).catch(() => {}).finally(() => setLoading(false));
  }, []);

  if (loading) return <div style={{ display: 'flex', justifyContent: 'center', padding: 100 }}><Spin size="large" /></div>;

  return (
    <div className="page-container">
      <Card style={{ marginBottom: 24 }}>
        <Title level={5} style={{ marginBottom: 12 }}>카테고리</Title>
        <Row gutter={[8, 8]}>
          {categories.map((cat) => (
            <Col key={cat.id}><Link href={ROUTES.CATEGORY(cat.id)}><Tag style={{ cursor: 'pointer', padding: '4px 12px', fontSize: 14 }}>{cat.name}</Tag></Link></Col>
          ))}
        </Row>
      </Card>
      <div style={{ marginBottom: 32 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
          <Title level={4} style={{ margin: 0 }}>인기 상품</Title>
          <Link href={`${ROUTES.PRODUCTS}?sort=popularity`}><Text type="secondary">더보기 <RightOutlined /></Text></Link>
        </div>
        <Row gutter={[16, 16]}>{popularProducts.map((p) => (<Col key={p.id} xs={12} sm={8} md={6}><ProductCard product={p} /></Col>))}</Row>
      </div>
      <div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
          <Title level={4} style={{ margin: 0 }}>신상품</Title>
          <Link href={`${ROUTES.PRODUCTS}?sort=newest`}><Text type="secondary">더보기 <RightOutlined /></Text></Link>
        </div>
        <Row gutter={[16, 16]}>{newProducts.map((p) => (<Col key={p.id} xs={12} sm={8} md={6}><ProductCard product={p} /></Col>))}</Row>
      </div>
    </div>
  );
}
