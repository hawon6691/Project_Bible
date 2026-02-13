'use client';

import { Card, Typography, Empty } from 'antd';

const { Title } = Typography;

export default function AdminBannersPage() {
  return (
    <div>
      <Title level={3}>배너 관리</Title>
      <Card>
        <Empty description="배너 관리 기능은 백엔드 API 구현 후 연동됩니다." />
      </Card>
    </div>
  );
}
