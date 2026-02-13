'use client';

import { Card, Typography, Rate, Tag } from 'antd';
import Link from 'next/link';
import { formatPrice, formatPriceDiff } from '@/lib/utils/format';
import { ROUTES } from '@/lib/utils/constants';
import type { ProductSummary } from '@/types/product.types';

const { Text, Paragraph } = Typography;

interface Props { product: ProductSummary; }

export default function ProductCard({ product }: Props) {
  return (
    <Link href={ROUTES.PRODUCT_DETAIL(product.id)}>
      <Card hoverable
        cover={<div style={{ height: 200, overflow: 'hidden', background: '#f5f5f5', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          {product.thumbnailUrl ? <img src={product.thumbnailUrl} alt={product.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} /> : <Text type="secondary">No Image</Text>}
        </div>}
        bodyStyle={{ padding: 12 }}>
        <Paragraph ellipsis={{ rows: 2 }} style={{ marginBottom: 4, fontSize: 14, minHeight: 42 }}>{product.name}</Paragraph>
        <div style={{ marginBottom: 4 }}>
          <Text strong style={{ fontSize: 16, color: '#1677ff' }}>{formatPrice(product.lowestPrice)}</Text>
          {product.priceDiff !== undefined && product.priceDiff !== 0 && (
            <Text style={{ fontSize: 12, marginLeft: 4 }} className={product.priceDiff < 0 ? 'price-down' : 'price-up'}>{formatPriceDiff(product.priceDiff)}</Text>
          )}
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
          <Rate disabled defaultValue={product.averageRating} style={{ fontSize: 12 }} />
          <Text type="secondary" style={{ fontSize: 12 }}>({product.reviewCount})</Text>
        </div>
        {product.sellerCount > 0 && <Tag color="blue" style={{ fontSize: 11 }}>판매처 {product.sellerCount}곳</Tag>}
      </Card>
    </Link>
  );
}
