'use client';

import { Card, Typography, Empty } from 'antd';

const { Title } = Typography;

export default function AdminFaqsPage() {
  return (
    <div>
      <Title level={3}>FAQ 관리</Title>
      <Card>
        <Empty description="FAQ 관리 기능은 백엔드 API 구현 후 연동됩니다." />
      </Card>
    </div>
  );
}
