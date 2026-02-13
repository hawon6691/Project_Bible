'use client';

import { useEffect, useState } from 'react';
import { Card, List, Typography, Rate, Button, Empty, Spin, Pagination, Space, Popconfirm, message, Tag } from 'antd';
import { DeleteOutlined, EditOutlined } from '@ant-design/icons';
import { useRouter } from 'next/navigation';
import { reviewApi } from '@/lib/api/endpoints';
import { useAuthStore } from '@/lib/stores/authStore';
import { formatDate } from '@/lib/utils/format';
import { ROUTES, PAGE_SIZE } from '@/lib/utils/constants';
import type { Review } from '@/types/order.types';
import type { PaginationMeta } from '@/types/common.types';

const { Title, Text, Paragraph } = Typography;

export default function MyReviewsPage() {
  const router = useRouter();
  const { isAuthenticated, isLoading: authLoading } = useAuthStore();
  const [reviews, setReviews] = useState<Review[]>([]);
  const [meta, setMeta] = useState<PaginationMeta | null>(null);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) router.push(ROUTES.LOGIN);
  }, [authLoading, isAuthenticated, router]);

  useEffect(() => {
    if (!isAuthenticated) return;
    setLoading(true);
    reviewApi.getMyReviews({ page, limit: PAGE_SIZE })
      .then((res) => { setReviews(res.data.data); setMeta(res.data.meta || null); })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [isAuthenticated, page]);

  const handleDelete = async (id: number) => {
    try {
      await reviewApi.delete(id);
      message.success('리뷰가 삭제되었습니다.');
      setReviews(reviews.filter((r) => r.id !== id));
    } catch {
      message.error('삭제에 실패했습니다.');
    }
  };

  return (
    <div className="page-container">
      <Title level={3}>내 리뷰</Title>

      {loading ? (
        <div style={{ textAlign: 'center', padding: 60 }}><Spin size="large" /></div>
      ) : reviews.length === 0 ? (
        <Empty description="작성한 리뷰가 없습니다" />
      ) : (
        <>
          <List
            dataSource={reviews}
            renderItem={(review) => (
              <Card style={{ marginBottom: 12 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <Text strong style={{ cursor: 'pointer', color: '#1677ff' }} onClick={() => router.push(ROUTES.PRODUCT_DETAIL(review.productId))}>
                      {review.productName}
                    </Text>
                    <div style={{ marginTop: 4 }}>
                      <Rate disabled value={review.rating} style={{ fontSize: 14 }} />
                      <Text type="secondary" style={{ marginLeft: 8 }}>{formatDate(review.createdAt)}</Text>
                    </div>
                    <Title level={5} style={{ marginTop: 8, marginBottom: 4 }}>{review.title}</Title>
                    <Paragraph>{review.content}</Paragraph>
                    {review.images?.length > 0 && (
                      <Space style={{ marginTop: 8 }}>
                        {review.images.map((img, i) => (
                          <img key={i} src={img} alt="" style={{ width: 60, height: 60, objectFit: 'cover', borderRadius: 4 }} />
                        ))}
                      </Space>
                    )}
                    <div style={{ marginTop: 8 }}>
                      <Tag>도움이 됐어요 {review.helpfulCount}</Tag>
                    </div>
                  </div>
                  <Space>
                    <Popconfirm title="삭제하시겠습니까?" onConfirm={() => handleDelete(review.id)} okText="삭제" cancelText="취소">
                      <Button type="text" danger icon={<DeleteOutlined />} />
                    </Popconfirm>
                  </Space>
                </div>
              </Card>
            )}
          />
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
