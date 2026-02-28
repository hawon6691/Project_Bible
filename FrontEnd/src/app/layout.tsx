'use client';

import { useEffect } from 'react';
import { ConfigProvider, Layout } from 'antd';
import { AntdRegistry } from '@ant-design/nextjs-registry';
import { usePathname } from 'next/navigation';
import { theme } from '@/styles/antd-theme';
import { useAuthStore } from '@/lib/stores/authStore';
import { useCartStore } from '@/lib/stores/cartStore';
import Header from '@/components/layout/Header';
import Footer from '@/components/layout/Footer';
import '@/styles/globals.css';

const { Content } = Layout;

export default function RootLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const initialize = useAuthStore((s) => s.initialize);
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const fetchCart = useCartStore((s) => s.fetchCart);
  const showHeader = pathname !== '/';

  useEffect(() => { initialize(); }, [initialize]);
  useEffect(() => { if (isAuthenticated) fetchCart(); }, [isAuthenticated, fetchCart]);

  return (
    <html lang="ko">
      <head>
        <link
          href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet"
        />
        <link
          href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css"
          rel="stylesheet"
        />
      </head>
      <body>
        <AntdRegistry>
          <ConfigProvider theme={theme}>
            <Layout style={{ minHeight: '100vh' }}>
              {showHeader && <Header />}
              <Content style={{ flex: 1 }}>{children}</Content>
              <Footer />
            </Layout>
          </ConfigProvider>
        </AntdRegistry>
      </body>
    </html>
  );
}
