'use client';

import { useCallback, useEffect, useState } from 'react';
import {
  Button,
  Card,
  Form,
  Input,
  Modal,
  Pagination,
  Popconfirm,
  Space,
  Switch,
  Table,
  Tag,
  Typography,
  message,
} from 'antd';
import { DeleteOutlined, EditOutlined, PlusOutlined } from '@ant-design/icons';
import { faqAdminApi } from '@/lib/api/endpoints';
import { PAGE_SIZE } from '@/lib/utils/constants';
import { formatDateTime } from '@/lib/utils/format';
import type { PaginationMeta } from '@/types/common.types';

const { Title } = Typography;
const { TextArea, Search } = Input;

type FaqItem = {
  id: number;
  category: string;
  question: string;
  answer: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
};

export default function AdminFaqsPage() {
  const [faqs, setFaqs] = useState<FaqItem[]>([]);
  const [meta, setMeta] = useState<PaginationMeta | null>(null);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [keyword, setKeyword] = useState('');
  const [category, setCategory] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [saving, setSaving] = useState(false);
  const [form] = Form.useForm();

  const fetchFaqs = useCallback(() => {
    setLoading(true);
    faqAdminApi
      .getFaqs({
        page,
        limit: PAGE_SIZE,
        keyword: keyword || undefined,
        category: category || undefined,
      })
      .then((res) => {
        setFaqs(res.data.data || []);
        setMeta(res.data.meta || null);
      })
      .catch(() => message.error('FAQ 목록 조회에 실패했습니다.'))
      .finally(() => setLoading(false));
  }, [page, keyword, category]);

  useEffect(() => {
    fetchFaqs();
  }, [fetchFaqs]);

  const openCreate = () => {
    setEditingId(null);
    form.resetFields();
    form.setFieldsValue({ isActive: true });
    setModalOpen(true);
  };

  const openEdit = (item: FaqItem) => {
    setEditingId(item.id);
    form.setFieldsValue({
      category: item.category,
      question: item.question,
      answer: item.answer,
      isActive: item.isActive,
    });
    setModalOpen(true);
  };

  const onSave = async (values: {
    category: string;
    question: string;
    answer: string;
    isActive: boolean;
  }) => {
    setSaving(true);
    try {
      if (editingId) {
        await faqAdminApi.updateFaq(editingId, values);
        message.success('FAQ가 수정되었습니다.');
      } else {
        await faqAdminApi.createFaq(values);
        message.success('FAQ가 등록되었습니다.');
      }
      setModalOpen(false);
      fetchFaqs();
    } catch (err: any) {
      message.error(err?.response?.data?.error?.message || 'FAQ 저장에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  };

  const onDelete = async (id: number) => {
    try {
      await faqAdminApi.deleteFaq(id);
      message.success('FAQ가 삭제되었습니다.');
      fetchFaqs();
    } catch {
      message.error('FAQ 삭제에 실패했습니다.');
    }
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 64 },
    { title: '카테고리', dataIndex: 'category', key: 'category', width: 120 },
    { title: '질문', dataIndex: 'question', key: 'question' },
    {
      title: '상태',
      dataIndex: 'isActive',
      key: 'isActive',
      width: 100,
      render: (active: boolean) => <Tag color={active ? 'green' : 'default'}>{active ? '활성' : '비활성'}</Tag>,
    },
    {
      title: '등록일',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 140,
      render: (value: string) => formatDateTime(value),
    },
    {
      title: '',
      key: 'action',
      width: 100,
      render: (_: unknown, record: FaqItem) => (
        <Space>
          <Button size="small" icon={<EditOutlined />} onClick={() => openEdit(record)} />
          <Popconfirm title="FAQ를 삭제하시겠습니까?" onConfirm={() => onDelete(record.id)}>
            <Button size="small" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Title level={3}>FAQ 관리</Title>
      <Card>
        <Space style={{ marginBottom: 16 }}>
          <Search
            placeholder="질문/답변 검색"
            allowClear
            onSearch={(v) => {
              setKeyword(v);
              setPage(1);
            }}
            style={{ width: 260 }}
          />
          <Input
            placeholder="카테고리"
            allowClear
            value={category}
            onChange={(e) => {
              setCategory(e.target.value);
              setPage(1);
            }}
            style={{ width: 180 }}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
            FAQ 등록
          </Button>
        </Space>

        <Table rowKey="id" dataSource={faqs} columns={columns} loading={loading} pagination={false} />
        {meta && (
          <div style={{ textAlign: 'center', marginTop: 16 }}>
            <Pagination
              current={page}
              total={meta.totalCount}
              pageSize={PAGE_SIZE}
              onChange={setPage}
              showSizeChanger={false}
            />
          </div>
        )}
      </Card>

      <Modal
        title={editingId ? 'FAQ 수정' : 'FAQ 등록'}
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={() => form.submit()}
        confirmLoading={saving}
        okText="저장"
      >
        <Form form={form} layout="vertical" onFinish={onSave}>
          <Form.Item name="category" label="카테고리" rules={[{ required: true, message: '카테고리를 입력하세요.' }]}>
            <Input maxLength={50} />
          </Form.Item>
          <Form.Item name="question" label="질문" rules={[{ required: true, message: '질문을 입력하세요.' }]}>
            <Input maxLength={200} />
          </Form.Item>
          <Form.Item name="answer" label="답변" rules={[{ required: true, message: '답변을 입력하세요.' }]}>
            <TextArea rows={5} maxLength={5000} />
          </Form.Item>
          <Form.Item name="isActive" label="활성 여부" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
