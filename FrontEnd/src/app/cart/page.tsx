'use client';

import { useEffect, useState } from 'react';
import { Card, Table, Typography, Button, InputNumber, Space, Empty, Spin, Popconfirm, message, Row, Col, Divider } from 'antd';
import { DeleteOutlined, ShoppingCartOutlined } from '@ant-design/icons';
import { useRouter } from 'next/navigation';
import { useCartStore } from '@/lib/stores/cartStore';
import { useAuthStore } from '@/lib/stores/authStore';
import { formatPrice } from '@/lib/utils/format';
import { ROUTES } from '@/lib/utils/constants';
import type { CartItem } from '@/types/order.types';

const { Title, Text } = Typography;

export default function CartPage() {
  const router = useRouter();
  const { isAuthenticated, isLoading: authLoading } = useAuthStore();
  const { items, isLoading, fetchCart, updateQuantity, removeItem, clearCart } = useCartStore();
  const [selectedKeys, setSelectedKeys] = useState<number[]>([]);

  useEffect(() => { if (!authLoading && !isAuthenticated) { router.push(ROUTES.LOGIN); return; } if (isAuthenticated) fetchCart(); }, [isAuthenticated, authLoading, fetchCart, router]);
  useEffect(() => { setSelectedKeys(items.map((i) => i.id)); }, [items]);

  const selectedItems = items.filter((i) => selectedKeys.includes(i.id));
  const totalPrice = selectedItems.reduce((s, i) => s + i.price * i.quantity, 0);

  const columns = [
    { title: '상품', key: 'product', render: (_: any, r: CartItem) => (<Space><div style={{ width: 60, height: 60, background: '#000', borderRadius: 4, overflow: 'hidden', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>{r.thumbnailUrl ? <img src={r.thumbnailUrl} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} /> : <span style={{ color: '#fff', fontSize: 10, fontWeight: 600 }}>NO IMAGE</span>}</div><div><Text strong style={{ cursor: 'pointer' }} onClick={() => router.push(ROUTES.PRODUCT_DETAIL(r.productId))}>{r.productName}</Text>{r.selectedOptions && <div><Text type="secondary" style={{ fontSize: 12 }}>{r.selectedOptions}</Text></div>}<div><Text type="secondary" style={{ fontSize: 12 }}>판매자: {r.sellerName}</Text></div></div></Space>) },
    { title: '가격', dataIndex: 'price', key: 'price', width: 120, render: (p: number) => formatPrice(p) },
    { title: '수량', key: 'quantity', width: 130, render: (_: any, r: CartItem) => <InputNumber min={1} max={99} value={r.quantity} onChange={(v) => v && updateQuantity(r.id, v)} size="small" /> },
    { title: '합계', key: 'total', width: 120, render: (_: any, r: CartItem) => <Text strong style={{ color: '#1677ff' }}>{formatPrice(r.price * r.quantity)}</Text> },
    { title: '', key: 'action', width: 60, render: (_: any, r: CartItem) => (<Popconfirm title="삭제하시겠습니까?" onConfirm={() => removeItem(r.id)}><Button type="text" danger icon={<DeleteOutlined />} /></Popconfirm>) },
  ];

  if (authLoading) return <div style={{ textAlign: 'center', padding: 100 }}><Spin size="large" /></div>;

  return (
    <div className="page-container">
      <Title level={3}><ShoppingCartOutlined /> 장바구니</Title>
      {isLoading ? <div style={{ textAlign: 'center', padding: 60 }}><Spin size="large" /></div>
       : items.length === 0 ? <Empty description="장바구니가 비어있습니다"><Button type="primary" onClick={() => router.push(ROUTES.PRODUCTS)}>쇼핑하러 가기</Button></Empty>
       : (<Row gutter={24}>
          <Col xs={24} lg={16}><Card>
            <div style={{ marginBottom: 12, display: 'flex', justifyContent: 'space-between' }}><Text>총 {items.length}개 상품</Text><Popconfirm title="장바구니를 비우시겠습니까?" onConfirm={clearCart}><Button size="small" danger>전체 삭제</Button></Popconfirm></div>
            <Table dataSource={items} columns={columns} rowKey="id" pagination={false} rowSelection={{ selectedRowKeys: selectedKeys, onChange: (k) => setSelectedKeys(k as number[]) }} />
          </Card></Col>
          <Col xs={24} lg={8}><Card>
            <Title level={5}>주문 요약</Title>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}><Text>선택 상품 ({selectedItems.length}개)</Text><Text>{formatPrice(totalPrice)}</Text></div>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}><Text>배송비</Text><Text>{totalPrice >= 30000 ? '무료' : formatPrice(3000)}</Text></div>
            <Divider />
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}><Text strong style={{ fontSize: 16 }}>총 결제금액</Text><Text strong style={{ fontSize: 20, color: '#1677ff' }}>{formatPrice(totalPrice + (totalPrice >= 30000 || totalPrice === 0 ? 0 : 3000))}</Text></div>
            <Button type="primary" block size="large" disabled={selectedItems.length === 0} onClick={() => router.push(ROUTES.CHECKOUT)}>주문하기 ({selectedItems.length}개)</Button>
          </Card></Col>
        </Row>)}
    </div>
  );
}
