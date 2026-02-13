'use client';

import { Layout, Space, Typography } from 'antd';
import Link from 'next/link';

const { Footer: AntFooter } = Layout;
const { Text } = Typography;

export default function Footer() {
  return (
    <AntFooter style={{ textAlign: 'center', background: '#fafafa', borderTop: '1px solid #f0f0f0' }}>
      <Space split={<Text type="secondary">|</Text>}>
        <Link href="/about"><Text type="secondary">회사소개</Text></Link>
        <Link href="/terms"><Text type="secondary">이용약관</Text></Link>
        <Link href="/privacy"><Text type="secondary">개인정보처리방침</Text></Link>
      </Space>
      <div style={{ marginTop: 8 }}>
        <Text type="secondary">&copy; 2026 NestShop. All rights reserved.</Text>
      </div>
    </AntFooter>
  );
}
