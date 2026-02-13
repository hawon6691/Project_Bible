'use client';

import { Layout, Menu } from 'antd';
import { DashboardOutlined, ShoppingOutlined, AppstoreOutlined, UserOutlined, OrderedListOutlined, ShopOutlined, StarOutlined, GiftOutlined, ToolOutlined, PictureOutlined, QuestionCircleOutlined, NotificationOutlined, BarChartOutlined, SettingOutlined } from '@ant-design/icons';
import { useRouter, usePathname } from 'next/navigation';
import { useUIStore } from '@/lib/stores/uiStore';
import { ROUTES } from '@/lib/utils/constants';
import type { MenuProps } from 'antd';

const { Sider } = Layout;

const menuItems: MenuProps['items'] = [
  { key: ROUTES.ADMIN, icon: <DashboardOutlined />, label: '대시보드' },
  { key: ROUTES.ADMIN_PRODUCTS, icon: <ShoppingOutlined />, label: '상품 관리' },
  { key: ROUTES.ADMIN_CATEGORIES, icon: <AppstoreOutlined />, label: '카테고리 관리' },
  { key: ROUTES.ADMIN_SPECS, icon: <ToolOutlined />, label: '스펙 관리' },
  { key: ROUTES.ADMIN_USERS, icon: <UserOutlined />, label: '회원 관리' },
  { key: ROUTES.ADMIN_SELLERS, icon: <ShopOutlined />, label: '판매자 관리' },
  { key: ROUTES.ADMIN_ORDERS, icon: <OrderedListOutlined />, label: '주문 관리' },
  { key: ROUTES.ADMIN_REVIEWS, icon: <StarOutlined />, label: '리뷰 관리' },
  { key: ROUTES.ADMIN_COUPONS, icon: <GiftOutlined />, label: '쿠폰 관리' },
  { key: ROUTES.ADMIN_BANNERS, icon: <PictureOutlined />, label: '배너 관리' },
  { key: ROUTES.ADMIN_FAQS, icon: <QuestionCircleOutlined />, label: 'FAQ 관리' },
  { key: ROUTES.ADMIN_NOTICES, icon: <NotificationOutlined />, label: '공지사항' },
  { key: ROUTES.ADMIN_STATS, icon: <BarChartOutlined />, label: '통계' },
  { key: ROUTES.ADMIN_SETTINGS, icon: <SettingOutlined />, label: '설정' },
];

export default function AdminSidebar() {
  const router = useRouter();
  const pathname = usePathname();
  const { sidebarCollapsed, setSidebarCollapsed } = useUIStore();

  return (
    <Sider collapsible collapsed={sidebarCollapsed} onCollapse={setSidebarCollapsed} style={{ background: '#fff' }} width={220}>
      <div style={{ height: 32, margin: 16, textAlign: 'center', fontWeight: 700, fontSize: sidebarCollapsed ? 14 : 18 }}>
        {sidebarCollapsed ? 'NS' : 'NestShop Admin'}
      </div>
      <Menu mode="inline" selectedKeys={[pathname]} items={menuItems} onClick={({ key }) => router.push(key)} />
    </Sider>
  );
}
