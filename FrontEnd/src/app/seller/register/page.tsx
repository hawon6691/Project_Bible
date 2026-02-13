'use client';

import { useState } from 'react';
import { Card, Form, Input, Button, Typography, message, Result } from 'antd';
import { ShopOutlined } from '@ant-design/icons';
import Link from 'next/link';
import { sellerApi } from '@/lib/api/endpoints';
import { ROUTES } from '@/lib/utils/constants';

const { Title } = Typography;

export default function SellerRegisterPage() {
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  const onFinish = async (values: any) => {
    setLoading(true);
    try {
      await sellerApi.register(values);
      setSuccess(true);
    } catch (err: any) {
      message.error(err?.response?.data?.error?.message || '등록에 실패했습니다.');
    } finally { setLoading(false); }
  };

  if (success) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 'calc(100vh - 180px)' }}>
        <Result
          status="success"
          title="판매자 등록 신청 완료!"
          subTitle="관리자 승인 후 이용 가능합니다."
          extra={<Link href={ROUTES.HOME}><Button type="primary">홈으로</Button></Link>}
        />
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', justifyContent: 'center', padding: 24, minHeight: 'calc(100vh - 180px)' }}>
      <Card style={{ width: 500 }}>
        <Title level={3} style={{ textAlign: 'center' }}><ShopOutlined /> 판매자 등록</Title>
        <Form layout="vertical" onFinish={onFinish} size="large">
          <Form.Item name="companyName" label="상호명" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="businessNumber" label="사업자등록번호" rules={[{ required: true }]}><Input placeholder="000-00-00000" /></Form.Item>
          <Form.Item name="representativeName" label="대표자명" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="phone" label="연락처" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="address" label="사업장 주소" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="website" label="웹사이트"><Input placeholder="https://" /></Form.Item>
          <Button type="primary" htmlType="submit" block loading={loading}>등록 신청</Button>
        </Form>
      </Card>
    </div>
  );
}
