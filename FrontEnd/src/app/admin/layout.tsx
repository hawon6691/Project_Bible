'use client';

import { Layout } from 'antd';
import AdminSidebar from '@/components/layout/AdminSidebar';
import AuthGuard from '@/components/common/AuthGuard';
import { UserRole } from '@/types/user.types';

const { Content } = Layout;

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  return (
    <AuthGuard requiredRole={UserRole.ADMIN}>
      <Layout style={{ minHeight: 'calc(100vh - 64px)' }}>
        <AdminSidebar />
        <Content style={{ padding: 24, background: '#f5f5f5' }}>
          {children}
        </Content>
      </Layout>
    </AuthGuard>
  );
}
