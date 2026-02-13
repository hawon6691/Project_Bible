'use client';

import { useState } from 'react';
import { Form, Input, Rate, Button, Upload, message } from 'antd';
import { UploadOutlined } from '@ant-design/icons';
import { reviewApi } from '@/lib/api/endpoints';

const { TextArea } = Input;

interface Props {
  productId: number;
  onSuccess?: () => void;
}

export default function ReviewForm({ productId, onSuccess }: Props) {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);

  const onFinish = async (values: { rating: number; title: string; content: string }) => {
    setLoading(true);
    try {
      const formData = new FormData();
      formData.append('rating', String(values.rating));
      formData.append('title', values.title);
      formData.append('content', values.content);

      await reviewApi.create(productId, formData);
      message.success('리뷰가 등록되었습니다.');
      form.resetFields();
      onSuccess?.();
    } catch (err: any) {
      message.error(err?.response?.data?.error?.message || '리뷰 등록에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Form form={form} layout="vertical" onFinish={onFinish}>
      <Form.Item name="rating" label="평점" rules={[{ required: true, message: '평점을 선택해주세요' }]}>
        <Rate />
      </Form.Item>
      <Form.Item name="title" label="제목" rules={[{ required: true, message: '제목을 입력해주세요' }]}>
        <Input placeholder="리뷰 제목" />
      </Form.Item>
      <Form.Item name="content" label="내용" rules={[{ required: true, message: '내용을 입력해주세요' }]}>
        <TextArea rows={4} placeholder="리뷰를 작성해주세요" />
      </Form.Item>
      <Form.Item>
        <Button type="primary" htmlType="submit" loading={loading}>리뷰 등록</Button>
      </Form.Item>
    </Form>
  );
}
