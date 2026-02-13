'use client';

import { useEffect, useState } from 'react';
import { Card, Table, Typography, Button, Modal, Form, Input, InputNumber, Space, Pagination, Spin, Empty, Popconfirm, Select, message } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/stores/authStore';
import { sellerApi, productApi } from '@/lib/api/endpoints';
import { formatPrice, formatDateTime } from '@/lib/utils/format';
import { ROUTES, PAGE_SIZE } from '@/lib/utils/constants';
import type { PaginationMeta } from '@/types/common.types';
import type { ProductSummary } from '@/types/product.types';

const { Title } = Typography;

interface SellerPrice {
  id: number;
  productId: number;
  productName: string;
  price: number;
  url: string;
  shipping: string;
  createdAt: string;
}

export default function SellerProductsPage() {
  const router = useRouter();
  const { isAuthenticated, isLoading: authLoading } = useAuthStore();
  const [prices, setPrices] = useState<SellerPrice[]>([]);
  const [meta, setMeta] = useState<PaginationMeta | null>(null);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [saving, setSaving] = useState(false);
  const [productSearch, setProductSearch] = useState<ProductSummary[]>([]);
  const [form] = Form.useForm();

  useEffect(() => {
    if (!authLoading && !isAuthenticated) router.push(ROUTES.LOGIN);
  }, [authLoading, isAuthenticated, router]);

  const fetchPrices = () => {
    setLoading(true);
    sellerApi.getMyPrices({ page, limit: PAGE_SIZE })
      .then((res) => { setPrices(res.data.data); setMeta(res.data.meta || null); })
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    if (isAuthenticated) fetchPrices();
  }, [isAuthenticated, page]);

  const handleProductSearch = async (value: string) => {
    if (!value.trim()) return;
    try {
      const { data } = await productApi.getAll({ search: value, limit: 10 });
      setProductSearch(data.data);
    } catch {}
  };

  const openModal = (price?: SellerPrice) => {
    if (price) {
      setEditingId(price.id);
      form.setFieldsValue({ price: price.price, url: price.url, shipping: price.shipping });
    } else {
      setEditingId(null);
      form.resetFields();
    }
    setModalOpen(true);
  };

  const handleSave = async (values: any) => {
    setSaving(true);
    try {
      if (editingId) {
        await sellerApi.updatePrice(editingId, { price: values.price, url: values.url, shipping: values.shipping });
        message.success('가격이 수정되었습니다.');
      } else {
        await sellerApi.setPrice({ productId: values.productId, price: values.price, url: values.url, shipping: values.shipping });
        message.success('가격이 등록되었습니다.');
      }
      setModalOpen(false);
      fetchPrices();
    } catch (err: any) {
      message.error(err?.response?.data?.error?.message || '저장에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await sellerApi.deletePrice(id);
      message.success('삭제되었습니다.');
      fetchPrices();
    } catch {
      message.error('삭제에 실패했습니다.');
    }
  };

  const columns = [
    { title: '상품명', dataIndex: 'productName', key: 'productName' },
    { title: '가격', dataIndex: 'price', key: 'price', render: (v: number) => formatPrice(v) },
    { title: '배송', dataIndex: 'shipping', key: 'shipping' },
    { title: '등록일', dataIndex: 'createdAt', key: 'createdAt', render: (d: string) => formatDateTime(d) },
    {
      title: '',
      key: 'action',
      render: (_: any, record: SellerPrice) => (
        <Space>
          <Button size="small" icon={<EditOutlined />} onClick={() => openModal(record)} />
          <Popconfirm title="삭제하시겠습니까?" onConfirm={() => handleDelete(record.id)}>
            <Button size="small" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div className="page-container">
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 24 }}>
        <Title level={3} style={{ margin: 0 }}>가격 관리</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => openModal()}>가격 등록</Button>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: 60 }}><Spin size="large" /></div>
      ) : prices.length === 0 ? (
        <Empty description="등록된 가격이 없습니다">
          <Button type="primary" onClick={() => openModal()}>가격 등록</Button>
        </Empty>
      ) : (
        <>
          <Table dataSource={prices} columns={columns} rowKey="id" pagination={false} />
          {meta && (
            <div style={{ textAlign: 'center', marginTop: 24 }}>
              <Pagination current={page} total={meta.totalCount} pageSize={PAGE_SIZE} onChange={setPage} showSizeChanger={false} />
            </div>
          )}
        </>
      )}

      <Modal
        title={editingId ? '가격 수정' : '가격 등록'}
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={() => form.submit()}
        confirmLoading={saving}
        okText="저장"
        cancelText="취소"
      >
        <Form form={form} layout="vertical" onFinish={handleSave}>
          {!editingId && (
            <Form.Item name="productId" label="상품 선택" rules={[{ required: true, message: '상품을 선택해주세요' }]}>
              <Select
                showSearch
                placeholder="상품명 검색"
                filterOption={false}
                onSearch={handleProductSearch}
                options={productSearch.map((p) => ({ label: `${p.name} (${formatPrice(p.lowestPrice)})`, value: p.id }))}
              />
            </Form.Item>
          )}
          <Form.Item name="price" label="판매 가격 (원)" rules={[{ required: true, message: '가격을 입력해주세요' }]}>
            <InputNumber style={{ width: '100%' }} min={1} formatter={(v) => `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')} />
          </Form.Item>
          <Form.Item name="url" label="구매 링크" rules={[{ required: true, message: '구매 링크를 입력해주세요' }]}>
            <Input placeholder="https://..." />
          </Form.Item>
          <Form.Item name="shipping" label="배송 정보">
            <Input placeholder="예: 무료배송, 3,000원" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
