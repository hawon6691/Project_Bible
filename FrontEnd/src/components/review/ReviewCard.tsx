'use client';

import { Card, Typography, Rate, Button, Space, Tag } from 'antd';
import { LikeOutlined } from '@ant-design/icons';
import { formatDate } from '@/lib/utils/format';
import { reviewApi } from '@/lib/api/endpoints';
import type { Review } from '@/types/order.types';

const { Text, Paragraph } = Typography;

interface Props {
  review: Review;
  onHelpful?: () => void;
}

export default function ReviewCard({ review, onHelpful }: Props) {
  const handleHelpful = async () => {
    try {
      await reviewApi.helpful(review.id);
      onHelpful?.();
    } catch {}
  };

  return (
    <Card size="small" style={{ marginBottom: 12 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
        <Space>
          <Text strong>{review.userName}</Text>
          <Rate disabled value={review.rating} style={{ fontSize: 12 }} />
        </Space>
        <Text type="secondary" style={{ fontSize: 12 }}>{formatDate(review.createdAt)}</Text>
      </div>
      <Text strong>{review.title}</Text>
      <Paragraph style={{ marginTop: 4, marginBottom: 8 }}>{review.content}</Paragraph>
      {review.images?.length > 0 && (
        <Space style={{ marginBottom: 8 }}>
          {review.images.map((img, i) => (
            <img key={i} src={img} alt="" style={{ width: 60, height: 60, objectFit: 'cover', borderRadius: 4 }} />
          ))}
        </Space>
      )}
      <div>
        <Button size="small" type="text" icon={<LikeOutlined />} onClick={handleHelpful}>
          도움이 됐어요 ({review.helpfulCount})
        </Button>
      </div>
    </Card>
  );
}
