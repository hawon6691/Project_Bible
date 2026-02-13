'use client';

import { useEffect } from 'react';
import { Card, Row, Col, Typography, Statistic, Space, Button, List, Tag } from 'antd';
import {
  UserOutlined, ShoppingCartOutlined, StarOutlined, HeartOutlined,
  GiftOutlined, WalletOutlined, EnvironmentOutlined, EditOutlined,
} from '@ant-design/icons';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/stores/authStore';
import { ROUTES } from '@/lib/utils/constants';

const { Title, Text } = Typography;

export default function MyPage() {
  const router = useRouter();
  const { user, isAuthenticated, isLoading } = useAuthStore();

  useEffect(() => {
    if (!isLoading && !isAuthenticated) router.push(ROUTES.LOGIN);
  }, [isLoading, isAuthenticated, router]);

  if (isLoading || !user) return null;

  const menuItems = [
    { icon: <ShoppingCartOutlined />, label: '주문 내역', href: ROUTES.ORDERS },
    { icon: <StarOutlined />, label: '내 리뷰', href: ROUTES.MYPAGE_REVIEWS },
    { icon: <HeartOutlined />, label: '찜한 상품', href: ROUTES.MYPAGE_WISHLIST },
    { icon: <WalletOutlined />, label: '포인트', href: ROUTES.MYPAGE_POINTS },
    { icon: <GiftOutlined />, label: '쿠폰', href: ROUTES.MYPAGE_COUPONS },
    { icon: <EnvironmentOutlined />, label: '배송지 관리', href: ROUTES.MYPAGE_ADDRESSES },
    { icon: <EditOutlined />, label: '프로필 수정', href: ROUTES.MYPAGE_PROFILE },
  ];

  return (
    <div className="page-container">
      <Title level={3}>마이페이지</Title>

      {/* User Info */}
      <Card style={{ marginBottom: 24 }}>
        <Row align="middle" gutter={24}>
          <Col>
            <div style={{ width: 64, height: 64, borderRadius: '50%', background: '#1677ff', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <UserOutlined style={{ fontSize: 28, color: '#fff' }} />
            </div>
          </Col>
          <Col flex="auto">
            <Title level={4} style={{ margin: 0 }}>{user.name}</Title>
            <Text type="secondary">{user.email}</Text>
            <div style={{ marginTop: 4 }}>
              {user.badges?.map((badge) => (
                <Tag key={badge.id} color="blue">{badge.name}</Tag>
              ))}
            </div>
          </Col>
          <Col>
            <Statistic title="포인트" value={user.point} suffix="P" />
          </Col>
        </Row>
      </Card>

      {/* Quick Menu */}
      <Row gutter={[16, 16]}>
        {menuItems.map((item) => (
          <Col key={item.href} xs={12} sm={8} md={6}>
            <Link href={item.href}>
              <Card hoverable style={{ textAlign: 'center' }}>
                <div style={{ fontSize: 28, color: '#1677ff', marginBottom: 8 }}>{item.icon}</div>
                <Text>{item.label}</Text>
              </Card>
            </Link>
          </Col>
        ))}
      </Row>
    </div>
  );
}
