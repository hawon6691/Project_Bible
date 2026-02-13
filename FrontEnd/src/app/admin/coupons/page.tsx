'use client';

import { useEffect, useState, useCallback } from 'react';
import { Card, Table, Typography, Tag, Button, Modal, Form, Input, InputNumber, Select, DatePicker, Pagination, Popconfirm, message, Space } from 'antd';
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons';
import { adminApi } from '@/lib/api/endpoints';
import { formatPrice, formatDate } from '@/lib/utils/format';
import { PAGE_SIZE } from '@/lib/utils/constants';
import type { Coupon } from '@/types/order.types';
import type { PaginationMeta } from '@/types/common.types';

const { Title } = Typography;

export default function AdminCouponsPage() {
  const [coupons, setCoupons] = useState<Coupon[]>([]);
  const [meta, setMeta] = useState<PaginationMeta | null>(null);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [modalOpen, setModalOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form] = Form.useForm();

  const fetchCoupons = useCallback(() => {
    setLoading(true);
    adminApi.getCoupons({ page, limit: PAGE_SIZE })
      .then((res) => { setCoupons(res.data.data); setMeta(res.data.meta || null); })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [page]);

  useEffect(() => { fetchCoupons(); }, [fetchCoupons]);

  const handleCreate = async (values: any) => {
    setSaving(true);
    try {
      await adminApi.createCoupon({ ...values, expiresAt: values.expiresAt?.toISOString() });
      message.success('쿠폰이 생성되었습니다.');
      setModalOpen(false);
      form.resetFields();
      fetchCoupons();
    } catch (err: any) {
      message.error(err?.response?.data?.error?.message || '생성에 실패했습니다.');
    } finally { setSaving(false); }
  };

  const handleDelete = async (id: number) => {
    try { await adminApi.deleteCoupon(id); message.success('삭제되었습니다.'); fetchCoupons(); }
    catch { message.error('삭제에 실패했습니다.'); }
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
    { title: '코드', dataIndex: 'code', key: 'code' },
    { title: '쿠폰명', dataIndex: 'name', key: 'name' },
    {
      title: '할인', key: 'discount',
      render: (_: any, r: Coupon) => r.discountType === 'PERCENT' ? `${r.discountValue}%` : formatPrice(r.discountValue),
    },
    { title: '최소주문', dataIndex: 'minOrderAmount', key: 'minOrderAmount', render: (v: number) => formatPrice(v) },
    { title: '만료일', dataIndex: 'expiresAt', key: 'expiresAt', render: (d: string) => formatDate(d) },
    {
      title: '', key: 'action',
      render: (_: any, r: Coupon) => (
        <Popconfirm title="삭제하시겠습니까?" onConfirm={() => handleDelete(r.id)}>
          <Button size="small" danger icon={<DeleteOutlined />} />
        </Popconfirm>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={3} style={{ margin: 0 }}>쿠폰 관리</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setModalOpen(true); }}>쿠폰 생성</Button>
      </div>
      <Card>
        <Table dataSource={coupons} columns={columns} rowKey="id" loading={loading} pagination={false} size="middle" />
        {meta && <div style={{ textAlign: 'center', marginTop: 16 }}><Pagination current={page} total={meta.totalCount} pageSize={PAGE_SIZE} onChange={setPage} showSizeChanger={false} /></div>}
      </Card>

      <Modal title="쿠폰 생성" open={modalOpen} onCancel={() => setModalOpen(false)} onOk={() => form.submit()} confirmLoading={saving} okText="생성">
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item name="code" label="쿠폰 코드" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="name" label="쿠폰명" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="discountType" label="할인 유형" rules={[{ required: true }]}>
            <Select options={[{ label: '정률 (%)', value: 'PERCENT' }, { label: '정액 (원)', value: 'FIXED' }]} />
          </Form.Item>
          <Form.Item name="discountValue" label="할인 값" rules={[{ required: true }]}><InputNumber style={{ width: '100%' }} min={1} /></Form.Item>
          <Form.Item name="minOrderAmount" label="최소 주문금액" rules={[{ required: true }]}><InputNumber style={{ width: '100%' }} min={0} /></Form.Item>
          <Form.Item name="maxDiscountAmount" label="최대 할인금액"><InputNumber style={{ width: '100%' }} min={0} /></Form.Item>
          <Form.Item name="expiresAt" label="만료일" rules={[{ required: true }]}><DatePicker style={{ width: '100%' }} /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
