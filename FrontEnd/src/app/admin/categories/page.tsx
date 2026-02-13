'use client';

import { useEffect, useState } from 'react';
import { Card, Tree, Typography, Button, Modal, Form, Input, InputNumber, Select, Space, Spin, message, Popconfirm } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { categoryApi } from '@/lib/api/endpoints';
import type { Category } from '@/types/product.types';
import type { DataNode } from 'antd/es/tree';

const { Title } = Typography;

export default function AdminCategoriesPage() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [saving, setSaving] = useState(false);
  const [form] = Form.useForm();

  const fetchCategories = () => {
    setLoading(true);
    categoryApi.getAll()
      .then((res) => setCategories(res.data.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchCategories(); }, []);

  const flatCats = (cats: Category[], prefix = ''): { id: number; name: string }[] =>
    cats.flatMap((c) => [{ id: c.id, name: prefix + c.name }, ...flatCats(c.children || [], prefix + c.name + ' > ')]);

  const toTreeData = (cats: Category[]): DataNode[] =>
    cats.map((cat) => ({
      key: cat.id,
      title: (
        <Space>
          <span>{cat.name}</span>
          <Button size="small" type="text" icon={<EditOutlined />} onClick={(e) => { e.stopPropagation(); openEdit(cat); }} />
          <Popconfirm title="삭제하시겠습니까?" onConfirm={(e) => { e?.stopPropagation(); handleDelete(cat.id); }}>
            <Button size="small" type="text" danger icon={<DeleteOutlined />} onClick={(e) => e.stopPropagation()} />
          </Popconfirm>
        </Space>
      ),
      children: cat.children?.length ? toTreeData(cat.children) : undefined,
    }));

  const openCreate = () => { setEditingId(null); form.resetFields(); setModalOpen(true); };
  const openEdit = (cat: Category) => {
    setEditingId(cat.id);
    form.setFieldsValue({ name: cat.name, sortOrder: cat.sortOrder });
    setModalOpen(true);
  };

  const handleSave = async (values: any) => {
    setSaving(true);
    try {
      if (editingId) {
        await categoryApi.update(editingId, values);
        message.success('카테고리가 수정되었습니다.');
      } else {
        await categoryApi.create(values);
        message.success('카테고리가 생성되었습니다.');
      }
      setModalOpen(false);
      fetchCategories();
    } catch (err: any) {
      message.error(err?.response?.data?.error?.message || '저장에 실패했습니다.');
    } finally { setSaving(false); }
  };

  const handleDelete = async (id: number) => {
    try {
      await categoryApi.delete(id);
      message.success('삭제되었습니다.');
      fetchCategories();
    } catch (err: any) {
      message.error(err?.response?.data?.error?.message || '삭제에 실패했습니다.');
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={3} style={{ margin: 0 }}>카테고리 관리</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>카테고리 추가</Button>
      </div>

      <Card>
        {loading ? (
          <div style={{ textAlign: 'center', padding: 40 }}><Spin /></div>
        ) : (
          <Tree treeData={toTreeData(categories)} defaultExpandAll blockNode />
        )}
      </Card>

      <Modal title={editingId ? '카테고리 수정' : '카테고리 추가'} open={modalOpen} onCancel={() => setModalOpen(false)} onOk={() => form.submit()} confirmLoading={saving} okText="저장">
        <Form form={form} layout="vertical" onFinish={handleSave}>
          <Form.Item name="name" label="카테고리명" rules={[{ required: true }]}><Input /></Form.Item>
          {!editingId && (
            <Form.Item name="parentId" label="상위 카테고리">
              <Select allowClear placeholder="최상위" options={flatCats(categories).map((c) => ({ label: c.name, value: c.id }))} />
            </Form.Item>
          )}
          <Form.Item name="sortOrder" label="정렬 순서"><InputNumber min={0} /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
