'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { ROUTES } from '@/lib/utils/constants';
import './home-style-header.css';

export default function HomeStyleHeader() {
  const router = useRouter();
  const [searchValue, setSearchValue] = useState('');

  const handleSearch = () => {
    const q = searchValue.trim();
    if (!q) return;
    router.push(`${ROUTES.PRODUCTS}?search=${encodeURIComponent(q)}`);
  };

  return (
    <section className="mb-3 ns-top-header">
      <div className="d-flex align-items-center gap-3 mb-2">
        <Link href={ROUTES.HOME} className="ns-logo m-0 text-decoration-none">NestShop</Link>
        <div className="input-group">
          <input
            className="form-control"
            placeholder="출근길에 자주 본 인기 남성 백팩 컬렉션"
            value={searchValue}
            onChange={(e) => setSearchValue(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') handleSearch();
            }}
          />
          <button className="btn btn-primary" onClick={handleSearch}>검색</button>
        </div>
        <div className="ns-quick-icons d-none d-lg-flex">
          <div className="ns-quick-icon"><span><i className="bi bi-clock-history" /></span><small>최근</small></div>
          <div className="ns-quick-icon"><span><i className="bi bi-heart" /></span><small>관심</small></div>
          <div className="ns-quick-icon ns-login-menu">
            <span><i className="bi bi-person" /></span>
            <small>로그인</small>
            <div className="ns-login-dropdown">
              <ul>
                <li><Link href={ROUTES.LOGIN}>로그인</Link></li>
                <li><Link href={ROUTES.SIGNUP}>회원가입</Link></li>
                <li>ID 찾기</li>
                <li>비밀번호 찾기</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
