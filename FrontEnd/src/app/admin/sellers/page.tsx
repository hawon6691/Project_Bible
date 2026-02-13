'use client';

import { useEffect, useState, useCallback } from 'react';
import { Card, Table, Typography, Tag, Button, Space, Pagination, Popconfirm, Modal, Input, message } from 'antd';
import { CheckOutlined, CloseOutlined } from '@ant-design/icons';
import { adminApi } from '@/lib/api/endpoints';
import { formatDateTime } from '@/lib/utils/format';
import { PAGE_SIZE } from '@/lib/utils/constants';
import type { PaginationMeta } from '@/types/common.types';

const { Title, Text } = Typography;
const { TextArea } = Input;

interface Seller {
  id: number;
  companyName: string;
  email: string;
  trustScore: number;
  status: string;
  createdAt: string;
}

const STATUS_MAP: Record<string, { color: string; label: string }> = {
  PENDING: { color: 'orange', label: '대기' },
  APPROVED: { color: 'green', label: '승인' },
  REJECTED: { color: 'red', label: '거절' },
};

export default function AdminSellersPage() {
  const [sellers, setSellers] = useState<Seller[]>([]);
  const [meta, setMeta] = useState<PaginationMeta | null>(null);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState<string | undefined>();
  const [page, setPage] = useState(1);
  const [rejectModal, setRejectModal] = useState<{ open: boolean; id: number | null }>({ open: false, id: null });
  const [rejectReason, setRejectReason] = useState('');

  const fetchSellers = useCallback(() => {
    setLoading(true);
    adminApi.getSellers({ page, limit: PAGE_SIZE, status: statusFilter })
      .then((res) => { setSellers(res.data.data); setMeta(res.data.meta || null); })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [page, statusFilter]);

  useEffect(() => { fetchSellers(); }, [fetchSellers]);

  const handleApprove = async (id: number) => {
    try { await adminApi.approveSeller(id); message.success('승인되었습니다.'); fetchSellers(); }
    catch { message.error('실패했습니다.'); }
  };

  const handleReject = async () => {
    if (!rejectModal.id) return;
    try { await adminApi.rejectSeller(rejectModal.id, rejectReason); message.success('거절되었습니다.'); setRejectModal({ open: false, id: null }); fetchSellers(); }
    catch { message.error('실패했습니다.'); }
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
    { title: '상호명', dataIndex: 'companyName', key: 'companyName' },
    { title: '이메일', dataIndex: 'email', key: 'email' },
    { title: '신뢰도', dataIndex: 'trustScore', key: 'trustScore' },
    {
      title: '상태', dataIndex: 'status', key: 'status',
      render: (s: string) => <Tag color={STATUS_MAP[s]?.color}>{STATUS_MAP[s]?.label || s}</Tag>,
    },
    { title: '신청일', dataIndex: 'createdAt', key: 'createdAt', render: (d: string) => formatDateTime(d) },
    {
      title: '', key: 'action',
      render: (_: any, r: Seller) => r.status === 'PENDING' ? (
        <Space>
          <Popconfirm title="승인하시겠습니까?" onConfirm={() => handleApprove(r.id)}>
            <Button size="small" type="primary" icon={<CheckOutlined />}>승인</Button>
          </Popconfirm>
          <Button size="small" danger icon={<CloseOutlined />} onClick={() => setRejectModal({ open: true, id: r.id })}>거절</Button>
        </Space>
      ) : null,
    },
  ];

  return (
    <div>
      <Title level={3}>판매자 관리</Title>
      <Card>
        <Table dataSource={sellers} columns={columns} rowKey="id" loading={loading} pagination={false} size="middle" />
        {meta && <div style={{ textAlign: 'center', marginTop: 16 }}><Pagination current={page} total={meta.totalCount} pageSize={PAGE_SIZE} onChange={setPage} showSizeChanger={false} /></div>}
      </Card>

      <Modal title="거절 사유" open={rejectModal.open} onCancel={() => setRejectModal({ open: false, id: null })} onOk={handleReject} okText="거절" okButtonProps={{ danger: true }}>
        <TextArea rows={3} value={rejectReason} onChange={(e) => setRejectReason(e.target.value)} placeholder="거절 사유를 입력해주세요" />
      </Modal>
    </div>
  );
}
