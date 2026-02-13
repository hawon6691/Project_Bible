'use client';

import { Table, Typography } from 'antd';
import type { ProductSpec } from '@/types/product.types';

const { Title } = Typography;

interface Props { specs: ProductSpec[]; }

export default function SpecTable({ specs }: Props) {
  if (!specs || specs.length === 0) return null;
  return (
    <div>
      <Title level={5}>상세 스펙</Title>
      <Table
        dataSource={specs.map((s, i) => ({ ...s, key: i }))}
        columns={[{ title: '항목', dataIndex: 'name', key: 'name', width: 150 }, { title: '값', dataIndex: 'value', key: 'value' }]}
        pagination={false} size="small" bordered
      />
    </div>
  );
}
