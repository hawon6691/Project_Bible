'use client';

import { useEffect, useState } from 'react';
import { Card, Col, Row, Spin, Table, Tag, Typography, message } from 'antd';
import { adminApi, observabilityApi } from '@/lib/api/endpoints';
import { formatDateTime } from '@/lib/utils/format';

const { Title } = Typography;

type Metrics = {
  totalRequests: number;
  errorRequests: number;
  errorRate: number;
  avgLatencyMs: number;
  p95LatencyMs: number;
  p99LatencyMs: number;
};

type TraceItem = {
  requestId: string;
  method: string;
  path: string;
  statusCode: number;
  durationMs: number;
  timestamp: string;
};

export default function AdminStatsPage() {
  const [loading, setLoading] = useState(true);
  const [opsStats, setOpsStats] = useState<any>(null);
  const [metrics, setMetrics] = useState<Metrics | null>(null);
  const [traces, setTraces] = useState<TraceItem[]>([]);

  useEffect(() => {
    Promise.all([
      adminApi.getStats(),
      observabilityApi.getMetrics(),
      observabilityApi.getTraces({ limit: 20 }),
    ])
      .then(([opsRes, metricsRes, tracesRes]) => {
        setOpsStats(opsRes.data.data || null);
        setMetrics(metricsRes.data.data || null);
        setTraces(tracesRes.data.data?.items || []);
      })
      .catch(() => message.error('통계 조회에 실패했습니다.'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 80 }}>
        <Spin size="large" />
      </div>
    );
  }

  const traceColumns = [
    { title: '시간', dataIndex: 'timestamp', key: 'timestamp', render: (v: string) => formatDateTime(v), width: 140 },
    { title: '메서드', dataIndex: 'method', key: 'method', width: 80 },
    { title: '경로', dataIndex: 'path', key: 'path' },
    {
      title: '상태코드',
      dataIndex: 'statusCode',
      key: 'statusCode',
      width: 100,
      render: (code: number) => (
        <Tag color={code >= 500 ? 'red' : code >= 400 ? 'orange' : 'green'}>{code}</Tag>
      ),
    },
    { title: '지연(ms)', dataIndex: 'durationMs', key: 'durationMs', width: 100 },
  ];

  return (
    <div>
      <Title level={3}>통계</Title>
      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={12} md={8} lg={6}>
          <Card title="운영 상태">
            <Tag color={opsStats?.overallStatus === 'up' ? 'green' : 'orange'}>
              {opsStats?.overallStatus || '-'}
            </Tag>
          </Card>
        </Col>
        <Col xs={12} md={8} lg={6}>
          <Card title="경보 수">{opsStats?.alertCount ?? 0}</Card>
        </Col>
        <Col xs={12} md={8} lg={6}>
          <Card title="요청 수(윈도우)">{metrics?.totalRequests ?? 0}</Card>
        </Col>
        <Col xs={12} md={8} lg={6}>
          <Card title="에러율">{metrics ? `${(metrics.errorRate * 100).toFixed(2)}%` : '-'}</Card>
        </Col>
        <Col xs={12} md={8} lg={6}>
          <Card title="평균 지연">{metrics?.avgLatencyMs ?? 0}ms</Card>
        </Col>
        <Col xs={12} md={8} lg={6}>
          <Card title="P95 지연">{metrics?.p95LatencyMs ?? 0}ms</Card>
        </Col>
      </Row>

      <Card title="최근 요청 트레이스">
        <Table rowKey={(r) => `${r.requestId}-${r.timestamp}`} dataSource={traces} columns={traceColumns} pagination={false} />
      </Card>
    </div>
  );
}
