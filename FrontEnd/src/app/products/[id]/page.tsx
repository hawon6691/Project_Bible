'use client';

import { useEffect, useState } from 'react';
import { Row, Col, Card, Typography, Rate, Tag, Table, Button, Spin, Descriptions, Space, message } from 'antd';
import { ShoppingCartOutlined, HeartOutlined, BellOutlined, SwapOutlined } from '@ant-design/icons';
import { useParams, useRouter } from 'next/navigation';
import { productApi } from '@/lib/api/endpoints';
import { useCartStore } from '@/lib/stores/cartStore';
import { useAuthStore } from '@/lib/stores/authStore';
import { formatPrice, formatRating } from '@/lib/utils/format';
import { ROUTES } from '@/lib/utils/constants';
import PriceChart from '@/components/product/PriceChart';
import SpecTable from '@/components/product/SpecTable';
import type { ProductDetail } from '@/types/product.types';

const { Title, Text, Paragraph } = Typography;

export default function ProductDetailPage() {
  const params = useParams();
  const router = useRouter();
  const productId = Number(params.id);
  const { addItem } = useCartStore();
  const { isAuthenticated } = useAuthStore();
  const [product, setProduct] = useState<ProductDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [selectedImage, setSelectedImage] = useState(0);

  useEffect(() => {
    productApi.getOne(productId).then((r) => setProduct(r.data.data)).catch(() => message.error('상품을 찾을 수 없습니다.')).finally(() => setLoading(false));
  }, [productId]);

  const handleAddToCart = async (sellerId: number) => {
    if (!isAuthenticated) { message.warning('로그인이 필요합니다.'); router.push(ROUTES.LOGIN); return; }
    try { await addItem({ productId, quantity: 1, sellerId }); message.success('장바구니에 추가되었습니다.'); }
    catch { message.error('장바구니 추가에 실패했습니다.'); }
  };

  if (loading) return <div style={{ display: 'flex', justifyContent: 'center', padding: 100 }}><Spin size="large" /></div>;
  if (!product) return <div style={{ textAlign: 'center', padding: 100 }}><Text>상품을 찾을 수 없습니다.</Text></div>;

  const priceColumns = [
    { title: '판매처', dataIndex: 'seller', key: 'seller', render: (s: any) => (<Space>{s.logoUrl && <img src={s.logoUrl} alt={s.name} style={{ width: 24, height: 24, borderRadius: 4 }} />}<Text strong>{s.name}</Text><Tag color="blue">신뢰도 {s.trustScore}</Tag></Space>) },
    { title: '가격', dataIndex: 'price', key: 'price', render: (p: number) => <Text strong style={{ color: '#1677ff', fontSize: 16 }}>{formatPrice(p)}</Text>, sorter: (a: any, b: any) => a.price - b.price },
    { title: '배송', dataIndex: 'shipping', key: 'shipping' },
    { title: '', key: 'action', render: (_: any, r: any) => (<Space><Button type="primary" icon={<ShoppingCartOutlined />} onClick={() => handleAddToCart(r.seller.id)}>담기</Button>{r.url && <Button href={r.url} target="_blank">구매하기</Button>}</Space>) },
  ];

  return (
    <div className="page-container">
      <Row gutter={[24, 24]}>
        <Col xs={24} md={10}>
          <Card>
            {product.images[selectedImage]?.url ? (
              <img
                src={product.images[selectedImage].url}
                alt={product.name}
                style={{ width: '100%', maxHeight: 400, objectFit: 'contain', display: 'block' }}
              />
            ) : (
              <div
                style={{
                  width: '100%',
                  height: 400,
                  background: '#000',
                  color: '#fff',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontWeight: 600,
                }}
              >
                NO IMAGE
              </div>
            )}
            {product.images.length > 1 && (<Row gutter={8} style={{ marginTop: 12 }}>{product.images.map((img, idx) => (<Col key={img.id} span={4}><img src={img.url} alt="" style={{ width: '100%', height: 60, objectFit: 'cover', cursor: 'pointer', border: idx === selectedImage ? '2px solid #1677ff' : '1px solid #d9d9d9', borderRadius: 4 }} onClick={() => setSelectedImage(idx)} /></Col>))}</Row>)}
          </Card>
        </Col>
        <Col xs={24} md={14}>
          <Card>
            {product.category && <Tag>{product.category.name}</Tag>}
            <Title level={3} style={{ marginTop: 8, marginBottom: 4 }}>{product.name}</Title>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 16 }}>
              <Rate disabled value={product.averageRating} style={{ fontSize: 16 }} /><Text>{formatRating(product.averageRating)}</Text><Text type="secondary">({product.reviewCount}개 리뷰)</Text>
            </div>
            <Descriptions column={1} size="small" bordered>
              <Descriptions.Item label="최저가"><Text strong style={{ fontSize: 24, color: '#1677ff' }}>{formatPrice(product.lowestPrice)}</Text></Descriptions.Item>
              <Descriptions.Item label="최고가">{formatPrice(product.highestPrice)}</Descriptions.Item>
              <Descriptions.Item label="평균가">{formatPrice(product.averagePrice)}</Descriptions.Item>
              <Descriptions.Item label="상태"><Tag color={product.status === 'ON_SALE' ? 'green' : 'red'}>{product.status === 'ON_SALE' ? '판매중' : product.status === 'SOLD_OUT' ? '품절' : '비공개'}</Tag></Descriptions.Item>
            </Descriptions>
            {product.options.length > 0 && (<div style={{ marginTop: 16 }}><Text strong>옵션</Text><div style={{ marginTop: 8 }}>{product.options.map((o) => (<div key={o.id} style={{ marginBottom: 8 }}><Text type="secondary">{o.name}: </Text>{o.values.map((v) => <Tag key={v}>{v}</Tag>)}</div>))}</div></div>)}
            <Space style={{ marginTop: 16 }}>
              <Button icon={<SwapOutlined />} onClick={() => router.push(`${ROUTES.COMPARE}?ids=${productId}`)}>비교하기</Button>
              <Button icon={<HeartOutlined />}>찜하기</Button>
              <Button icon={<BellOutlined />}>가격 알림</Button>
            </Space>
          </Card>
        </Col>
      </Row>
      <Card style={{ marginTop: 24 }}><Title level={4}>가격 비교</Title><Table dataSource={product.priceEntries.map((e, i) => ({ ...e, key: i }))} columns={priceColumns} pagination={false} size="middle" /></Card>
      <div style={{ marginTop: 24 }}><PriceChart productId={productId} /></div>
      <Card style={{ marginTop: 24 }}><Title level={4}>상품 설명</Title><Paragraph>{product.description}</Paragraph></Card>
      <Card style={{ marginTop: 24 }}><SpecTable specs={product.specs} /></Card>
    </div>
  );
}
