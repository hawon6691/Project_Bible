'use client';

import { Card, Typography, Empty } from 'antd';

const { Title } = Typography;

export default function AdminStatsPage() {
  return (
    <div>
      <Title level={3}>통계</Title>
      <Card>
        <Empty description="통계 기능은 백엔드 API 구현 후 연동됩니다." />
      </Card>
    </div>
  );
}
