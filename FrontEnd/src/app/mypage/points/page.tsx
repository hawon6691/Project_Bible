'use client';

import { useEffect, useState } from 'react';
import { Card, Table, Typography, Statistic, Spin, Empty, Pagination, Tag } from 'antd';
import { WalletOutlined } from '@ant-design/icons';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/stores/authStore';
import { formatPrice, formatDateTime } from '@/lib/utils/format';
import { ROUTES, PAGE_SIZE } from '@/lib/utils/constants';
import apiClient from '@/lib/api/client';
import type { PaginationMeta } from '@/types/common.types';

const { Title } = Typography;

interface PointHistory {
  id: number;
  type: 'EARN' | 'USE' | 'EXPIRE';
  amount: number;
  description: string;
  createdAt: string;
}

export default function PointsPage() {
  const router = useRouter();
  const { user, isAuthenticated, isLoading: authLoading } = useAuthStore();
  const [history, setHistory] = useState<PointHistory[]>([]);
  const [meta, setMeta] = useState<PaginationMeta | null>(null);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) router.push(ROUTES.LOGIN);
  }, [authLoading, isAuthenticated, router]);

  useEffect(() => {
    if (!isAuthenticated) return;
    setLoading(true);
    apiClient.get('/points/history', { params: { page, limit: PAGE_SIZE } })
      .then((res) => { setHistory(res.data.data); setMeta(res.data.meta || null); })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [isAuthenticated, page]);

  const columns = [
    { title: '일시', dataIndex: 'createdAt', key: 'createdAt', render: (d: string) => formatDateTime(d) },
    {
      title: '구분',
      dataIndex: 'type',
      key: 'type',
      render: (t: string) => (
        <Tag color={t === 'EARN' ? 'green' : t === 'USE' ? 'blue' : 'default'}>
          {t === 'EARN' ? '적립' : t === 'USE' ? '사용' : '만료'}
        </Tag>
      ),
    },
    { title: '내용', dataIndex: 'description', key: 'description' },
    {
      title: '포인트',
      dataIndex: 'amount',
      key: 'amount',
      render: (amount: number, record: PointHistory) => (
        <span style={{ color: record.type === 'EARN' ? '#52c41a' : '#ff4d4f' }}>
          {record.type === 'EARN' ? '+' : '-'}{amount.toLocaleString()}P
        </span>
      ),
    },
  ];

  return (
    <div className="page-container">
      <Title level={3}>포인트</Title>

      <Card style={{ marginBottom: 24 }}>
        <Statistic title="보유 포인트" value={user?.point || 0} suffix="P" prefix={<WalletOutlined />} />
      </Card>

      {loading ? (
        <div style={{ textAlign: 'center', padding: 60 }}><Spin size="large" /></div>
      ) : history.length === 0 ? (
        <Empty description="포인트 내역이 없습니다" />
      ) : (
        <>
          <Table dataSource={history} columns={columns} rowKey="id" pagination={false} />
          {meta && (
            <div style={{ textAlign: 'center', marginTop: 24 }}>
              <Pagination current={page} total={meta.totalCount} pageSize={PAGE_SIZE} onChange={setPage} showSizeChanger={false} />
            </div>
          )}
        </>
      )}
    </div>
  );
}
