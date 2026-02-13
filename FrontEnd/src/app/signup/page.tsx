'use client';

import { useState } from 'react';
import { Card, Form, Input, Button, Typography, message, Result } from 'antd';
import { MailOutlined, LockOutlined, UserOutlined, PhoneOutlined } from '@ant-design/icons';
import Link from 'next/link';
import { authApi } from '@/lib/api/endpoints';
import { ROUTES } from '@/lib/utils/constants';

const { Title } = Typography;

export default function SignupPage() {
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  const onFinish = async (values: { email: string; password: string; name: string; phone: string }) => {
    setLoading(true);
    try { await authApi.signup(values); setSuccess(true); }
    catch (err: any) { message.error(err?.response?.data?.error?.message || '회원가입에 실패했습니다.'); }
    finally { setLoading(false); }
  };

  if (success) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 'calc(100vh - 180px)' }}>
        <Result status="success" title="회원가입 완료!" subTitle="인증 메일이 발송되었습니다. 이메일을 확인해주세요."
          extra={<Link href={ROUTES.LOGIN}><Button type="primary">로그인 하기</Button></Link>} />
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 'calc(100vh - 180px)', padding: 24 }}>
      <Card style={{ width: 420 }}>
        <Title level={3} style={{ textAlign: 'center', marginBottom: 24 }}>회원가입</Title>
        <Form layout="vertical" onFinish={onFinish} size="large">
          <Form.Item name="email" rules={[{ required: true, message: '이메일을 입력해주세요' }, { type: 'email' }]}><Input prefix={<MailOutlined />} placeholder="이메일" /></Form.Item>
          <Form.Item name="password" rules={[{ required: true }, { min: 8 }, { pattern: /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*])/, message: '영문, 숫자, 특수문자 포함' }]}><Input.Password prefix={<LockOutlined />} placeholder="비밀번호 (영문+숫자+특수문자 8자 이상)" /></Form.Item>
          <Form.Item name="confirmPassword" dependencies={['password']} rules={[{ required: true }, ({ getFieldValue }) => ({ validator(_, v) { if (!v || getFieldValue('password') === v) return Promise.resolve(); return Promise.reject(new Error('비밀번호가 일치하지 않습니다')); } })]}>
            <Input.Password prefix={<LockOutlined />} placeholder="비밀번호 확인" /></Form.Item>
          <Form.Item name="name" rules={[{ required: true }, { min: 2, max: 20 }]}><Input prefix={<UserOutlined />} placeholder="이름" /></Form.Item>
          <Form.Item name="phone" rules={[{ required: true }, { pattern: /^01[016789]-?\d{3,4}-?\d{4}$/, message: '올바른 전화번호를 입력해주세요' }]}><Input prefix={<PhoneOutlined />} placeholder="전화번호 (010-1234-5678)" /></Form.Item>
          <Form.Item><Button type="primary" htmlType="submit" block loading={loading}>회원가입</Button></Form.Item>
        </Form>
        <div style={{ textAlign: 'center' }}>이미 계정이 있으신가요? <Link href={ROUTES.LOGIN}>로그인</Link></div>
      </Card>
    </div>
  );
}
