'use client';

import { useState } from 'react';
import { Card, Form, Input, Button, Typography, Steps, message, Result } from 'antd';
import { MailOutlined, PhoneOutlined, LockOutlined, SafetyOutlined } from '@ant-design/icons';
import Link from 'next/link';
import { authApi } from '@/lib/api/endpoints';
import { ROUTES } from '@/lib/utils/constants';

const { Title } = Typography;

export default function PasswordResetPage() {
  const [step, setStep] = useState(0);
  const [loading, setLoading] = useState(false);
  const [email, setEmail] = useState('');
  const [resetToken, setResetToken] = useState('');

  const [requestForm] = Form.useForm();
  const [verifyForm] = Form.useForm();
  const [resetForm] = Form.useForm();

  const handleRequest = async (values: { email: string; phone: string }) => {
    setLoading(true);
    try {
      await authApi.passwordResetRequest(values.email, values.phone);
      setEmail(values.email);
      setStep(1);
      message.success('인증 메일이 발송되었습니다.');
    } catch (err: any) {
      message.error(err?.response?.data?.error?.message || '요청에 실패했습니다.');
    } finally { setLoading(false); }
  };

  const handleVerify = async (values: { code: string }) => {
    setLoading(true);
    try {
      const { data } = await authApi.passwordResetVerify(email, values.code);
      setResetToken(data.data.resetToken);
      setStep(2);
    } catch (err: any) {
      message.error(err?.response?.data?.error?.message || '인증에 실패했습니다.');
    } finally { setLoading(false); }
  };

  const handleReset = async (values: { newPassword: string }) => {
    setLoading(true);
    try {
      await authApi.passwordResetConfirm(resetToken, values.newPassword);
      setStep(3);
    } catch (err: any) {
      message.error(err?.response?.data?.error?.message || '비밀번호 변경에 실패했습니다.');
    } finally { setLoading(false); }
  };

  if (step === 3) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 'calc(100vh - 180px)' }}>
        <Result
          status="success"
          title="비밀번호가 변경되었습니다!"
          extra={<Link href={ROUTES.LOGIN}><Button type="primary">로그인 하기</Button></Link>}
        />
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 'calc(100vh - 180px)', padding: 24 }}>
      <Card style={{ width: 420 }}>
        <Title level={3} style={{ textAlign: 'center', marginBottom: 24 }}>비밀번호 찾기</Title>
        <Steps current={step} size="small" style={{ marginBottom: 24 }}
          items={[{ title: '본인 확인' }, { title: '인증 코드' }, { title: '새 비밀번호' }]} />

        {step === 0 && (
          <Form form={requestForm} layout="vertical" onFinish={handleRequest}>
            <Form.Item name="email" rules={[{ required: true, type: 'email' }]}>
              <Input prefix={<MailOutlined />} placeholder="이메일" />
            </Form.Item>
            <Form.Item name="phone" rules={[{ required: true }]}>
              <Input prefix={<PhoneOutlined />} placeholder="가입 시 등록한 전화번호" />
            </Form.Item>
            <Button type="primary" htmlType="submit" block loading={loading}>인증 메일 발송</Button>
          </Form>
        )}

        {step === 1 && (
          <Form form={verifyForm} layout="vertical" onFinish={handleVerify}>
            <Form.Item name="code" rules={[{ required: true, len: 6 }]}>
              <Input prefix={<SafetyOutlined />} placeholder="6자리 인증 코드" maxLength={6} />
            </Form.Item>
            <Button type="primary" htmlType="submit" block loading={loading}>인증 확인</Button>
          </Form>
        )}

        {step === 2 && (
          <Form form={resetForm} layout="vertical" onFinish={handleReset}>
            <Form.Item name="newPassword" rules={[
              { required: true },
              { min: 8 },
              { pattern: /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*])/, message: '영문, 숫자, 특수문자 포함' },
            ]}>
              <Input.Password prefix={<LockOutlined />} placeholder="새 비밀번호" />
            </Form.Item>
            <Form.Item name="confirmPassword" dependencies={['newPassword']} rules={[
              { required: true },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('newPassword') === value) return Promise.resolve();
                  return Promise.reject(new Error('비밀번호가 일치하지 않습니다'));
                },
              }),
            ]}>
              <Input.Password prefix={<LockOutlined />} placeholder="비밀번호 확인" />
            </Form.Item>
            <Button type="primary" htmlType="submit" block loading={loading}>비밀번호 변경</Button>
          </Form>
        )}
      </Card>
    </div>
  );
}
