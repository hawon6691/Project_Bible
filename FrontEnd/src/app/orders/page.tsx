'use client';

import { useEffect, useState } from 'react';
import { Card, Table, Typography, Tag, Button, Select, Spin, Empty, Pagination } from 'antd';
import { EyeOutlined } from '@ant-design/icons';
import { useRouter } from 'next/navigation';
import { orderApi } from '@/lib/api/endpoints';
import { useAuthStore } from '@/lib/stores/authStore';
import { formatPrice, formatDateTime } from '@/lib/utils/format';
import { ROUTES, ORDER_STATUS_LABELS, ORDER_STATUS_COLORS, PAGE_SIZE } from '@/lib/utils/constants';
import type { Order } from '@/types/order.types';
import type { PaginationMeta } from '@/types/common.types';

const { Title } = Typography;

export default function OrdersPage() {
  const router = useRouter();
  const { isAuthenticated, isLoading: authLoading } = useAuthStore();
  const [orders, setOrders] = useState<Order[]>([]);
  const [meta, setMeta] = useState<PaginationMeta | null>(null);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState<string | undefined>();
  const [page, setPage] = useState(1);

  useEffect(() => { if (!authLoading && !isAuthenticated) router.push(ROUTES.LOGIN); }, [isAuthenticated, authLoading, router]);
  useEffect(() => {
    if (!isAuthenticated) return;
    setLoading(true);
    orderApi.getAll({ page, limit: PAGE_SIZE, status: statusFilter }).then((r) => { setOrders(r.data.data); setMeta(r.data.meta || null); }).catch(() => {}).finally(() => setLoading(false));
  }, [isAuthenticated, page, statusFilter]);

  const columns = [
    { title: '주문번호', dataIndex: 'orderNumber', key: 'orderNumber' },
    { title: '주문일', dataIndex: 'createdAt', key: 'createdAt', render: (d: string) => formatDateTime(d) },
    { title: '상품', key: 'items', render: (_: any, r: Order) => <span>{r.items[0]?.productName}{r.items.length > 1 ? ` 외 ${r.items.length - 1}건` : ''}</span> },
    { title: '결제금액', dataIndex: 'finalAmount', key: 'finalAmount', render: (v: number) => formatPrice(v) },
    { title: '상태', dataIndex: 'status', key: 'status', render: (s: string) => <Tag color={ORDER_STATUS_COLORS[s]}>{ORDER_STATUS_LABELS[s] || s}</Tag> },
    { title: '', key: 'action', render: (_: any, r: Order) => <Button type="link" icon={<EyeOutlined />} onClick={() => router.push(ROUTES.ORDER_DETAIL(r.id))}>상세</Button> },
  ];

  return (
    <div className="page-container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <Title level={3} style={{ margin: 0 }}>주문 내역</Title>
        <Select style={{ width: 150 }} placeholder="주문 상태" allowClear value={statusFilter} onChange={(v) => { setStatusFilter(v); setPage(1); }} options={Object.entries(ORDER_STATUS_LABELS).map(([k, v]) => ({ label: v, value: k }))} />
      </div>
      {loading ? <div style={{ textAlign: 'center', padding: 60 }}><Spin size="large" /></div>
       : orders.length === 0 ? <Empty description="주문 내역이 없습니다" />
       : (<><Table dataSource={orders} columns={columns} rowKey="id" pagination={false} />{meta && <div style={{ textAlign: 'center', marginTop: 24 }}><Pagination current={page} total={meta.totalCount} pageSize={PAGE_SIZE} onChange={setPage} showSizeChanger={false} /></div>}</>)}
    </div>
  );
}
