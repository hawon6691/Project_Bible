'use client';

import { useEffect, useState, useCallback } from 'react';
import { Card, Table, Typography, Tag, Select, Input, Pagination, Space, Button, Popconfirm, message } from 'antd';
import { userApi } from '@/lib/api/endpoints';
import { formatDateTime } from '@/lib/utils/format';
import { PAGE_SIZE } from '@/lib/utils/constants';
import type { User, UserStatus, UserRole } from '@/types/user.types';
import type { PaginationMeta } from '@/types/common.types';

const { Title } = Typography;

const STATUS_COLORS: Record<string, string> = { ACTIVE: 'green', INACTIVE: 'default', BLOCKED: 'red' };
const STATUS_LABELS: Record<string, string> = { ACTIVE: '활성', INACTIVE: '비활성', BLOCKED: '차단' };
const ROLE_LABELS: Record<string, string> = { USER: '일반', SELLER: '판매자', ADMIN: '관리자' };

export default function AdminUsersPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [meta, setMeta] = useState<PaginationMeta | null>(null);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<string | undefined>();
  const [roleFilter, setRoleFilter] = useState<string | undefined>();
  const [page, setPage] = useState(1);

  const fetchUsers = useCallback(() => {
    setLoading(true);
    userApi.getUsers({ page, limit: PAGE_SIZE, search: search || undefined, status: statusFilter, role: roleFilter })
      .then((res) => { setUsers(res.data.data); setMeta(res.data.meta || null); })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [page, search, statusFilter, roleFilter]);

  useEffect(() => { fetchUsers(); }, [fetchUsers]);

  const handleStatusChange = async (userId: number, status: string) => {
    try {
      await userApi.updateUserStatus(userId, status);
      message.success('상태가 변경되었습니다.');
      fetchUsers();
    } catch { message.error('변경에 실패했습니다.'); }
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
    { title: '이메일', dataIndex: 'email', key: 'email' },
    { title: '이름', dataIndex: 'name', key: 'name' },
    { title: '역할', dataIndex: 'role', key: 'role', render: (r: string) => <Tag>{ROLE_LABELS[r] || r}</Tag> },
    {
      title: '상태',
      dataIndex: 'status',
      key: 'status',
      render: (s: string, record: User) => (
        <Select
          value={s}
          size="small"
          style={{ width: 100 }}
          onChange={(v) => handleStatusChange(record.id, v)}
          options={Object.entries(STATUS_LABELS).map(([k, v]) => ({ label: v, value: k }))}
        />
      ),
    },
    { title: '포인트', dataIndex: 'point', key: 'point', render: (v: number) => (v || 0).toLocaleString() },
    { title: '가입일', dataIndex: 'createdAt', key: 'createdAt', render: (d: string) => formatDateTime(d) },
  ];

  return (
    <div>
      <Title level={3}>회원 관리</Title>
      <Card>
        <Space style={{ marginBottom: 16 }}>
          <Input.Search placeholder="이메일/이름 검색" onSearch={(v) => { setSearch(v); setPage(1); }} style={{ width: 250 }} />
          <Select allowClear placeholder="상태" value={statusFilter} onChange={(v) => { setStatusFilter(v); setPage(1); }} style={{ width: 100 }}
            options={Object.entries(STATUS_LABELS).map(([k, v]) => ({ label: v, value: k }))} />
          <Select allowClear placeholder="역할" value={roleFilter} onChange={(v) => { setRoleFilter(v); setPage(1); }} style={{ width: 100 }}
            options={Object.entries(ROLE_LABELS).map(([k, v]) => ({ label: v, value: k }))} />
        </Space>
        <Table dataSource={users} columns={columns} rowKey="id" loading={loading} pagination={false} size="middle" />
        {meta && <div style={{ textAlign: 'center', marginTop: 16 }}><Pagination current={page} total={meta.totalCount} pageSize={PAGE_SIZE} onChange={setPage} showSizeChanger={false} /></div>}
      </Card>
    </div>
  );
}
