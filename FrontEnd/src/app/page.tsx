'use client';

import { useEffect, useState } from 'react';
import { Spin } from 'antd';
import Link from 'next/link';
import { productApi, categoryApi } from '@/lib/api/endpoints';
import { ROUTES } from '@/lib/utils/constants';
import type { ProductSummary, Category } from '@/types/product.types';
import { formatPrice } from '@/lib/utils/format';
import HomeStyleHeader from '@/components/layout/HomeStyleHeader';
import './page-home.css';

export default function HomePage() {
  const [popularProducts, setPopularProducts] = useState<ProductSummary[]>([]);
  const [newProducts, setNewProducts] = useState<ProductSummary[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [hoveredCategory, setHoveredCategory] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      productApi.getAll({ sort: 'popularity', limit: 12 }),
      productApi.getAll({ sort: 'newest', limit: 12 }),
      categoryApi.getAll(),
    ]).then(([popRes, newRes, categoryRes]) => {
      setPopularProducts(popRes.data.data);
      setNewProducts(newRes.data.data);
      setCategories(categoryRes.data.data);
    }).catch(() => {}).finally(() => setLoading(false));
  }, []);

  if (loading) return <div style={{ display: 'flex', justifyContent: 'center', padding: 100 }}><Spin size="large" /></div>;

  // .etc/shop_requirement.md 기준 메인 카테고리
  const homeCategories = [
    { name: '가전/TV', icon: 'bi-tv' },
    { name: '컴퓨터/노트북/조립PC', icon: 'bi-laptop' },
    { name: '태블릿/모바일/디카', icon: 'bi-phone' },
    { name: '아웃도어/스포츠/골프', icon: 'bi-trophy' },
    { name: '자동차/용품/공구', icon: 'bi-car-front' },
    { name: '가구/조명', icon: 'bi-lamp' },
    { name: '식품/유아/완구', icon: 'bi-egg-fried' },
    { name: '생활/주방/건강', icon: 'bi-cup-hot' },
    { name: '패션/잡화/화장품', icon: 'bi-bag' },
    { name: '사무/취미/반려동물', icon: 'bi-pencil-square' },
  ];
  const topTabs: Array<{ label: string; submenu?: string[] }> = [
    { label: '자동차' },
    { label: '조립PC' },
    { label: 'PC견적' },
    { label: '중고' },
    { label: '쇼핑기획전' },
    { label: '커뮤니티' },
    { label: '이벤트/체험단' },
  ];

  const categoryDirectory: Record<
    string,
    {
      leftTitle: string;
      leftItems: string[];
      rightTitle: string;
      rightItems: string[];
      tips: string[];
    }
  > = {
    '가전/TV': {
      leftTitle: '가전/TV 구매상담',
      leftItems: ['TV', '프로젝터/스크린', '홈시어터/오디오', '영상/음향가전', '음향가전'],
      rightTitle: '생활/계절가전',
      rightItems: ['세탁기/건조기', '청소기', '공기청정기/계절가전', '미용/소형가전'],
      tips: ['LG 워시타워 VS 삼성 올인원 비교', '26년형 에어컨 출시 EVENT', '가전 배송/설치 비용 정리'],
    },
    '컴퓨터/노트북/조립PC': {
      leftTitle: '컴퓨터',
      leftItems: ['노트북', '브랜드PC/조립PC', '모니터/사운드', 'PC주요부품', 'PC저장장치'],
      rightTitle: 'PC주변기기',
      rightItems: ['복합기/프린터/SW', '게임기/게이밍가구', '기업구매상담', 'PC견적', '표준PC'],
      tips: ['그래픽카드 BEST 가성비 가이드', 'SSD 용량별 추천 정리', '게이밍PC 예산별 견적 모음'],
    },
    '태블릿/모바일/디카': {
      leftTitle: '태블릿/스마트폰',
      leftItems: ['휴대폰/스마트폰', '태블릿/전자책', '스마트워치/VR', '모바일 액세서리'],
      rightTitle: '포터블 음향/디카',
      rightItems: ['이어폰/헤드폰', '휴대용 스피커', '카메라/액션캠', '촬영용품', '메모리카드'],
      tips: ['갤럭시 vs 아이폰 카메라 비교', '무선이어폰 음질 순위', '입문용 미러리스 추천'],
    },
    '아웃도어/스포츠/골프': {
      leftTitle: '골프',
      leftItems: ['골프클럽', '골프용품', '골프의류/잡화'],
      rightTitle: '아웃도어/스포츠',
      rightItems: ['캠핑', '자전거/전동킥보드', '등산', '구기/라켓', '스포츠의류'],
      tips: ['봄 시즌 골프채 인기순위', '캠핑 입문 필수템', '러닝화 쿠셔닝 비교'],
    },
    '자동차/용품/공구': {
      leftTitle: '자동차용품',
      leftItems: ['타이어/휠/배터리', '블박/내비', '오일/필터', '충전/거치/수납', '세차/와이퍼'],
      rightTitle: '공구/산업용품',
      rightItems: ['전동드릴/드라이버', '절삭/연마공구', '수공구/안전용품', '원예/건설장비'],
      tips: ['블랙박스 실사용 후기 모음', '배터리 교체 주기 체크', '가정용 공구 세트 추천'],
    },
    '가구/조명': {
      leftTitle: '가구',
      leftItems: ['침대/매트리스', '소파/리클라이너', '거실장/테이블', '의자', '책상/책장'],
      rightTitle: '조명/인테리어',
      rightItems: ['LED전구/조명', '수납가구', '침구/커튼/카페트', '홈데코/소품'],
      tips: ['원룸 가구 배치 가이드', '책상 의자 인체공학 추천', '무드등 베스트 10'],
    },
    '식품/유아/완구': {
      leftTitle: '식품',
      leftItems: ['건강식품/홍삼', '라면/밥/찌개', '냉동/냉장식품', '생수/음료/우유', '커피/차'],
      rightTitle: '유아/완구',
      rightItems: ['분유/기저귀/물티슈', '유모차/카시트', '놀이매트', '장난감/인형'],
      tips: ['아이 간식 인기순위', '분유 단계별 추천', '명절 선물세트 특가 모음'],
    },
    '생활/주방/건강': {
      leftTitle: '생활용품',
      leftItems: ['세제/세정/탈취', '욕실/청소/세탁', '수납/정리용품'],
      rightTitle: '주방/건강',
      rightItems: ['냄비/팬/조리도구', '식기/보관용기', '건강측정/물리치료', '안마/찜질용품'],
      tips: ['주방 필수템 TOP 20', '청소도구 비교 리뷰', '안마기 부위별 추천'],
    },
    '패션/잡화/화장품': {
      leftTitle: '패션잡화/의류',
      leftItems: ['가방/지갑', '시계/주얼리', '남성의류', '여성의류', '언더웨어'],
      rightTitle: '뷰티',
      rightItems: ['스킨케어', '헤어케어', '바디케어', '향수/메이크업', '남성뷰티'],
      tips: ['가을 신상 아우터 모음', '쿠션 파운데이션 비교', '남성 스킨케어 입문 가이드'],
    },
    '사무/취미/반려동물': {
      leftTitle: '문구/사무',
      leftItems: ['복사용지/사무용지', '사무기기/금고', '필기/화방용품', 'CCTV/보안'],
      rightTitle: '취미/반려동물',
      rightItems: ['드론/레고', '악기/디지털피아노', '강아지용품', '고양이/관상어용품'],
      tips: ['재택근무 사무용품 추천', '초보 집사를 위한 준비물', '키덜트 취미템 인기순위'],
    },
  };

  const normalizeName = (value: string) =>
    value.toLowerCase().replace(/[\s/·,-]/g, '');

  const flattenCategories = (items: Category[]): Category[] =>
    items.flatMap((item) => [item, ...flattenCategories(item.children || [])]);

  const allCategories = flattenCategories(categories);

  const resolveCategoryLink = (keyword: string) => {
    const target = normalizeName(keyword);
    const matched = allCategories.find((cat) => {
      const catName = normalizeName(cat.name);
      return target.includes(catName) || catName.includes(target);
    });

    return matched ? ROUTES.CATEGORY(matched.id) : ROUTES.PRODUCTS;
  };

  return (
    <div className="ns-home container py-4">
      <HomeStyleHeader />

      <section className="row g-3 mb-4">
        <div className="col-lg-3">
          <div
            className="ns-side-menu-wrap"
            onMouseLeave={() => setHoveredCategory(null)}
          >
            <div className="ns-side-menu h-100">
            <div className="ns-side-menu-title">
              <span className="me-2"><i className="bi bi-list" /></span>
              <span>전체 카테고리</span>
            </div>
            <ul className="list-unstyled m-0 p-3">
              {homeCategories.map((cat) => (
                <li
                  key={cat.name}
                  className={`ns-side-item ${hoveredCategory === cat.name ? 'active' : ''}`}
                  onMouseEnter={() => setHoveredCategory(cat.name)}
                >
                  <button type="button" className="ns-side-trigger">
                    <span className="ns-side-icon">
                      <i className={`bi ${cat.icon}`} />
                    </span>
                    <span>{cat.name}</span>
                    <i className="bi bi-chevron-right ms-auto" />
                  </button>
                </li>
              ))}
            </ul>
            </div>
            {hoveredCategory && categoryDirectory[hoveredCategory] && (
              <div className="ns-mega-panel">
                <div className="ns-mega-col">
                  <h6>{categoryDirectory[hoveredCategory].leftTitle}</h6>
                  <ul>
                    {categoryDirectory[hoveredCategory].leftItems.map((item) => (
                      <li key={item}>
                        <Link href={resolveCategoryLink(item)}>{item}</Link>
                      </li>
                    ))}
                  </ul>
                </div>
                <div className="ns-mega-col ns-mega-divider">
                  <h6>{categoryDirectory[hoveredCategory].rightTitle}</h6>
                  <ul>
                    {categoryDirectory[hoveredCategory].rightItems.map((item) => (
                      <li key={item}>
                        <Link href={resolveCategoryLink(item)}>{item}</Link>
                      </li>
                    ))}
                  </ul>
                </div>
                <div className="ns-mega-col ns-mega-tips">
                  <h6>쇼핑꿀팁</h6>
                  <ul>
                    {categoryDirectory[hoveredCategory].tips.map((tip) => (
                      <li key={tip}>{tip}</li>
                    ))}
                  </ul>
                </div>
              </div>
            )}
          </div>
        </div>
        <div className="col-lg-9">
          <div className="ns-main-tabs d-flex justify-content-between align-items-center mb-3">
            <div className="ns-tab-links d-flex flex-wrap gap-3">
              {topTabs.map((tab, idx) => (
                <div key={tab.label} className="ns-tab-item">
                  <span className={idx === 0 ? 'active' : ''}>
                    {tab.label}
                    {tab.submenu ? <i className="bi bi-chevron-down ns-tab-caret" /> : null}
                  </span>
                  {tab.submenu ? (
                    <div className="ns-tab-dropdown">
                      <ul>
                        {tab.submenu.map((menu) => (
                          <li key={menu}>{menu}</li>
                        ))}
                      </ul>
                    </div>
                  ) : null}
                </div>
              ))}
            </div>
          </div>
          <div className="row g-3 mb-3">
            <div className="col-md-9">
              <div className="ns-hero-card ns-hero-main p-4">
                <p className="text-uppercase small m-0">SPECIAL DEAL</p>
                <h3 className="mt-2 mb-1">가격비교로 찾는 오늘의 핫딜</h3>
                <p className="mb-0 text-secondary">실시간 최저가와 배송조건을 한 번에 비교하세요.</p>
              </div>
            </div>
            <div className="col-md-3">
              <div className="ns-hero-card p-3 ns-news-ticker">
                <ul className="m-0 ps-3">
                  <li>듀얼 모니터도 최적인 고성능 노트북</li>
                  <li>초경량 LG gram 체험 리뷰</li>
                  <li>인피니티 미러를 탑재한 케이스 특가</li>
                </ul>
              </div>
            </div>
          </div>
          <div className="row g-3">
            <div className="col-lg-8">
              <div className="ns-info-card p-3">
                <div className="d-flex justify-content-between align-items-center mb-3">
                  <h4 className="m-0 fw-bold">뉴스룸</h4>
                  <span className="small text-secondary">주요이슈 · 컴퓨터 · 테크</span>
                </div>
                <div className="row g-2">
                  {[1, 2, 3, 4].map((idx) => (
                    <div className="col-md-6" key={idx}>
                      <div className="ns-news-card">
                        <div className="ns-news-thumb">NEWS</div>
                        <p className="mb-0 small">그래픽카드 {idx}세대 성능과 가성비 분석 요약</p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
            <div className="col-lg-4">
              <div className="ns-info-card p-3 mb-3">
                <h5 className="fw-bold mb-2">남들은 월 10만원 더 싸게 타던데?</h5>
                <div className="ns-ad-box">AD BANNER</div>
              </div>
              <div className="ns-info-card p-3">
                <h6 className="fw-bold mb-0">건강식품 가이드: 영양제 주요 기능 모아보기</h6>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="mb-4">
        <div className="d-flex justify-content-between align-items-center mb-3">
          <h4 className="m-0 fw-bold">인기 상품</h4>
          <Link href={`${ROUTES.PRODUCTS}?sort=popularity`} className="small text-decoration-none">더보기</Link>
        </div>
        <div className="row row-cols-2 row-cols-md-3 row-cols-xl-6 g-3">
          {popularProducts.slice(0, 12).map((p) => (
            <div key={p.id} className="col">
              <Link href={ROUTES.PRODUCT_DETAIL(p.id)} className="text-decoration-none text-dark">
                <div className="ns-product-card p-2">
                  <div className="ns-product-thumb mb-2">
                    {p.thumbnailUrl ? <img src={p.thumbnailUrl} alt={p.name} /> : <span>NO IMAGE</span>}
                  </div>
                  <p className="ns-product-name mb-1">{p.name}</p>
                  <p className="ns-product-price mb-0">{formatPrice(p.lowestPrice)}</p>
                </div>
              </Link>
            </div>
          ))}
        </div>
      </section>

      <section className="mb-4">
        <div className="d-flex justify-content-between align-items-center mb-3">
          <h4 className="m-0 fw-bold">신상품</h4>
          <Link href={`${ROUTES.PRODUCTS}?sort=newest`} className="small text-decoration-none">더보기</Link>
        </div>
        <div className="row row-cols-2 row-cols-md-3 row-cols-xl-6 g-3">
          {newProducts.slice(0, 12).map((p) => (
            <div key={p.id} className="col">
              <Link href={ROUTES.PRODUCT_DETAIL(p.id)} className="text-decoration-none text-dark">
                <div className="ns-product-card p-2">
                  <div className="ns-product-thumb mb-2">
                    {p.thumbnailUrl ? <img src={p.thumbnailUrl} alt={p.name} /> : <span>NO IMAGE</span>}
                  </div>
                  <p className="ns-product-name mb-1">{p.name}</p>
                  <p className="ns-product-price mb-0">{formatPrice(p.lowestPrice)}</p>
                </div>
              </Link>
            </div>
          ))}
        </div>
      </section>

      <section className="row g-3">
        <div className="col-lg-8">
          <div className="ns-info-card p-3">
            <h6 className="fw-bold">오늘의 추천 키워드</h6>
            <div className="d-flex flex-wrap gap-2">
              {['RTX', '게이밍노트북', 'SSD', '모니터', '무선이어폰', '기계식키보드'].map((keyword) => (
                <span key={keyword} className="badge rounded-pill text-bg-light border">{keyword}</span>
              ))}
            </div>
          </div>
        </div>
        <div className="col-lg-4">
          <div className="ns-info-card p-3 h-100">
            <h6 className="fw-bold">서비스 바로가기</h6>
            <ul className="list-unstyled m-0 small">
              <li className="mb-2"><Link href={ROUTES.COMPARE}>상품 비교</Link></li>
              <li className="mb-2"><Link href={ROUTES.CART}>장바구니</Link></li>
              <li className="mb-2"><Link href={ROUTES.ORDERS}>주문 내역</Link></li>
              <li><Link href={ROUTES.MYPAGE}>마이페이지</Link></li>
            </ul>
          </div>
        </div>
      </section>
    </div>
  );
}
