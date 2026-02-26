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
import { noticeAdminApi } from '@/lib/api/endpoints';
import { PAGE_SIZE } from '@/lib/utils/constants';
import { formatDateTime } from '@/lib/utils/format';
import type { PaginationMeta } from '@/types/common.types';

const { Title } = Typography;
const { TextArea, Search } = Input;

type NoticeItem = {
  id: number;
  title: string;
  content: string;
  isPublished: boolean;
  createdAt: string;
  updatedAt: string;
};

export default function AdminNoticesPage() {
  const [notices, setNotices] = useState<NoticeItem[]>([]);
  const [meta, setMeta] = useState<PaginationMeta | null>(null);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [saving, setSaving] = useState(false);
  const [form] = Form.useForm();

  const fetchNotices = useCallback(() => {
    setLoading(true);
    noticeAdminApi
      .getNotices({ page, limit: PAGE_SIZE })
      .then((res) => {
        const items = (res.data.data || []) as NoticeItem[];
        const filtered = search
          ? items.filter(
              (item) =>
                item.title.toLowerCase().includes(search.toLowerCase()) ||
                item.content.toLowerCase().includes(search.toLowerCase()),
            )
          : items;
        setNotices(filtered);
        setMeta(res.data.meta || null);
      })
      .catch(() => message.error('공지사항 목록 조회에 실패했습니다.'))
      .finally(() => setLoading(false));
  }, [page, search]);

  useEffect(() => {
    fetchNotices();
  }, [fetchNotices]);

  const openCreate = () => {
    setEditingId(null);
    form.resetFields();
    form.setFieldsValue({ isPublished: true });
    setModalOpen(true);
  };

  const openEdit = (item: NoticeItem) => {
    setEditingId(item.id);
    form.setFieldsValue({
      title: item.title,
      content: item.content,
      isPublished: item.isPublished,
    });
    setModalOpen(true);
  };

  const onSave = async (values: { title: string; content: string; isPublished: boolean }) => {
    setSaving(true);
    try {
      if (editingId) {
        await noticeAdminApi.updateNotice(editingId, values);
        message.success('공지사항이 수정되었습니다.');
      } else {
        await noticeAdminApi.createNotice(values);
        message.success('공지사항이 등록되었습니다.');
      }
      setModalOpen(false);
      fetchNotices();
    } catch (err: any) {
      message.error(err?.response?.data?.error?.message || '공지사항 저장에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  };

  const onDelete = async (id: number) => {
    try {
      await noticeAdminApi.deleteNotice(id);
      message.success('공지사항이 삭제되었습니다.');
      fetchNotices();
    } catch {
      message.error('공지사항 삭제에 실패했습니다.');
    }
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 64 },
    { title: '제목', dataIndex: 'title', key: 'title' },
    {
      title: '게시 상태',
      dataIndex: 'isPublished',
      key: 'isPublished',
      width: 100,
      render: (published: boolean) => (
        <Tag color={published ? 'blue' : 'default'}>{published ? '게시' : '비공개'}</Tag>
      ),
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
      render: (_: unknown, record: NoticeItem) => (
        <Space>
          <Button size="small" icon={<EditOutlined />} onClick={() => openEdit(record)} />
          <Popconfirm title="공지사항을 삭제하시겠습니까?" onConfirm={() => onDelete(record.id)}>
            <Button size="small" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Title level={3}>공지사항 관리</Title>
      <Card>
        <Space style={{ marginBottom: 16 }}>
          <Search
            placeholder="제목/내용 검색"
            allowClear
            onSearch={(v) => {
              setSearch(v);
              setPage(1);
            }}
            style={{ width: 280 }}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
            공지 등록
          </Button>
        </Space>

        <Table rowKey="id" dataSource={notices} columns={columns} loading={loading} pagination={false} />
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
        title={editingId ? '공지사항 수정' : '공지사항 등록'}
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={() => form.submit()}
        confirmLoading={saving}
        okText="저장"
      >
        <Form form={form} layout="vertical" onFinish={onSave}>
          <Form.Item name="title" label="제목" rules={[{ required: true, message: '제목을 입력하세요.' }]}>
            <Input maxLength={200} />
          </Form.Item>
          <Form.Item name="content" label="내용" rules={[{ required: true, message: '내용을 입력하세요.' }]}>
            <TextArea rows={7} maxLength={10000} />
          </Form.Item>
          <Form.Item name="isPublished" label="게시 여부" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
