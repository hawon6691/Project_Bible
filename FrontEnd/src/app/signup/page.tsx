'use client';

import { useState } from 'react';
import { Card, Form, Input, Button, Typography, message, Result } from 'antd';
import { MailOutlined, LockOutlined, UserOutlined, PhoneOutlined } from '@ant-design/icons';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { authApi } from '@/lib/api/endpoints';
import { ROUTES } from '@/lib/utils/constants';

const { Title } = Typography;

export default function SignupPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [signupEmail, setSignupEmail] = useState('');
  const [verifyLoading, setVerifyLoading] = useState(false);
  const [resendLoading, setResendLoading] = useState(false);

  const onFinish = async (values: { email: string; password: string; confirmPassword?: string; name: string; phone: string }) => {
    setLoading(true);
    try {
      const { confirmPassword: _confirmPassword, ...payload } = values;
      await authApi.signup(payload);
      setSignupEmail(payload.email);
      setSuccess(true);
    }
    catch (err: any) { message.error(err?.response?.data?.error?.message || '회원가입에 실패했습니다.'); }
    finally { setLoading(false); }
  };

  const onVerifyFinish = async (values: { code: string }) => {
    if (!signupEmail) {
      message.error('인증할 이메일 정보가 없습니다. 다시 회원가입을 진행해주세요.');
      return;
    }

    setVerifyLoading(true);
    try {
      await authApi.verifyEmail(signupEmail, values.code);
      message.success('이메일 인증이 완료되었습니다. 로그인 페이지로 이동합니다.');
      router.replace(ROUTES.LOGIN);
    } catch (err: any) {
      message.error(err?.response?.data?.error?.message || '인증에 실패했습니다.');
    } finally {
      setVerifyLoading(false);
    }
  };

  const onResend = async () => {
    if (!signupEmail) {
      message.error('재발송할 이메일 정보가 없습니다. 다시 회원가입을 진행해주세요.');
      return;
    }

    setResendLoading(true);
    try {
      await authApi.resendVerification(signupEmail);
      message.success('인증 메일을 재발송했습니다.');
    } catch (err: any) {
      message.error(err?.response?.data?.error?.message || '재발송에 실패했습니다.');
    } finally {
      setResendLoading(false);
    }
  };

  if (success) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 'calc(100vh - 180px)' }}>
        <Card style={{ width: 460 }}>
          <Result
            status="success"
            title="회원가입 완료!"
            subTitle={`${signupEmail || '입력한 이메일'}로 인증코드를 보냈습니다.`}
          />
          <Form layout="vertical" onFinish={onVerifyFinish}>
            <Form.Item
              name="code"
              label="이메일 인증코드"
              rules={[
                { required: true, message: '인증코드를 입력해주세요' },
                { len: 6, message: '인증코드는 6자리입니다' },
                { pattern: /^\d{6}$/, message: '숫자 6자리만 입력해주세요' },
              ]}
            >
              <Input placeholder="예: 123456" />
            </Form.Item>
            <Form.Item style={{ marginBottom: 8 }}>
              <Button type="primary" htmlType="submit" block loading={verifyLoading}>
                인증 완료
              </Button>
            </Form.Item>
          </Form>
          <Button block onClick={onResend} loading={resendLoading} style={{ marginBottom: 8 }}>
            인증코드 재발송
          </Button>
          <Link href={ROUTES.LOGIN}>
            <Button block>로그인 페이지로 이동</Button>
          </Link>
        </Card>
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 'calc(100vh - 180px)', padding: 24 }}>
      <Card style={{ width: 420 }}>
        <Title level={3} style={{ textAlign: 'center', marginBottom: 24 }}>회원가입</Title>
        <Form layout="vertical" onFinish={onFinish} size="large">
          <Form.Item name="email" rules={[{ required: true, message: '이메일을 입력해주세요' }, { type: 'email' }]}><Input prefix={<MailOutlined />} placeholder="이메일" /></Form.Item>
          <Form.Item
            name="password"
            rules={[
              { required: true, message: '비밀번호를 입력해주세요' },
              { min: 8, message: '비밀번호는 8자 이상이어야 합니다' },
              {
                pattern: /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]).{8,}$/,
                message: '대문자/소문자/숫자/특수문자를 각각 1개 이상 포함해야 합니다',
              },
            ]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="비밀번호 (예: Abcd1234!)" />
          </Form.Item>
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
