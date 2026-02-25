'use client';

import { useEffect, useState } from 'react';
import { Card, Typography, Form, Select, InputNumber, Button, Radio, Space, Divider, message, Spin, Row, Col } from 'antd';
import { useRouter } from 'next/navigation';
import { useCartStore } from '@/lib/stores/cartStore';
import { useAuthStore } from '@/lib/stores/authStore';
import { addressApi, couponApi, orderApi } from '@/lib/api/endpoints';
import { formatPrice } from '@/lib/utils/format';
import { ROUTES } from '@/lib/utils/constants';
import type { Address, Coupon } from '@/types/order.types';

const { Title, Text } = Typography;

export default function CheckoutPage() {
  const router = useRouter();
  const { isAuthenticated, user } = useAuthStore();
  const { items, clearCart } = useCartStore();
  const [form] = Form.useForm();
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [coupons, setCoupons] = useState<Coupon[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [selectedCoupon, setSelectedCoupon] = useState<Coupon | null>(null);
  const [usePoints, setUsePoints] = useState(0);

  useEffect(() => {
    if (!isAuthenticated) { router.push(ROUTES.LOGIN); return; }
    Promise.all([addressApi.getAll().then((r) => setAddresses(r.data.data)), couponApi.getMyCoupons().then((r) => setCoupons(r.data.data.filter((c: Coupon) => !c.isUsed)))]).catch(() => {}).finally(() => setLoading(false));
  }, [isAuthenticated, router]);

  const subtotal = items.reduce((s, i) => s + i.price * i.quantity, 0);
  const shippingFee = subtotal >= 30000 ? 0 : 3000;
  const couponDiscount = selectedCoupon ? (selectedCoupon.discountType === 'PERCENT' ? Math.min(subtotal * selectedCoupon.discountValue / 100, selectedCoupon.maxDiscountAmount || Infinity) : selectedCoupon.discountValue) : 0;
  const totalAmount = subtotal + shippingFee - couponDiscount - usePoints;

  const onFinish = async (values: any) => {
    setSubmitting(true);
    try {
      await orderApi.create({
        items: items.map((i) => ({
          id: i.id,
          productId: i.productId,
          sellerId: i.sellerId,
          quantity: i.quantity,
          selectedOptions: i.selectedOptions,
        })),
        addressId: values.addressId,
        usePoints: usePoints > 0 ? usePoints : undefined,
        paymentMethod: values.paymentMethod,
      });
      message.success('주문이 완료되었습니다!'); await clearCart(); router.push(ROUTES.ORDERS);
    } catch (e: any) { message.error(e?.response?.data?.error?.message || '주문에 실패했습니다.'); } finally { setSubmitting(false); }
  };

  if (loading) return <div style={{ textAlign: 'center', padding: 100 }}><Spin size="large" /></div>;

  return (
    <div className="page-container">
      <Title level={3}>주문/결제</Title>
      <Form form={form} layout="vertical" onFinish={onFinish}>
        <Row gutter={24}>
          <Col xs={24} lg={16}>
            <Card title="배송지 정보" style={{ marginBottom: 16 }}>
              <Form.Item name="addressId" label="배송지 선택" rules={[{ required: true, message: '배송지를 선택해주세요' }]}>
                <Select placeholder="배송지를 선택해주세요">{addresses.map((a) => (<Select.Option key={a.id} value={a.id}>{a.isDefault && '[기본] '}{a.name} - {a.address} {a.addressDetail} ({a.phone})</Select.Option>))}</Select>
              </Form.Item>
              <Button type="link" onClick={() => router.push(ROUTES.MYPAGE_ADDRESSES)}>배송지 관리</Button>
            </Card>
            <Card title="주문 상품" style={{ marginBottom: 16 }}>
              {items.map((item) => (<div key={item.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 0', borderBottom: '1px solid #f0f0f0' }}><div><Text strong>{item.productName}</Text>{item.selectedOptions && <Text type="secondary" style={{ marginLeft: 8 }}>{item.selectedOptions}</Text>}<div><Text type="secondary">수량: {item.quantity}개 / 판매자: {item.sellerName}</Text></div></div><Text strong>{formatPrice(item.price * item.quantity)}</Text></div>))}
            </Card>
            <Card title="결제 수단" style={{ marginBottom: 16 }}>
              <Form.Item name="paymentMethod" rules={[{ required: true, message: '결제 수단을 선택해주세요' }]}>
                <Radio.Group><Space direction="vertical"><Radio value="CARD">신용/체크카드</Radio><Radio value="BANK_TRANSFER">계좌이체</Radio><Radio value="VIRTUAL_ACCOUNT">가상계좌</Radio><Radio value="MOBILE">휴대폰 결제</Radio></Space></Radio.Group>
              </Form.Item>
            </Card>
          </Col>
          <Col xs={24} lg={8}><Card>
            <Title level={5}>결제 정보</Title>
            <div style={{ marginBottom: 16 }}><Text>쿠폰</Text><Select style={{ width: '100%', marginTop: 4 }} placeholder="쿠폰 선택 (선택사항)" allowClear onChange={(id) => setSelectedCoupon(coupons.find((c) => c.id === id) || null)}>{coupons.map((c) => (<Select.Option key={c.id} value={c.id}>{c.name} ({c.discountType === 'PERCENT' ? `${c.discountValue}%` : formatPrice(c.discountValue)})</Select.Option>))}</Select></div>
            <div style={{ marginBottom: 16 }}><Text>포인트 사용 (보유: {(user?.point || 0).toLocaleString()}P)</Text><InputNumber style={{ width: '100%', marginTop: 4 }} min={0} max={user?.point || 0} value={usePoints} onChange={(v) => setUsePoints(v || 0)} /></div>
            <Divider />
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}><Text>상품금액</Text><Text>{formatPrice(subtotal)}</Text></div>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}><Text>배송비</Text><Text>{shippingFee === 0 ? '무료' : formatPrice(shippingFee)}</Text></div>
            {couponDiscount > 0 && <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}><Text>쿠폰 할인</Text><Text type="danger">-{formatPrice(couponDiscount)}</Text></div>}
            {usePoints > 0 && <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}><Text>포인트 사용</Text><Text type="danger">-{formatPrice(usePoints)}</Text></div>}
            <Divider />
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}><Text strong style={{ fontSize: 16 }}>총 결제금액</Text><Text strong style={{ fontSize: 20, color: '#1677ff' }}>{formatPrice(Math.max(0, totalAmount))}</Text></div>
            <Button type="primary" htmlType="submit" block size="large" loading={submitting}>결제하기</Button>
          </Card></Col>
        </Row>
      </Form>
    </div>
  );
}
