# NestShop FrontEnd (API Contract First)

## 목적
- 백엔드 구현 언어와 무관하게 동작하도록 API 계약(`success/data/meta`) 기준으로 작성된 프론트엔드입니다.
- 기본 개발 서버 포트는 `3000`입니다.

## 실행
1. `npm install`
2. `.env.example`을 `.env`로 복사
3. 필요 시 `VITE_API_BASE_URL` 수정 (기본값: `http://localhost:8000/api/v1`)
4. `npm run dev`

## 주요 페이지
- `/` 홈 (카테고리 + 인기 상품)
- `/products` 상품 목록 (검색/카테고리 쿼리)
- `/products/:id` 상품 상세
- `/login` 로그인
- `/cart` 장바구니 (로그인/비로그인 자동 분기)

## API 호환 규칙
- 공통 응답 포맷:
  - `success: true`
  - `data: ...`
  - `meta: { page, limit, totalCount, totalPages }` (선택)
- 이 포맷만 맞추면 백엔드 언어(Node, Java, Go, Python 등)와 무관하게 동일 프론트를 사용할 수 있습니다.
- 프론트에서 백엔드별 차이를 흡수하는 지점은 `src/lib/apiClient.ts` 하나로 제한했습니다.

## 장바구니 동작
- 로그인 시: `Authorization: Bearer <token>`로 `/cart` 사용
- 비로그인 시: `x-cart-key` 헤더로 `/cart/guest` 사용

## 참고
- 상세 페이지의 장바구니 추가는 `sellerId`가 필요해서 입력 필드를 제공했습니다.
- 백엔드에서 상품별 seller 선택 API를 추가하면 입력 없이 자동화 가능합니다.

