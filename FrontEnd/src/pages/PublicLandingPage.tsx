import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  fetchDeals,
  fetchNews,
  fetchPopularKeywords,
  fetchProducts,
  fetchRankingProducts,
} from '@/lib/endpoints';
import type { DealItem, NewsItem, ProductSummary, RankingProductItem } from '@/lib/types';

const displayCategories = [
  'AI',
  '가전/TV',
  '컴퓨터/노트북/조립PC',
  '태블릿/모바일/디카',
  '스포츠/골프',
  '자동차/용품/공구',
  '가구/조명',
  '식품/유아/완구',
  '생활/주방/건강',
  '패션/잡화/뷰티',
  '취미/문구/반려',
  '로켓배송관',
];

const promoCards = [
  {
    title: '최대 60% OFF! 쇼핑 찬스',
    image: 'https://images.unsplash.com/photo-1614680376408-81e91ffe3db7?w=640&q=80&auto=format&fit=crop',
  },
  {
    title: '26 봄 마라톤 완벽 대비',
    image: 'https://images.unsplash.com/photo-1552674605-db6ffd4facb5?w=640&q=80&auto=format&fit=crop',
  },
  {
    title: '2026 개화 시기는? 봄꽃 신발 추천',
    image: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=640&q=80&auto=format&fit=crop',
  },
];

const communityItems = [
  '플랜 그림보고 흥분했네요',
  '자신감을 되찾는 방법',
  '메모리에 이어서 ssd 업그레이드',
  '경북 영주 F-16C 추락 사고',
  '경험입니다. 가구리뷰 찾아 오려나요',
  '모니터 암 추천 부탁드립니다',
  '3월 4일 뉴스요약',
  'PC케이스 커스텀 공유',
];

const eventBanners = [
  'https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=640&q=80&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=640&q=80&auto=format&fit=crop',
];

const autoCards = [
  {
    title: '쉐보레, 트랙스 크로스오버 출시',
    summary: '신형 트림과 안전 옵션 강화 모델 공개.',
    image: 'https://images.unsplash.com/photo-1492144534655-ae79c964c9d7?w=640&q=80&auto=format&fit=crop',
  },
  {
    title: 'MINI 쿠퍼 특별 에디션 공개',
    summary: '도심형 전기차 라인업 확장.',
    image: 'https://images.unsplash.com/photo-1584345604476-8ec5f452d1f2?w=640&q=80&auto=format&fit=crop',
  },
  {
    title: '벤츠 EQE 350+ SUV 국내 출시',
    summary: '고효율 전비와 주행 보조 강화.',
    image: 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=640&q=80&auto=format&fit=crop',
  },
];

const categoryMallGroups: Array<{ name: string; products: Array<{ name: string; price: number }> }> = [
  { name: '패션의류', products: ['아우터', '니트', '셔츠', '팬츠', '원피스', '트레이닝', '자켓', '가디건'].map((name, idx) => ({ name, price: 9900 + idx * 4300 })) },
  { name: '패션잡화', products: ['가방', '지갑', '벨트', '모자', '시계', '주얼리', '양말', '장갑'].map((name, idx) => ({ name, price: 7900 + idx * 5100 })) },
  { name: '가구/인테리어', products: ['침대', '소파', '책상', '의자', '수납장', '조명', '거울', '러그'].map((name, idx) => ({ name, price: 19900 + idx * 9200 })) },
  { name: '생활/주방', products: ['청소용품', '욕실용품', '수납', '식기', '주방가전', '텀블러', '밀폐용기', '행주'].map((name, idx) => ({ name, price: 3900 + idx * 2600 })) },
  { name: '식품/건강', products: ['간편식', '정육', '과일', '건강식품', '음료', '커피', '유제품', '과자'].map((name, idx) => ({ name, price: 2500 + idx * 1800 })) },
  { name: '출산/유아동', products: ['기저귀', '분유', '물티슈', '유모차', '젖병', '아기침대', '완구', '학습교구'].map((name, idx) => ({ name, price: 7900 + idx * 6100 })) },
  { name: '디지털/가전', products: ['이어폰', '스피커', '가습기', '공기청정기', '청소기', '드라이기', '면도기', '전기포트'].map((name, idx) => ({ name, price: 12900 + idx * 8500 })) },
  { name: '컴퓨터/게임', products: ['키보드', '마우스', '모니터', '노트북', 'SSD', '램', '헤드셋', '의자'].map((name, idx) => ({ name, price: 15900 + idx * 12000 })) },
  { name: '자동차/공구', products: ['세차용품', '와이퍼', '방향제', '점프선', '타이어체인', '드릴', '공구세트', '에어건'].map((name, idx) => ({ name, price: 6900 + idx * 7400 })) },
  { name: '스포츠/레저', products: ['러닝화', '요가매트', '덤벨', '텐트', '의자', '랜턴', '낚시대', '자전거용품'].map((name, idx) => ({ name, price: 8900 + idx * 6800 })) },
  { name: '취미/문구', products: ['노트', '펜', '프라모델', '드론', '카메라가방', '보드게임', '스케치북', '붓세트'].map((name, idx) => ({ name, price: 2900 + idx * 3900 })) },
  { name: '반려동물', products: ['사료', '간식', '장난감', '하우스', '위생용품', '외출용품', '캣타워', '배변패드'].map((name, idx) => ({ name, price: 4900 + idx * 4700 })) },
];

const sampleThumbs = [
  'https://images.unsplash.com/photo-1585386959984-a4155224a1ad?w=240&q=80&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=240&q=80&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=240&q=80&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1583394838336-acd977736f90?w=240&q=80&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1616627454826-7f1d30c7f68f?w=240&q=80&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=240&q=80&auto=format&fit=crop',
];

export default function PublicLandingPage() {
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [ranking, setRanking] = useState<RankingProductItem[]>([]);
  const [popularKeywords, setPopularKeywords] = useState<string[]>([]);
  const [news, setNews] = useState<NewsItem[]>([]);
  const [deals, setDeals] = useState<DealItem[]>([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      fetchProducts({ page: 1, limit: 10, sort: 'popularity' }),
      fetchRankingProducts(8),
      fetchPopularKeywords(8),
      fetchNews(4),
      fetchDeals(6),
    ])
      .then(([productRes, rankRes, keywordRes, newsRes, dealRes]) => {
        setProducts(productRes.data);
        setRanking(rankRes.data);
        setPopularKeywords(keywordRes.data.items.map((item) => item.keyword));
        setNews(newsRes.data);
        setDeals(dealRes.data);
      })
      .catch((err: Error) => setError(err.message || 'failed to load'))
      .finally(() => setLoading(false));
  }, []);

  const displayDeals = useMemo(() => (deals.length ? deals : products.slice(0, 6).map((p) => ({
    id: p.id,
    title: p.name,
    description: '오늘의 특가 상품',
    discountRate: 10,
    product: { id: p.id },
  } as unknown as DealItem))), [deals, products]);

  const displayNews = useMemo(
    () =>
      news.length
        ? news
        : [
            { id: 1, title: '인기 부품 가격 리포트', summary: '이번 주 주요 부품 가격 변동 요약' },
            { id: 2, title: '봄맞이 가전 구매 가이드', summary: '공간/예산별 추천 모델 정리' },
            { id: 3, title: '초경량 노트북 비교', summary: '실사용 중심 성능 비교' },
          ],
    [news],
  );

  return (
    <section className="danawa-shell revamped-main">
      <div className="rev-main-grid">
        <aside id="home-categories" className="rev-category-panel">
          <div className="rev-category-head">☰ 전체 카테고리</div>
          <ul className="rev-category-list">
            {displayCategories.map((name) => (
              <li key={name}>
                <Link to={`/public/products?search=${encodeURIComponent(name)}`}>{name}</Link>
              </li>
            ))}
          </ul>
        </aside>

        <div className="rev-main-content">
          <div className="rev-news-top">
            <div className="rev-news-box">
              <div className="rev-box-head">
                <h2>뉴스룸</h2>
                <div className="rev-box-tabs">주요이슈 · 컴퓨터 · 테크 · 자동차</div>
              </div>
              <ul>
                {displayNews.map((item) => (
                  <li key={item.id}>
                    <strong>{item.title}</strong>
                    <p>{item.summary}</p>
                  </li>
                ))}
              </ul>
            </div>

            <div className="rev-issue-box">
              <ul>
                {(popularKeywords.length ? popularKeywords : ['핫딜', '그래픽카드', '게이밍노트북', 'TV추천']).map((item) => (
                  <li key={item}>
                    <Link to={`/public/products?search=${encodeURIComponent(item)}`}>{item}</Link>
                  </li>
                ))}
              </ul>
            </div>
          </div>

          <div className="rev-promo-row">
            {promoCards.map((card) => (
              <article key={card.title} className="rev-promo-card">
                <img src={card.image} alt={card.title} />
                <h3>{card.title}</h3>
              </article>
            ))}
          </div>

          <div className="rev-special-section">
            <div className="rev-section-head">
              <h2>놓치면 후회하는 오늘의 특가</h2>
              <Link to="/public/products">전체보기</Link>
            </div>
            <div className="rev-deal-grid">
              {displayDeals.slice(0, 6).map((deal, idx) => (
                <article key={`${deal.id}-${idx}`} className="rev-deal-item">
                  <img src={sampleThumbs[idx % sampleThumbs.length]} alt={deal.title} />
                  <h3>{deal.title}</h3>
                  <p>{deal.description || '오늘만 특가 진행 중'}</p>
                  <strong>{((products[idx]?.lowestPrice ?? 95500) as number).toLocaleString()}원</strong>
                </article>
              ))}
            </div>
          </div>

          <div className="rev-community-event">
            <div className="rev-community-box">
              <div className="rev-section-head small">
                <h2>커뮤니티</h2>
                <span>3/5</span>
              </div>
              <ol>
                {communityItems.map((item) => (
                  <li key={item}>{item}</li>
                ))}
              </ol>
            </div>
            <div className="rev-event-box">
              <div className="rev-section-head small">
                <h2>이벤트/체험단</h2>
                <span>1/2</span>
              </div>
              <div className="rev-event-circles">
                {eventBanners.map((img, idx) => (
                  <div key={`${idx}-${img}`} className="rev-circle">
                    <img src={img} alt="event" />
                  </div>
                ))}
              </div>
            </div>
          </div>

          <div className="rev-auto-shop-row">
            <div className="rev-auto-box">
              <div className="rev-section-head small">
                <h2>자동차</h2>
                <span>1/1</span>
              </div>
              <div className="rev-auto-cards">
                {autoCards.map((item) => (
                  <article key={item.title}>
                    <img src={item.image} alt={item.title} />
                    <h3>{item.title}</h3>
                    <p>{item.summary}</p>
                    <button type="button">모델 정보 보기</button>
                  </article>
                ))}
              </div>
            </div>

            <div className="rev-shop-box">
              <div className="rev-section-head small">
                <h2>샵다나와</h2>
                <span>4/7</span>
              </div>
              <div className="rev-shop-feature">
                <img src="https://images.unsplash.com/photo-1624705002806-5d72df19c3f0?w=540&q=80&auto=format&fit=crop" alt="pc견적" />
                <div>
                  <strong>[PC견적] 조립 갤러리 추천 구성</strong>
                  <p>인텔 코어 울트라 + RTX 시리즈 조합</p>
                  <h3>3,574,230원</h3>
                  <button type="button">제품 감상하기</button>
                </div>
              </div>
            </div>
          </div>

          <div className="rev-category-mall">
            <div className="rev-section-head">
              <h2>카테고리별 추천 쇼핑</h2>
            </div>
            {categoryMallGroups.map((group, gIdx) => (
              <div key={group.name} className="rev-mall-group">
                <div className="rev-mall-title">{group.name}</div>
                <div className="rev-mall-products">
                  {group.products.map((item, idx) => (
                    <Link key={item.name} to={`/public/products?search=${encodeURIComponent(item.name)}`} className="rev-mall-item">
                      <img src={sampleThumbs[(gIdx + idx) % sampleThumbs.length]} alt={item.name} />
                      <span>{item.name}</span>
                      <em>{item.price.toLocaleString()}원</em>
                    </Link>
                  ))}
                </div>
              </div>
            ))}
          </div>

          <div className="rev-bottom-logos">
            {['SAMSUNG', 'LG', 'APPLE', 'ASUS', 'MSI', 'Lenovo', 'DELL', 'SONY', 'Canon', 'Dyson', 'Nikon', 'PHILIPS'].map((brand) => (
              <span key={brand}>{brand}</span>
            ))}
          </div>
        </div>
      </div>

      {loading ? <p className="text-secondary mt-3">로딩 중...</p> : null}
      {error ? <p className="text-danger mt-3">{error}</p> : null}
    </section>
  );
}
