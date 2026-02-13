'use client';

import { Layout, Menu, Input, Badge, Dropdown, Button, Space, Avatar } from 'antd';
import { ShoppingCartOutlined, UserOutlined, SearchOutlined, LoginOutlined, LogoutOutlined, SettingOutlined, ShopOutlined, DashboardOutlined } from '@ant-design/icons';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { useAuthStore } from '@/lib/stores/authStore';
import { useCartStore } from '@/lib/stores/cartStore';
import { ROUTES } from '@/lib/utils/constants';
import { UserRole } from '@/types/user.types';
import type { MenuProps } from 'antd';

const { Header: AntHeader } = Layout;

export default function Header() {
  const router = useRouter();
  const { user, isAuthenticated, logout } = useAuthStore();
  const { totalCount } = useCartStore();
  const [searchValue, setSearchValue] = useState('');

  const handleSearch = () => {
    if (searchValue.trim()) {
      router.push(`${ROUTES.PRODUCTS}?search=${encodeURIComponent(searchValue.trim())}`);
    }
  };

  const userMenuItems: MenuProps['items'] = isAuthenticated
    ? [
        { key: 'mypage', icon: <UserOutlined />, label: <Link href={ROUTES.MYPAGE}>마이페이지</Link> },
        { key: 'orders', icon: <ShoppingCartOutlined />, label: <Link href={ROUTES.ORDERS}>주문내역</Link> },
        ...(user?.role === UserRole.SELLER ? [{ key: 'seller', icon: <ShopOutlined />, label: <Link href={ROUTES.SELLER}>판매자 관리</Link> }] : []),
        ...(user?.role === UserRole.ADMIN ? [{ key: 'admin', icon: <DashboardOutlined />, label: <Link href={ROUTES.ADMIN}>관리자</Link> }] : []),
        { key: 'settings', icon: <SettingOutlined />, label: <Link href={ROUTES.MYPAGE_PROFILE}>설정</Link> },
        { type: 'divider' as const },
        { key: 'logout', icon: <LogoutOutlined />, label: '로그아웃', onClick: async () => { await logout(); router.push(ROUTES.HOME); } },
      ]
    : [];

  const navItems: MenuProps['items'] = [
    { key: 'products', label: <Link href={ROUTES.PRODUCTS}>상품</Link> },
    { key: 'compare', label: <Link href={ROUTES.COMPARE}>비교</Link> },
  ];

  return (
    <AntHeader style={{ display: 'flex', alignItems: 'center', gap: 16, padding: '0 24px', borderBottom: '1px solid #f0f0f0' }}>
      <Link href={ROUTES.HOME} style={{ fontSize: 20, fontWeight: 700, color: '#1677ff', whiteSpace: 'nowrap' }}>NestShop</Link>
      <Menu mode="horizontal" items={navItems} style={{ flex: '0 0 auto', border: 'none' }} />
      <Input.Search placeholder="상품 검색" value={searchValue} onChange={(e) => setSearchValue(e.target.value)} onSearch={handleSearch} style={{ maxWidth: 400, flex: 1 }} enterButton={<SearchOutlined />} />
      <Space size="middle" style={{ marginLeft: 'auto' }}>
        {isAuthenticated ? (
          <>
            <Link href={ROUTES.CART}><Badge count={totalCount} size="small"><ShoppingCartOutlined style={{ fontSize: 20 }} /></Badge></Link>
            <Dropdown menu={{ items: userMenuItems }} placement="bottomRight"><Avatar icon={<UserOutlined />} style={{ cursor: 'pointer' }} /></Dropdown>
          </>
        ) : (
          <Space>
            <Button type="text" icon={<LoginOutlined />} onClick={() => router.push(ROUTES.LOGIN)}>로그인</Button>
            <Button type="primary" onClick={() => router.push(ROUTES.SIGNUP)}>회원가입</Button>
          </Space>
        )}
      </Space>
    </AntHeader>
  );
}
