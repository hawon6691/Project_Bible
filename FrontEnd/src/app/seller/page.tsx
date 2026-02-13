'use client';

import { useEffect, useState } from 'react';
import { Card, Row, Col, Typography, Statistic, Table, Button, Spin, Empty, message, Tag } from 'antd';
import { ShopOutlined, DollarOutlined, ShoppingOutlined, PlusOutlined } from '@ant-design/icons';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/stores/authStore';
import { sellerApi } from '@/lib/api/endpoints';
import { formatPrice, formatDateTime } from '@/lib/utils/format';
import { ROUTES } from '@/lib/utils/constants';

const { Title, Text } = Typography;

interface SellerInfo {
  id: number;
  companyName: string;
  trustScore: number;
  status: string;
  totalProducts: number;
  totalSales: number;
}

interface SellerPrice {
  id: number;
  productId: number;
  productName: string;
  price: number;
  url: string;
  shipping: string;
  createdAt: string;
}

export default function SellerDashboardPage() {
  const router = useRouter();
  const { isAuthenticated, isLoading: authLoading } = useAuthStore();
  const [sellerInfo, setSellerInfo] = useState<SellerInfo | null>(null);
  const [prices, setPrices] = useState<SellerPrice[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) { router.push(ROUTES.LOGIN); return; }
    if (!isAuthenticated) return;
    Promise.all([
      sellerApi.getMyInfo().then((res) => setSellerInfo(res.data.data)),
      sellerApi.getMyPrices({ limit: 5 }).then((res) => setPrices(res.data.data)),
    ]).catch(() => {}).finally(() => setLoading(false));
  }, [isAuthenticated, authLoading, router]);

  if (loading || authLoading) return <div style={{ textAlign: 'center', padding: 100 }}><Spin size="large" /></div>;

  if (!sellerInfo) {
    return (
      <div className="page-container" style={{ textAlign: 'center', padding: 100 }}>
        <ShopOutlined style={{ fontSize: 64, color: '#d9d9d9' }} />
        <Title level={4} style={{ marginTop: 16 }}>판매자 등록이 필요합니다</Title>
        <Button type="primary" size="large" onClick={() => router.push('/seller/register')}>판매자 등록 신청</Button>
      </div>
    );
  }

  return (
    <div className="page-container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <Title level={3} style={{ margin: 0 }}>판매자 대시보드</Title>
        <Tag color={sellerInfo.status === 'APPROVED' ? 'green' : 'orange'}>
          {sellerInfo.status === 'APPROVED' ? '승인됨' : '승인 대기'}
        </Tag>
      </div>

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={12} sm={6}>
          <Card><Statistic title="상호명" value={sellerInfo.companyName} valueStyle={{ fontSize: 16 }} /></Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card><Statistic title="신뢰도" value={sellerInfo.trustScore} suffix="점" /></Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card><Statistic title="등록 상품" value={sellerInfo.totalProducts} suffix="개" /></Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card><Statistic title="총 판매" value={sellerInfo.totalSales} suffix="건" /></Card>
        </Col>
      </Row>

      <Card
        title="최근 등록 가격"
        extra={<Link href={ROUTES.SELLER_PRODUCTS}><Button type="primary" icon={<PlusOutlined />}>가격 관리</Button></Link>}
      >
        {prices.length === 0 ? (
          <Empty description="등록된 가격이 없습니다" />
        ) : (
          <Table
            dataSource={prices}
            rowKey="id"
            pagination={false}
            columns={[
              { title: '상품명', dataIndex: 'productName', key: 'productName' },
              { title: '가격', dataIndex: 'price', key: 'price', render: (v: number) => formatPrice(v) },
              { title: '배송', dataIndex: 'shipping', key: 'shipping' },
              { title: '등록일', dataIndex: 'createdAt', key: 'createdAt', render: (d: string) => formatDateTime(d) },
            ]}
          />
        )}
      </Card>
    </div>
  );
}
