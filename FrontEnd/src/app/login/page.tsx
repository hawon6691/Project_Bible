'use client';

import { useState } from 'react';
import { Card, Form, Input, Button, Typography, message, Divider, Space } from 'antd';
import { MailOutlined, LockOutlined } from '@ant-design/icons';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/stores/authStore';
import { ROUTES } from '@/lib/utils/constants';

const { Title, Text } = Typography;

export default function LoginPage() {
  const router = useRouter();
  const login = useAuthStore((s) => s.login);
  const [loading, setLoading] = useState(false);

  const onFinish = async (values: { email: string; password: string }) => {
    setLoading(true);
    try {
      await login(values.email, values.password);
      message.success('로그인 되었습니다.');
      router.push(ROUTES.HOME);
    } catch (err: any) {
      message.error(err?.response?.data?.error?.message || '로그인에 실패했습니다.');
    } finally { setLoading(false); }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 'calc(100vh - 180px)', padding: 24 }}>
      <Card style={{ width: 400 }}>
        <Title level={3} style={{ textAlign: 'center', marginBottom: 24 }}>로그인</Title>
        <Form layout="vertical" onFinish={onFinish} size="large">
          <Form.Item name="email" rules={[{ required: true, message: '이메일을 입력해주세요' }, { type: 'email', message: '올바른 이메일을 입력해주세요' }]}>
            <Input prefix={<MailOutlined />} placeholder="이메일" />
          </Form.Item>
          <Form.Item name="password" rules={[{ required: true, message: '비밀번호를 입력해주세요' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="비밀번호" />
          </Form.Item>
          <Form.Item><Button type="primary" htmlType="submit" block loading={loading}>로그인</Button></Form.Item>
        </Form>
        <div style={{ textAlign: 'center' }}>
          <Space split={<Divider type="vertical" />}>
            <Link href={ROUTES.SIGNUP}><Text>회원가입</Text></Link>
            <Link href="/password-reset"><Text>비밀번호 찾기</Text></Link>
          </Space>
        </div>
      </Card>
    </div>
  );
}
