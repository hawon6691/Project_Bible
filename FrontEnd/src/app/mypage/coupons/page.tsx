'use client';

import { useEffect, useState } from 'react';
import { Card, Row, Col, Typography, Tag, Empty, Spin, Input, Button, message, Space } from 'antd';
import { GiftOutlined } from '@ant-design/icons';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/stores/authStore';
import { couponApi } from '@/lib/api/endpoints';
import { formatPrice, formatDate } from '@/lib/utils/format';
import { ROUTES } from '@/lib/utils/constants';
import type { Coupon } from '@/types/order.types';

const { Title, Text } = Typography;

export default function CouponsPage() {
  const router = useRouter();
  const { isAuthenticated, isLoading: authLoading } = useAuthStore();
  const [coupons, setCoupons] = useState<Coupon[]>([]);
  const [loading, setLoading] = useState(true);
  const [couponCode, setCouponCode] = useState('');
  const [claiming, setClaiming] = useState(false);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) router.push(ROUTES.LOGIN);
  }, [authLoading, isAuthenticated, router]);

  useEffect(() => {
    if (!isAuthenticated) return;
    setLoading(true);
    couponApi.getMyCoupons()
      .then((res) => setCoupons(res.data.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [isAuthenticated]);

  const handleClaim = async () => {
    if (!couponCode.trim()) return;
    setClaiming(true);
    try {
      const { data } = await couponApi.claim(couponCode.trim());
      setCoupons([data.data, ...coupons]);
      setCouponCode('');
      message.success('쿠폰이 등록되었습니다!');
    } catch (err: any) {
      message.error(err?.response?.data?.error?.message || '쿠폰 등록에 실패했습니다.');
    } finally {
      setClaiming(false);
    }
  };

  return (
    <div className="page-container">
      <Title level={3}>쿠폰</Title>

      <Card style={{ marginBottom: 24 }}>
        <Space.Compact style={{ width: '100%', maxWidth: 400 }}>
          <Input
            placeholder="쿠폰 코드를 입력하세요"
            value={couponCode}
            onChange={(e) => setCouponCode(e.target.value)}
            onPressEnter={handleClaim}
          />
          <Button type="primary" onClick={handleClaim} loading={claiming}>등록</Button>
        </Space.Compact>
      </Card>

      {loading ? (
        <div style={{ textAlign: 'center', padding: 60 }}><Spin size="large" /></div>
      ) : coupons.length === 0 ? (
        <Empty description="보유한 쿠폰이 없습니다" />
      ) : (
        <Row gutter={[16, 16]}>
          {coupons.map((coupon) => (
            <Col key={coupon.id} xs={24} sm={12} md={8}>
              <Card style={{ opacity: coupon.isUsed ? 0.5 : 1 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                  <GiftOutlined style={{ fontSize: 24, color: '#1677ff' }} />
                  {coupon.isUsed ? <Tag>사용완료</Tag> : <Tag color="green">사용가능</Tag>}
                </div>
                <Title level={4} style={{ margin: '8px 0 4px' }}>
                  {coupon.discountType === 'PERCENT'
                    ? `${coupon.discountValue}% 할인`
                    : `${formatPrice(coupon.discountValue)} 할인`}
                </Title>
                <Text strong>{coupon.name}</Text>
                <div style={{ marginTop: 8 }}>
                  <Text type="secondary" style={{ fontSize: 12 }}>
                    {formatPrice(coupon.minOrderAmount)} 이상 주문 시
                  </Text>
                </div>
                {coupon.maxDiscountAmount && (
                  <div><Text type="secondary" style={{ fontSize: 12 }}>최대 {formatPrice(coupon.maxDiscountAmount)} 할인</Text></div>
                )}
                <div style={{ marginTop: 4 }}>
                  <Text type="secondary" style={{ fontSize: 12 }}>~ {formatDate(coupon.expiresAt)}</Text>
                </div>
              </Card>
            </Col>
          ))}
        </Row>
      )}
    </div>
  );
}
