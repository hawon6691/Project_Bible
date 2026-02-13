'use client';

import { useEffect, useState } from 'react';
import { Card, Table, Typography, Button, Modal, Form, Input, Select, InputNumber, Switch, Space, Popconfirm, message, Spin } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { specApi, categoryApi } from '@/lib/api/endpoints';
import type { SpecDefinition, Category } from '@/types/product.types';

const { Title } = Typography;

export default function AdminSpecsPage() {
  const [specs, setSpecs] = useState<SpecDefinition[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedCategory, setSelectedCategory] = useState<number | undefined>();
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [saving, setSaving] = useState(false);
  const [form] = Form.useForm();

  useEffect(() => {
    categoryApi.getAll().then((res) => setCategories(res.data.data)).catch(() => {});
  }, []);

  const fetchSpecs = () => {
    setLoading(true);
    specApi.getDefinitions(selectedCategory)
      .then((res) => setSpecs(res.data.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchSpecs(); }, [selectedCategory]);

  const flatCats = (cats: Category[], prefix = ''): { id: number; name: string }[] =>
    cats.flatMap((c) => [{ id: c.id, name: prefix + c.name }, ...flatCats(c.children || [], prefix + c.name + ' > ')]);

  const openCreate = () => { setEditingId(null); form.resetFields(); setModalOpen(true); };
  const openEdit = (spec: SpecDefinition) => {
    setEditingId(spec.id);
    form.setFieldsValue({ name: spec.name, type: spec.type, unit: spec.unit, options: spec.options?.join(', ') });
    setModalOpen(true);
  };

  const handleSave = async (values: any) => {
    const data = {
      ...values,
      categoryId: selectedCategory,
      options: values.options ? values.options.split(',').map((s: string) => s.trim()).filter(Boolean) : undefined,
    };
    setSaving(true);
    try {
      if (editingId) {
        await specApi.updateDefinition(editingId, data);
        message.success('수정되었습니다.');
      } else {
        await specApi.createDefinition(data);
        message.success('생성되었습니다.');
      }
      setModalOpen(false);
      fetchSpecs();
    } catch (err: any) {
      message.error(err?.response?.data?.error?.message || '저장에 실패했습니다.');
    } finally { setSaving(false); }
  };

  const handleDelete = async (id: number) => {
    try { await specApi.deleteDefinition(id); message.success('삭제되었습니다.'); fetchSpecs(); }
    catch { message.error('삭제에 실패했습니다.'); }
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
    { title: '스펙명', dataIndex: 'name', key: 'name' },
    { title: '유형', dataIndex: 'type', key: 'type' },
    { title: '단위', dataIndex: 'unit', key: 'unit', render: (v: string) => v || '-' },
    { title: '옵션', dataIndex: 'options', key: 'options', render: (v: string[]) => v?.join(', ') || '-' },
    {
      title: '', key: 'action',
      render: (_: any, r: SpecDefinition) => (
        <Space>
          <Button size="small" icon={<EditOutlined />} onClick={() => openEdit(r)} />
          <Popconfirm title="삭제?" onConfirm={() => handleDelete(r.id)}><Button size="small" danger icon={<DeleteOutlined />} /></Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={3} style={{ margin: 0 }}>스펙 관리</Title>
        <Space>
          <Select allowClear placeholder="카테고리 선택" value={selectedCategory} onChange={setSelectedCategory} style={{ width: 200 }}
            options={flatCats(categories).map((c) => ({ label: c.name, value: c.id }))} />
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate} disabled={!selectedCategory}>스펙 추가</Button>
        </Space>
      </div>
      <Card>
        <Table dataSource={specs} columns={columns} rowKey="id" loading={loading} pagination={false} size="middle" />
      </Card>

      <Modal title={editingId ? '스펙 수정' : '스펙 추가'} open={modalOpen} onCancel={() => setModalOpen(false)} onOk={() => form.submit()} confirmLoading={saving} okText="저장">
        <Form form={form} layout="vertical" onFinish={handleSave}>
          <Form.Item name="name" label="스펙명" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="type" label="입력 유형" rules={[{ required: true }]}>
            <Select options={[{ label: '텍스트', value: 'TEXT' }, { label: '숫자', value: 'NUMBER' }, { label: '선택', value: 'SELECT' }]} />
          </Form.Item>
          <Form.Item name="unit" label="단위"><Input placeholder="예: GB, kg, GHz" /></Form.Item>
          <Form.Item name="options" label="옵션 (콤마 구분)"><Input placeholder="i5, i7, i9" /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
