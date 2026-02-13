'use client';

import { useEffect, useState } from 'react';
import { Row, Col, Card, Statistic, Typography, Spin } from 'antd';
import { UserOutlined, ShoppingOutlined, ShopOutlined, DollarOutlined, OrderedListOutlined, StarOutlined } from '@ant-design/icons';
import { adminApi } from '@/lib/api/endpoints';

const { Title } = Typography;

interface Stats {
  totalUsers: number;
  totalProducts: number;
  totalOrders: number;
  totalSellers: number;
  totalRevenue: number;
  totalReviews: number;
  todayOrders: number;
  todayRevenue: number;
}

export default function AdminDashboardPage() {
  const [stats, setStats] = useState<Stats | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    adminApi.getStats()
      .then((res) => setStats(res.data.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div style={{ textAlign: 'center', padding: 100 }}><Spin size="large" /></div>;

  return (
    <div>
      <Title level={3}>관리자 대시보드</Title>

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={12} sm={8} md={6}>
          <Card><Statistic title="전체 회원" value={stats?.totalUsers || 0} prefix={<UserOutlined />} /></Card>
        </Col>
        <Col xs={12} sm={8} md={6}>
          <Card><Statistic title="전체 상품" value={stats?.totalProducts || 0} prefix={<ShoppingOutlined />} /></Card>
        </Col>
        <Col xs={12} sm={8} md={6}>
          <Card><Statistic title="전체 주문" value={stats?.totalOrders || 0} prefix={<OrderedListOutlined />} /></Card>
        </Col>
        <Col xs={12} sm={8} md={6}>
          <Card><Statistic title="판매자" value={stats?.totalSellers || 0} prefix={<ShopOutlined />} /></Card>
        </Col>
        <Col xs={12} sm={8} md={6}>
          <Card><Statistic title="리뷰" value={stats?.totalReviews || 0} prefix={<StarOutlined />} /></Card>
        </Col>
        <Col xs={12} sm={8} md={6}>
          <Card><Statistic title="총 매출" value={stats?.totalRevenue || 0} prefix={<DollarOutlined />} suffix="원" /></Card>
        </Col>
        <Col xs={12} sm={8} md={6}>
          <Card><Statistic title="오늘 주문" value={stats?.todayOrders || 0} valueStyle={{ color: '#1677ff' }} /></Card>
        </Col>
        <Col xs={12} sm={8} md={6}>
          <Card><Statistic title="오늘 매출" value={stats?.todayRevenue || 0} suffix="원" valueStyle={{ color: '#52c41a' }} /></Card>
        </Col>
      </Row>
    </div>
  );
}
