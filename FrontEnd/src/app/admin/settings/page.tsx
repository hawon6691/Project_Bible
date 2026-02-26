'use client';

import { useEffect, useState } from 'react';
import { Button, Card, Col, Form, Input, InputNumber, Row, Space, Spin, Typography, message } from 'antd';
import { settingsApi } from '@/lib/api/endpoints';

const { Title } = Typography;

export default function AdminSettingsPage() {
  const [loading, setLoading] = useState(true);
  const [savingExtensions, setSavingExtensions] = useState(false);
  const [savingUploadLimits, setSavingUploadLimits] = useState(false);
  const [savingReviewPolicy, setSavingReviewPolicy] = useState(false);
  const [extensionsForm] = Form.useForm();
  const [uploadForm] = Form.useForm();
  const [reviewForm] = Form.useForm();

  useEffect(() => {
    Promise.all([
      settingsApi.getAllowedExtensions(),
      settingsApi.getUploadLimits(),
      settingsApi.getReviewPolicy(),
    ])
      .then(([extensionsRes, uploadRes, reviewRes]) => {
        const extensions = (extensionsRes.data.data?.extensions || []).join(', ');
        extensionsForm.setFieldsValue({ extensions });
        uploadForm.setFieldsValue(uploadRes.data.data || {});
        reviewForm.setFieldsValue(reviewRes.data.data || {});
      })
      .catch(() => message.error('시스템 설정 조회에 실패했습니다.'))
      .finally(() => setLoading(false));
  }, [extensionsForm, uploadForm, reviewForm]);

  const saveExtensions = async (values: { extensions: string }) => {
    setSavingExtensions(true);
    try {
      const parsed = values.extensions
        .split(',')
        .map((v) => v.trim().toLowerCase())
        .filter(Boolean);
      await settingsApi.setAllowedExtensions(parsed);
      message.success('허용 확장자가 저장되었습니다.');
    } catch (err: any) {
      message.error(err?.response?.data?.error?.message || '허용 확장자 저장에 실패했습니다.');
    } finally {
      setSavingExtensions(false);
    }
  };

  const saveUploadLimits = async (values: { image?: number; video?: number; audio?: number }) => {
    setSavingUploadLimits(true);
    try {
      await settingsApi.updateUploadLimits(values);
      message.success('업로드 제한이 저장되었습니다.');
    } catch (err: any) {
      message.error(err?.response?.data?.error?.message || '업로드 제한 저장에 실패했습니다.');
    } finally {
      setSavingUploadLimits(false);
    }
  };

  const saveReviewPolicy = async (values: { maxImageCount: number; pointAmount: number }) => {
    setSavingReviewPolicy(true);
    try {
      await settingsApi.updateReviewPolicy(values);
      message.success('리뷰 정책이 저장되었습니다.');
    } catch (err: any) {
      message.error(err?.response?.data?.error?.message || '리뷰 정책 저장에 실패했습니다.');
    } finally {
      setSavingReviewPolicy(false);
    }
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 80 }}>
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div>
      <Title level={3}>시스템 설정</Title>
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card title="허용 확장자">
            <Form form={extensionsForm} layout="vertical" onFinish={saveExtensions}>
              <Form.Item
                name="extensions"
                label="확장자 목록 (콤마 구분)"
                rules={[{ required: true, message: '확장자를 입력하세요.' }]}
              >
                <Input placeholder="jpg, png, webp, mp4" />
              </Form.Item>
              <Button type="primary" htmlType="submit" loading={savingExtensions}>
                저장
              </Button>
            </Form>
          </Card>
        </Col>

        <Col xs={24} lg={12}>
          <Card title="업로드 용량 제한 (MB)">
            <Form form={uploadForm} layout="vertical" onFinish={saveUploadLimits}>
              <Space size={12} wrap>
                <Form.Item name="image" label="이미지">
                  <InputNumber min={1} max={1024} />
                </Form.Item>
                <Form.Item name="video" label="비디오">
                  <InputNumber min={1} max={10240} />
                </Form.Item>
                <Form.Item name="audio" label="오디오">
                  <InputNumber min={1} max={1024} />
                </Form.Item>
              </Space>
              <div>
                <Button type="primary" htmlType="submit" loading={savingUploadLimits}>
                  저장
                </Button>
              </div>
            </Form>
          </Card>
        </Col>

        <Col xs={24} lg={12}>
          <Card title="리뷰 정책">
            <Form form={reviewForm} layout="vertical" onFinish={saveReviewPolicy}>
              <Space size={12} wrap>
                <Form.Item name="maxImageCount" label="리뷰 최대 이미지 수" rules={[{ required: true }]}>
                  <InputNumber min={0} max={30} />
                </Form.Item>
                <Form.Item name="pointAmount" label="리뷰 적립 포인트" rules={[{ required: true }]}>
                  <InputNumber min={0} max={100000} />
                </Form.Item>
              </Space>
              <div>
                <Button type="primary" htmlType="submit" loading={savingReviewPolicy}>
                  저장
                </Button>
              </div>
            </Form>
          </Card>
        </Col>
      </Row>
    </div>
  );
}
