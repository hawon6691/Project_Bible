'use client';

import { useEffect, useState } from 'react';
import { Card, Typography, Descriptions, Tag, Table, Steps, Button, Spin, message, Popconfirm, Space, Divider } from 'antd';
import { useParams, useRouter } from 'next/navigation';
import { orderApi } from '@/lib/api/endpoints';
import { useAuthStore } from '@/lib/stores/authStore';
import { formatPrice, formatDateTime } from '@/lib/utils/format';
import { ROUTES, ORDER_STATUS_LABELS, ORDER_STATUS_COLORS } from '@/lib/utils/constants';
import type { Order } from '@/types/order.types';

const { Title, Text } = Typography;
const statusSteps = ['PENDING', 'PAID', 'PREPARING', 'SHIPPING', 'DELIVERED'];

export default function OrderDetailPage() {
  const params = useParams();
  const router = useRouter();
  const orderId = Number(params.id);
  const { isAuthenticated, isLoading: authLoading } = useAuthStore();
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) { router.push(ROUTES.LOGIN); return; }
    if (isAuthenticated) orderApi.getOne(orderId).then((r) => setOrder(r.data.data)).catch(() => message.error('주문을 찾을 수 없습니다.')).finally(() => setLoading(false));
  }, [orderId, isAuthenticated, authLoading, router]);

  const handleCancel = async () => {
    try { await orderApi.cancel(orderId); message.success('주문이 취소되었습니다.'); const { data } = await orderApi.getOne(orderId); setOrder(data.data); }
    catch (e: any) { message.error(e?.response?.data?.error?.message || '취소에 실패했습니다.'); }
  };

  if (loading || authLoading) return <div style={{ textAlign: 'center', padding: 100 }}><Spin size="large" /></div>;
  if (!order) return <div style={{ textAlign: 'center', padding: 100 }}><Text>주문을 찾을 수 없습니다.</Text></div>;

  const currentStep = statusSteps.indexOf(order.status as string);

  return (
    <div className="page-container">
      <Title level={3}>주문 상세</Title>
      {currentStep >= 0 && <Card style={{ marginBottom: 24 }}><Steps current={currentStep} items={statusSteps.map((s) => ({ title: ORDER_STATUS_LABELS[s] }))} /></Card>}
      <Card style={{ marginBottom: 24 }}>
        <Descriptions title="주문 정보" bordered column={{ xs: 1, sm: 2 }}>
          <Descriptions.Item label="주문번호">{order.orderNumber}</Descriptions.Item>
          <Descriptions.Item label="주문일시">{formatDateTime(order.createdAt)}</Descriptions.Item>
          <Descriptions.Item label="상태"><Tag color={ORDER_STATUS_COLORS[order.status]}>{ORDER_STATUS_LABELS[order.status]}</Tag></Descriptions.Item>
          {order.paidAt && <Descriptions.Item label="결제일시">{formatDateTime(order.paidAt)}</Descriptions.Item>}
        </Descriptions>
      </Card>
      <Card title="주문 상품" style={{ marginBottom: 24 }}>
        <Table dataSource={order.items} rowKey="id" pagination={false} columns={[
          { title: '상품', key: 'product', render: (_: any, r: any) => (<Space><div style={{ width: 50, height: 50, background: '#f5f5f5', borderRadius: 4 }}>{r.thumbnailUrl && <img src={r.thumbnailUrl} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />}</div><div><Text strong style={{ cursor: 'pointer' }} onClick={() => router.push(ROUTES.PRODUCT_DETAIL(r.productId))}>{r.productName}</Text><div><Text type="secondary" style={{ fontSize: 12 }}>판매자: {r.sellerName}</Text></div></div></Space>) },
          { title: '단가', dataIndex: 'unitPrice', key: 'unitPrice', render: (v: number) => formatPrice(v) },
          { title: '수량', dataIndex: 'quantity', key: 'quantity' },
          { title: '합계', dataIndex: 'totalPrice', key: 'totalPrice', render: (v: number) => formatPrice(v) },
        ]} />
      </Card>
      {order.shippingAddress && (<Card title="배송지 정보" style={{ marginBottom: 24 }}><Descriptions column={1}><Descriptions.Item label="수령인">{order.shippingAddress.name}</Descriptions.Item><Descriptions.Item label="연락처">{order.shippingAddress.phone}</Descriptions.Item><Descriptions.Item label="주소">({order.shippingAddress.zipCode}) {order.shippingAddress.address} {order.shippingAddress.addressDetail}</Descriptions.Item></Descriptions></Card>)}
      <Card title="결제 정보" style={{ marginBottom: 24 }}>
        <div style={{ maxWidth: 400 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}><Text>상품금액</Text><Text>{formatPrice(order.totalAmount)}</Text></div>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}><Text>배송비</Text><Text>{formatPrice(order.shippingFee)}</Text></div>
          {order.discountAmount > 0 && <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}><Text>할인</Text><Text type="danger">-{formatPrice(order.discountAmount)}</Text></div>}
          <Divider />
          <div style={{ display: 'flex', justifyContent: 'space-between' }}><Text strong style={{ fontSize: 16 }}>총 결제금액</Text><Text strong style={{ fontSize: 20, color: '#1677ff' }}>{formatPrice(order.finalAmount)}</Text></div>
        </div>
      </Card>
      <Space>
        <Button onClick={() => router.push(ROUTES.ORDERS)}>목록으로</Button>
        {(order.status === 'PENDING' || order.status === 'PAID') && <Popconfirm title="주문을 취소하시겠습니까?" onConfirm={handleCancel}><Button danger>주문 취소</Button></Popconfirm>}
      </Space>
    </div>
  );
}
