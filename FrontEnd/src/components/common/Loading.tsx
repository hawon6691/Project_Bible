'use client';

import { Spin } from 'antd';

interface Props {
  tip?: string;
}

export default function Loading({ tip }: Props) {
  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', padding: 100 }}>
      <Spin size="large" tip={tip} />
    </div>
  );
}
