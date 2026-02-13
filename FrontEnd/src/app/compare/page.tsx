'use client';

import { useState } from 'react';
import { Card, Typography, Select, Button, Table, Spin, Empty, Row, Col, Tag, message, Space } from 'antd';
import { BarChartOutlined } from '@ant-design/icons';
import { useSearchParams } from 'next/navigation';
import { productApi, specApi } from '@/lib/api/endpoints';
import { formatPrice } from '@/lib/utils/format';
import type { ProductSummary, CompareResult, ScoredCompareResult } from '@/types/product.types';

const { Title, Text } = Typography;

export default function ComparePage() {
  const searchParams = useSearchParams();
  const initialIds = searchParams.get('ids')?.split(',').map(Number).filter(Boolean) || [];
  const [selectedIds, setSelectedIds] = useState<number[]>(initialIds);
  const [searchResults, setSearchResults] = useState<ProductSummary[]>([]);
  const [compareResult, setCompareResult] = useState<CompareResult | null>(null);
  const [scoredResult, setScoredResult] = useState<ScoredCompareResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [showScored, setShowScored] = useState(false);

  const handleSearch = async (value: string) => {
    if (!value.trim()) return;
    try { const { data } = await productApi.getAll({ search: value, limit: 10 }); setSearchResults(data.data); } catch {}
  };

  const addProduct = (id: number) => {
    if (selectedIds.length >= 4) { message.warning('최대 4개까지 비교할 수 있습니다.'); return; }
    if (selectedIds.includes(id)) { message.warning('이미 추가된 상품입니다.'); return; }
    setSelectedIds([...selectedIds, id]); setCompareResult(null); setScoredResult(null);
  };

  const removeProduct = (id: number) => { setSelectedIds(selectedIds.filter((i) => i !== id)); setCompareResult(null); setScoredResult(null); };

  const compare = async () => {
    if (selectedIds.length < 2) { message.warning('2개 이상의 상품을 선택해주세요.'); return; }
    setLoading(true);
    try { const { data } = await specApi.compare(selectedIds); setCompareResult(data.data); setShowScored(false); }
    catch { message.error('비교에 실패했습니다.'); } finally { setLoading(false); }
  };

  const scoredCompare = async () => {
    if (selectedIds.length < 2) return;
    setLoading(true);
    try { const { data } = await specApi.scoredCompare(selectedIds); setScoredResult(data.data); setShowScored(true); }
    catch { message.error('점수화 비교에 실패했습니다.'); } finally { setLoading(false); }
  };

  const result = showScored ? scoredResult : compareResult;

  return (
    <div className="page-container">
      <Title level={3}>상품 비교</Title>
      <Card style={{ marginBottom: 24 }}>
        <Row gutter={16} align="middle">
          <Col flex="auto"><Select showSearch placeholder="상품 검색 후 추가" style={{ width: '100%' }} filterOption={false} onSearch={handleSearch} value={undefined} onSelect={(v) => v !== undefined && addProduct(v as number)} options={searchResults.map((p) => ({ label: `${p.name} (${formatPrice(p.lowestPrice)})`, value: p.id }))} /></Col>
          <Col><Button type="primary" onClick={compare} disabled={selectedIds.length < 2} loading={loading}>비교하기</Button><Button style={{ marginLeft: 8 }} icon={<BarChartOutlined />} onClick={scoredCompare} disabled={selectedIds.length < 2}>점수 비교</Button></Col>
        </Row>
        <div style={{ marginTop: 12 }}>{selectedIds.map((id) => (<Tag key={id} closable onClose={() => removeProduct(id)} style={{ marginBottom: 4 }}>상품 #{id}</Tag>))}</div>
      </Card>
      {loading ? <div style={{ textAlign: 'center', padding: 60 }}><Spin size="large" /></div>
       : result ? (
        <Card>
          {showScored && scoredResult?.scores && (<div style={{ marginBottom: 24 }}><Title level={5}>종합 점수</Title><Row gutter={16}>{scoredResult.products.map((p) => (<Col key={p.id} span={Math.floor(24 / scoredResult.products.length)}><Card size="small" style={{ textAlign: 'center' }}><Text strong>{p.name}</Text><div style={{ fontSize: 32, fontWeight: 700, color: '#1677ff', marginTop: 8 }}>{scoredResult.scores[p.id]?.toFixed(1) || '-'}</div></Card></Col>))}</Row></div>)}
          <Table dataSource={result.specs.map((s, i) => ({ key: i, specName: s.name, ...s.values }))}
            columns={[{ title: '스펙', dataIndex: 'specName', key: 'specName', width: 150, fixed: 'left' as const }, ...result.products.map((p) => ({ title: p.name, dataIndex: String(p.id), key: String(p.id), render: (v: string) => v || '-' }))]}
            pagination={false} size="middle" bordered scroll={{ x: true }} />
        </Card>
      ) : selectedIds.length > 0 ? <Empty description="비교하기 버튼을 눌러주세요" /> : <Empty description="비교할 상품을 검색하여 추가해주세요" />}
    </div>
  );
}
