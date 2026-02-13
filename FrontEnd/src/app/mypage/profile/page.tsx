'use client';

import { useEffect, useState } from 'react';
import { Card, Form, Input, Button, Typography, message, Divider, Spin } from 'antd';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/stores/authStore';
import { userApi } from '@/lib/api/endpoints';
import { ROUTES } from '@/lib/utils/constants';

const { Title } = Typography;

export default function ProfilePage() {
  const router = useRouter();
  const { user, isAuthenticated, isLoading, fetchUser } = useAuthStore();
  const [form] = Form.useForm();
  const [passwordForm] = Form.useForm();
  const [saving, setSaving] = useState(false);
  const [changingPw, setChangingPw] = useState(false);

  useEffect(() => {
    if (!isLoading && !isAuthenticated) router.push(ROUTES.LOGIN);
  }, [isLoading, isAuthenticated, router]);

  useEffect(() => {
    if (user) {
      form.setFieldsValue({ name: user.name, phone: user.phone });
    }
  }, [user, form]);

  const onUpdateProfile = async (values: { name: string; phone: string }) => {
    setSaving(true);
    try {
      await userApi.updateMe(values);
      await fetchUser();
      message.success('프로필이 수정되었습니다.');
    } catch (err: any) {
      message.error(err?.response?.data?.error?.message || '수정에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  };

  const onChangePassword = async (values: { password: string }) => {
    setChangingPw(true);
    try {
      await userApi.updateMe({ password: values.password });
      message.success('비밀번호가 변경되었습니다.');
      passwordForm.resetFields();
    } catch (err: any) {
      message.error(err?.response?.data?.error?.message || '변경에 실패했습니다.');
    } finally {
      setChangingPw(false);
    }
  };

  if (isLoading) return <div style={{ textAlign: 'center', padding: 100 }}><Spin size="large" /></div>;

  return (
    <div className="page-container" style={{ maxWidth: 600, margin: '0 auto' }}>
      <Title level={3}>프로필 수정</Title>

      <Card style={{ marginBottom: 24 }}>
        <Form form={form} layout="vertical" onFinish={onUpdateProfile}>
          <Form.Item label="이메일">
            <Input value={user?.email} disabled />
          </Form.Item>
          <Form.Item name="name" label="이름" rules={[{ required: true, message: '이름을 입력해주세요' }, { min: 2, max: 20 }]}>
            <Input />
          </Form.Item>
          <Form.Item name="phone" label="전화번호" rules={[{ required: true, message: '전화번호를 입력해주세요' }]}>
            <Input />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={saving}>저장</Button>
          </Form.Item>
        </Form>
      </Card>

      <Card title="비밀번호 변경">
        <Form form={passwordForm} layout="vertical" onFinish={onChangePassword}>
          <Form.Item name="password" label="새 비밀번호" rules={[
            { required: true, message: '새 비밀번호를 입력해주세요' },
            { min: 8, message: '최소 8자 이상' },
            { pattern: /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*])/, message: '영문, 숫자, 특수문자 포함' },
          ]}>
            <Input.Password />
          </Form.Item>
          <Form.Item name="confirmPassword" label="비밀번호 확인" dependencies={['password']} rules={[
            { required: true, message: '비밀번호를 확인해주세요' },
            ({ getFieldValue }) => ({
              validator(_, value) {
                if (!value || getFieldValue('password') === value) return Promise.resolve();
                return Promise.reject(new Error('비밀번호가 일치하지 않습니다'));
              },
            }),
          ]}>
            <Input.Password />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={changingPw}>비밀번호 변경</Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}
