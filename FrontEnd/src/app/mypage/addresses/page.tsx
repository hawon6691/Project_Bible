'use client';

import { useEffect, useState } from 'react';
import { Card, List, Typography, Button, Modal, Form, Input, Space, Tag, Empty, Spin, Popconfirm, message } from 'antd';
import { PlusOutlined, DeleteOutlined, EditOutlined, EnvironmentOutlined } from '@ant-design/icons';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/stores/authStore';
import { addressApi } from '@/lib/api/endpoints';
import { ROUTES } from '@/lib/utils/constants';
import type { Address } from '@/types/order.types';

const { Title, Text } = Typography;

export default function AddressesPage() {
  const router = useRouter();
  const { isAuthenticated, isLoading: authLoading } = useAuthStore();
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [saving, setSaving] = useState(false);
  const [form] = Form.useForm();

  useEffect(() => {
    if (!authLoading && !isAuthenticated) router.push(ROUTES.LOGIN);
  }, [authLoading, isAuthenticated, router]);

  const fetchAddresses = () => {
    setLoading(true);
    addressApi.getAll()
      .then((res) => setAddresses(res.data.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    if (isAuthenticated) fetchAddresses();
  }, [isAuthenticated]);

  const openModal = (address?: Address) => {
    if (address) {
      setEditingId(address.id);
      form.setFieldsValue(address);
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
        await addressApi.update(editingId, values);
        message.success('배송지가 수정되었습니다.');
      } else {
        await addressApi.create(values);
        message.success('배송지가 추가되었습니다.');
      }
      setModalOpen(false);
      fetchAddresses();
    } catch (err: any) {
      message.error(err?.response?.data?.error?.message || '저장에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await addressApi.delete(id);
      message.success('배송지가 삭제되었습니다.');
      setAddresses(addresses.filter((a) => a.id !== id));
    } catch {
      message.error('삭제에 실패했습니다.');
    }
  };

  const handleSetDefault = async (id: number) => {
    try {
      await addressApi.setDefault(id);
      message.success('기본 배송지로 설정되었습니다.');
      fetchAddresses();
    } catch {
      message.error('설정에 실패했습니다.');
    }
  };

  return (
    <div className="page-container">
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 24 }}>
        <Title level={3} style={{ margin: 0 }}>배송지 관리</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => openModal()}>배송지 추가</Button>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: 60 }}><Spin size="large" /></div>
      ) : addresses.length === 0 ? (
        <Empty description="등록된 배송지가 없습니다">
          <Button type="primary" onClick={() => openModal()}>배송지 추가</Button>
        </Empty>
      ) : (
        <List
          dataSource={addresses}
          renderItem={(addr) => (
            <Card style={{ marginBottom: 12 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <div>
                  <Space>
                    <EnvironmentOutlined />
                    <Text strong>{addr.name}</Text>
                    {addr.isDefault && <Tag color="blue">기본</Tag>}
                  </Space>
                  <div style={{ marginTop: 4 }}>
                    <Text>({addr.zipCode}) {addr.address} {addr.addressDetail}</Text>
                  </div>
                  <div><Text type="secondary">{addr.phone}</Text></div>
                </div>
                <Space>
                  {!addr.isDefault && (
                    <Button size="small" onClick={() => handleSetDefault(addr.id)}>기본 설정</Button>
                  )}
                  <Button size="small" icon={<EditOutlined />} onClick={() => openModal(addr)} />
                  <Popconfirm title="삭제하시겠습니까?" onConfirm={() => handleDelete(addr.id)}>
                    <Button size="small" danger icon={<DeleteOutlined />} />
                  </Popconfirm>
                </Space>
              </div>
            </Card>
          )}
        />
      )}

      <Modal
        title={editingId ? '배송지 수정' : '배송지 추가'}
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={() => form.submit()}
        confirmLoading={saving}
        okText="저장"
        cancelText="취소"
      >
        <Form form={form} layout="vertical" onFinish={handleSave}>
          <Form.Item name="name" label="수령인" rules={[{ required: true, message: '수령인을 입력해주세요' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="phone" label="연락처" rules={[{ required: true, message: '연락처를 입력해주세요' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="zipCode" label="우편번호" rules={[{ required: true, message: '우편번호를 입력해주세요' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="address" label="주소" rules={[{ required: true, message: '주소를 입력해주세요' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="addressDetail" label="상세주소">
            <Input />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
