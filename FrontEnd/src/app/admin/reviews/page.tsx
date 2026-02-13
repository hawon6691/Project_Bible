'use client';

import { useEffect, useState, useCallback } from 'react';
import { Card, Table, Typography, Rate, Button, Pagination, Popconfirm, message, Space } from 'antd';
import { DeleteOutlined } from '@ant-design/icons';
import { adminApi } from '@/lib/api/endpoints';
import { formatDateTime } from '@/lib/utils/format';
import { PAGE_SIZE } from '@/lib/utils/constants';
import type { Review } from '@/types/order.types';
import type { PaginationMeta } from '@/types/common.types';

const { Title } = Typography;

export default function AdminReviewsPage() {
  const [reviews, setReviews] = useState<Review[]>([]);
  const [meta, setMeta] = useState<PaginationMeta | null>(null);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);

  const fetchReviews = useCallback(() => {
    setLoading(true);
    adminApi.getReviews({ page, limit: PAGE_SIZE })
      .then((res) => { setReviews(res.data.data); setMeta(res.data.meta || null); })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [page]);

  useEffect(() => { fetchReviews(); }, [fetchReviews]);

  const handleDelete = async (id: number) => {
    try { await adminApi.deleteReview(id); message.success('삭제되었습니다.'); fetchReviews(); }
    catch { message.error('삭제에 실패했습니다.'); }
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
    { title: '상품', dataIndex: 'productName', key: 'productName' },
    { title: '작성자', dataIndex: 'userName', key: 'userName' },
    { title: '평점', dataIndex: 'rating', key: 'rating', render: (v: number) => <Rate disabled value={v} style={{ fontSize: 12 }} /> },
    { title: '제목', dataIndex: 'title', key: 'title' },
    { title: '작성일', dataIndex: 'createdAt', key: 'createdAt', render: (d: string) => formatDateTime(d) },
    {
      title: '', key: 'action',
      render: (_: any, r: Review) => (
        <Popconfirm title="삭제하시겠습니까?" onConfirm={() => handleDelete(r.id)}>
          <Button size="small" danger icon={<DeleteOutlined />} />
        </Popconfirm>
      ),
    },
  ];

  return (
    <div>
      <Title level={3}>리뷰 관리</Title>
      <Card>
        <Table dataSource={reviews} columns={columns} rowKey="id" loading={loading} pagination={false} size="middle" />
        {meta && <div style={{ textAlign: 'center', marginTop: 16 }}><Pagination current={page} total={meta.totalCount} pageSize={PAGE_SIZE} onChange={setPage} showSizeChanger={false} /></div>}
      </Card>
    </div>
  );
}
