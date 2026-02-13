'use client';

import { useEffect } from 'react';
import { ConfigProvider, Layout } from 'antd';
import { AntdRegistry } from '@ant-design/nextjs-registry';
import { theme } from '@/styles/antd-theme';
import { useAuthStore } from '@/lib/stores/authStore';
import { useCartStore } from '@/lib/stores/cartStore';
import Header from '@/components/layout/Header';
import Footer from '@/components/layout/Footer';
import '@/styles/globals.css';

const { Content } = Layout;

export default function RootLayout({ children }: { children: React.ReactNode }) {
  const initialize = useAuthStore((s) => s.initialize);
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const fetchCart = useCartStore((s) => s.fetchCart);

  useEffect(() => { initialize(); }, [initialize]);
  useEffect(() => { if (isAuthenticated) fetchCart(); }, [isAuthenticated, fetchCart]);

  return (
    <html lang="ko">
      <body>
        <AntdRegistry>
          <ConfigProvider theme={theme}>
            <Layout style={{ minHeight: '100vh' }}>
              <Header />
              <Content style={{ flex: 1 }}>{children}</Content>
              <Footer />
            </Layout>
          </ConfigProvider>
        </AntdRegistry>
      </body>
    </html>
  );
}
