'use client';

import { useEffect, useState, useCallback } from 'react';
import { Card, Table, Typography, Tag, Select, Pagination, Space, message } from 'antd';
import { adminApi } from '@/lib/api/endpoints';
import { formatPrice, formatDateTime } from '@/lib/utils/format';
import { PAGE_SIZE, ORDER_STATUS_LABELS, ORDER_STATUS_COLORS } from '@/lib/utils/constants';
import type { Order } from '@/types/order.types';
import type { PaginationMeta } from '@/types/common.types';

const { Title } = Typography;

export default function AdminOrdersPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [meta, setMeta] = useState<PaginationMeta | null>(null);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState<string | undefined>();
  const [page, setPage] = useState(1);

  const fetchOrders = useCallback(() => {
    setLoading(true);
    adminApi.getOrders({ page, limit: PAGE_SIZE, status: statusFilter })
      .then((res) => { setOrders(res.data.data); setMeta(res.data.meta || null); })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [page, statusFilter]);

  useEffect(() => { fetchOrders(); }, [fetchOrders]);

  const handleStatusChange = async (id: number, status: string) => {
    try {
      await adminApi.updateOrderStatus(id, status);
      message.success('상태가 변경되었습니다.');
      fetchOrders();
    } catch { message.error('변경에 실패했습니다.'); }
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
    { title: '주문번호', dataIndex: 'orderNumber', key: 'orderNumber' },
    {
      title: '상품',
      key: 'items',
      render: (_: any, r: Order) => r.items?.[0]?.productName + (r.items?.length > 1 ? ` 외 ${r.items.length - 1}건` : ''),
    },
    { title: '결제금액', dataIndex: 'finalAmount', key: 'finalAmount', render: (v: number) => formatPrice(v) },
    {
      title: '상태',
      key: 'status',
      render: (_: any, r: Order) => (
        <Select
          value={r.status}
          size="small"
          style={{ width: 120 }}
          onChange={(v) => handleStatusChange(r.id, v)}
          options={Object.entries(ORDER_STATUS_LABELS).map(([k, v]) => ({ label: v, value: k }))}
        />
      ),
    },
    { title: '주문일', dataIndex: 'createdAt', key: 'createdAt', render: (d: string) => formatDateTime(d) },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={3} style={{ margin: 0 }}>주문 관리</Title>
        <Select allowClear placeholder="주문 상태" value={statusFilter} onChange={(v) => { setStatusFilter(v); setPage(1); }} style={{ width: 150 }}
          options={Object.entries(ORDER_STATUS_LABELS).map(([k, v]) => ({ label: v, value: k }))} />
      </div>
      <Card>
        <Table dataSource={orders} columns={columns} rowKey="id" loading={loading} pagination={false} size="middle" />
        {meta && <div style={{ textAlign: 'center', marginTop: 16 }}><Pagination current={page} total={meta.totalCount} pageSize={PAGE_SIZE} onChange={setPage} showSizeChanger={false} /></div>}
      </Card>
    </div>
  );
}
