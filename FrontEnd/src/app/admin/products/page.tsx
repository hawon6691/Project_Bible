'use client';

import { useEffect, useState, useCallback } from 'react';
import { Card, Table, Typography, Button, Space, Input, Pagination, Spin, Tag, Popconfirm, Modal, Form, Select, message } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined } from '@ant-design/icons';
import { productApi, categoryApi } from '@/lib/api/endpoints';
import { formatPrice, formatDateTime } from '@/lib/utils/format';
import { PAGE_SIZE } from '@/lib/utils/constants';
import type { ProductSummary, Category } from '@/types/product.types';
import type { PaginationMeta } from '@/types/common.types';

const { Title } = Typography;
const { TextArea } = Input;

export default function AdminProductsPage() {
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [meta, setMeta] = useState<PaginationMeta | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(1);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [saving, setSaving] = useState(false);
  const [form] = Form.useForm();

  useEffect(() => {
    categoryApi.getAll().then((res) => setCategories(res.data.data)).catch(() => {});
  }, []);

  const fetchProducts = useCallback(() => {
    setLoading(true);
    productApi.getAll({ page, limit: PAGE_SIZE, search: search || undefined })
      .then((res) => { setProducts(res.data.data); setMeta(res.data.meta || null); })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [page, search]);

  useEffect(() => { fetchProducts(); }, [fetchProducts]);

  const flatCats = (cats: Category[], prefix = ''): { id: number; name: string }[] =>
    cats.flatMap((c) => [{ id: c.id, name: prefix + c.name }, ...flatCats(c.children || [], prefix + c.name + ' > ')]);

  const openCreate = () => { setEditingId(null); form.resetFields(); setModalOpen(true); };
  const openEdit = async (id: number) => {
    try {
      const { data } = await productApi.getOne(id);
      setEditingId(id);
      form.setFieldsValue({ name: data.data.name, description: data.data.description, categoryId: data.data.category?.id });
      setModalOpen(true);
    } catch {}
  };

  const handleSave = async (values: any) => {
    setSaving(true);
    try {
      if (editingId) {
        await productApi.update(editingId, values);
        message.success('상품이 수정되었습니다.');
      } else {
        await productApi.create(values);
        message.success('상품이 등록되었습니다.');
      }
      setModalOpen(false);
      fetchProducts();
    } catch (err: any) {
      message.error(err?.response?.data?.error?.message || '저장에 실패했습니다.');
    } finally { setSaving(false); }
  };

  const handleDelete = async (id: number) => {
    try {
      await productApi.delete(id);
      message.success('삭제되었습니다.');
      fetchProducts();
    } catch { message.error('삭제에 실패했습니다.'); }
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
    { title: '상품명', dataIndex: 'name', key: 'name' },
    { title: '최저가', dataIndex: 'lowestPrice', key: 'lowestPrice', render: (v: number) => formatPrice(v) },
    { title: '판매처', dataIndex: 'sellerCount', key: 'sellerCount' },
    { title: '리뷰', dataIndex: 'reviewCount', key: 'reviewCount' },
    { title: '평점', dataIndex: 'averageRating', key: 'averageRating', render: (v: number) => v?.toFixed(1) || '-' },
    {
      title: '',
      key: 'action',
      render: (_: any, r: ProductSummary) => (
        <Space>
          <Button size="small" icon={<EditOutlined />} onClick={() => openEdit(r.id)} />
          <Popconfirm title="삭제하시겠습니까?" onConfirm={() => handleDelete(r.id)}>
            <Button size="small" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={3} style={{ margin: 0 }}>상품 관리</Title>
        <Space>
          <Input.Search placeholder="상품 검색" onSearch={(v) => { setSearch(v); setPage(1); }} style={{ width: 250 }} />
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>상품 등록</Button>
        </Space>
      </div>

      <Card>
        <Table dataSource={products} columns={columns} rowKey="id" loading={loading} pagination={false} size="middle" />
        {meta && (
          <div style={{ textAlign: 'center', marginTop: 16 }}>
            <Pagination current={page} total={meta.totalCount} pageSize={PAGE_SIZE} onChange={setPage} showSizeChanger={false} />
          </div>
        )}
      </Card>

      <Modal title={editingId ? '상품 수정' : '상품 등록'} open={modalOpen} onCancel={() => setModalOpen(false)} onOk={() => form.submit()} confirmLoading={saving} okText="저장" width={600}>
        <Form form={form} layout="vertical" onFinish={handleSave}>
          <Form.Item name="name" label="상품명" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="categoryId" label="카테고리" rules={[{ required: true }]}>
            <Select options={flatCats(categories).map((c) => ({ label: c.name, value: c.id }))} placeholder="카테고리 선택" />
          </Form.Item>
          <Form.Item name="description" label="설명"><TextArea rows={4} /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
