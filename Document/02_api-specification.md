# 쇼핑몰 프로젝트 API 명세서

> Base URL: `/api/v1`
> 인증: `Authorization: Bearer {accessToken}`
> 공통 응답 형식: `{ success, data, meta?, error? }`
> 다국어: `Accept-Language: ko | en | ja` (기본값: ko)
> 다화폐: `X-Currency: KRW | USD | JPY` (기본값: KRW)

---

## 1. 인증 (Auth)

| Method | Endpoint | 설명 | 권한 | Request Body | Response |
|--------|----------|------|------|-------------|----------|
| POST | `/auth/signup` | 회원가입 (인증 메일 자동 발송) | Public | `{ email, password, name, phone }` | `{ id, email, name, message }` |
| POST | `/auth/verify-email` | 이메일 인증 확인 | Public | `{ email, code }` | `{ message, verified }` |
| POST | `/auth/resend-verification` | 인증 메일 재발송 | Public | `{ email }` | `{ message }` |
| POST | `/auth/login` | 로그인 (이메일 인증 완료 필수) | Public | `{ email, password }` | `{ accessToken, refreshToken, expiresIn }` |
| POST | `/auth/logout` | 로그아웃 | User | - | `{ message }` |
| POST | `/auth/refresh` | 토큰 갱신 | Public | `{ refreshToken }` | `{ accessToken, refreshToken, expiresIn }` |
| POST | `/auth/password-reset/request` | 비밀번호 재설정 요청 | Public | `{ email, phone }` | `{ message }` |
| POST | `/auth/password-reset/verify` | 재설정 인증코드 확인 | Public | `{ email, code }` | `{ resetToken }` |
| POST | `/auth/password-reset/confirm` | 새 비밀번호 설정 | Public | `{ resetToken, newPassword }` | `{ message }` |
| GET | `/auth/login/{provider}` | 소셜 로그인 리다이렉트 (google/naver/kakao/facebook/instagram) | Public | - | Redirect |
| GET | `/auth/callback/{provider}` | 소셜 인증 콜백 및 JWT 발급 | Public | - | `{ accessToken, refreshToken, isNewUser }` |
| POST | `/auth/social/complete` | 소셜 신규 회원 추가정보 입력 | Public | `{ phone, nickname }` | `{ accessToken, refreshToken }` |
| POST | `/auth/social/link` | 현재 계정에 소셜 계정 연동 | User | `{ provider, socialToken }` | `{ message, linkedProvider }` |
| DELETE | `/auth/social/unlink/{provider}` | 소셜 계정 연동 해제 | User | - | `{ message }` |

### 상세

#### POST `/auth/signup`
```
Request:
{
  "email": "user@example.com",       // 필수, 이메일 형식, 중복 불가
  "password": "Password1!",          // 필수, 최소 8자, 영문+숫자+특수문자
  "name": "홍길동",                   // 필수, 2~20자
  "phone": "010-1234-5678"           // 필수, 전화번호 형식
}

Response: 201 Created
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "홍길동",
    "message": "인증 메일이 발송되었습니다. 이메일을 확인해주세요."
  }
}

Errors:
- 409 DUPLICATE_EMAIL: 이미 사용 중인 이메일입니다.
- 400 VALIDATION_ERROR: 입력값 검증 실패

※ 회원가입 완료 시 이메일로 6자리 인증코드가 자동 발송됩니다.
   이메일 인증을 완료해야 로그인이 가능합니다.
```

#### POST `/auth/verify-email`
```
Request:
{
  "email": "user@example.com",       // 필수
  "code": "482931"                   // 필수, 6자리 인증코드
}

Response: 200 OK
{
  "success": true,
  "data": {
    "message": "이메일 인증이 완료되었습니다.",
    "verified": true
  }
}

Errors:
- 400 INVALID_VERIFICATION_CODE: 인증코드가 올바르지 않습니다.
- 410 VERIFICATION_CODE_EXPIRED: 인증코드가 만료되었습니다. 재발송해주세요.
- 429 VERIFICATION_ATTEMPT_EXCEEDED: 인증 시도 횟수를 초과했습니다. (최대 5회)
- 404 USER_NOT_FOUND: 해당 이메일로 등록된 계정이 없습니다.
- 409 ALREADY_VERIFIED: 이미 인증된 이메일입니다.
```

#### POST `/auth/resend-verification`
```
Request:
{
  "email": "user@example.com"        // 필수
}

Response: 200 OK
{
  "success": true,
  "data": {
    "message": "인증 메일이 재발송되었습니다."
  }
}

Errors:
- 404 USER_NOT_FOUND: 해당 이메일로 등록된 계정이 없습니다.
- 409 ALREADY_VERIFIED: 이미 인증된 이메일입니다.
- 429 RESEND_RATE_LIMITED: 1분 후 다시 시도해주세요.
```

#### POST `/auth/login`
```
Request:
{
  "email": "user@example.com",
  "password": "Password1!"
}

Response: 200 OK
{
  "success": true,
  "data": {
    "accessToken": "eyJhbG...",
    "refreshToken": "eyJhbG...",
    "expiresIn": 1800
  }
}

Errors:
- 401 INVALID_CREDENTIALS: 이메일 또는 비밀번호가 올바르지 않습니다.
- 403 EMAIL_NOT_VERIFIED: 이메일 인증이 완료되지 않았습니다. 인증 메일을 확인해주세요.
- 403 USER_BLOCKED: 차단된 계정입니다.
```

#### POST `/auth/password-reset/request`
```
Request:
{
  "email": "user@example.com",       // 필수
  "phone": "010-1234-5678"           // 필수, 가입 시 등록한 전화번호
}

Response: 200 OK
{
  "success": true,
  "data": {
    "message": "비밀번호 재설정 인증 메일이 발송되었습니다."
  }
}

Errors:
- 404 USER_NOT_FOUND: 해당 이메일로 등록된 계정이 없습니다.
- 400 PHONE_MISMATCH: 등록된 전화번호와 일치하지 않습니다.
- 429 RESEND_RATE_LIMITED: 1분 후 다시 시도해주세요.

※ 이메일과 전화번호가 모두 일치해야 인증 메일이 발송됩니다.
   보안을 위해 이메일/전화번호 불일치 시에도 동일한 응답 시간을 유지합니다.
```

#### POST `/auth/password-reset/verify`
```
Request:
{
  "email": "user@example.com",       // 필수
  "code": "573928"                   // 필수, 6자리 인증코드
}

Response: 200 OK
{
  "success": true,
  "data": {
    "resetToken": "eyJhbG..."        // 비밀번호 재설정용 일회성 토큰 (유효시간 5분)
  }
}

Errors:
- 400 INVALID_VERIFICATION_CODE: 인증코드가 올바르지 않습니다.
- 410 VERIFICATION_CODE_EXPIRED: 인증코드가 만료되었습니다.
- 429 VERIFICATION_ATTEMPT_EXCEEDED: 인증 시도 횟수를 초과했습니다. (최대 5회)
```

#### POST `/auth/password-reset/confirm`
```
Request:
{
  "resetToken": "eyJhbG...",         // 필수, verify에서 발급받은 토큰
  "newPassword": "NewPassword1!"     // 필수, 최소 8자, 영문+숫자+특수문자
}

Response: 200 OK
{
  "success": true,
  "data": {
    "message": "비밀번호가 성공적으로 변경되었습니다."
  }
}

Errors:
- 401 INVALID_RESET_TOKEN: 유효하지 않거나 만료된 재설정 토큰입니다.
- 400 SAME_PASSWORD: 기존 비밀번호와 동일한 비밀번호입니다.
- 400 VALIDATION_ERROR: 비밀번호 형식이 올바르지 않습니다.

※ 비밀번호 변경 완료 시 모든 기존 세션(Refresh Token)이 무효화됩니다.
```

#### GET `/auth/login/{provider}`
```
Path Parameters:
- provider: google | naver | kakao | facebook | instagram

Response: 302 Redirect
→ 해당 소셜 로그인 페이지로 리다이렉트 (state 토큰 포함)

Errors:
- 400 INVALID_PROVIDER: 지원하지 않는 소셜 로그인 공급자입니다.
```

#### GET `/auth/callback/{provider}`
```
Query Parameters:
- code: string (인증 서버 발급 코드)
- state: string (CSRF 방지 토큰)

Response: 200 OK
{
  "success": true,
  "data": {
    "accessToken": "eyJhbG...",
    "refreshToken": "eyJhbG...",
    "expiresIn": 1800,
    "isNewUser": false
  }
}

※ isNewUser가 true일 경우 /auth/social/complete로 추가정보 입력 필요

Errors:
- 401 SOCIAL_AUTH_FAILED: 소셜 인증에 실패했습니다.
- 400 INVALID_STATE: 유효하지 않은 state 토큰입니다. (CSRF 방지)
```

#### POST `/auth/social/link`
```
Request:
{
  "provider": "kakao",
  "socialToken": "..."
}

Response: 200 OK
{
  "success": true,
  "data": {
    "message": "카카오 계정이 연동되었습니다.",
    "linkedProvider": "kakao"
  }
}

Errors:
- 409 SOCIAL_ALREADY_LINKED: 이미 다른 계정에 연동된 소셜 계정입니다.
- 409 PROVIDER_ALREADY_LINKED: 이미 해당 소셜 서비스가 연동되어 있습니다.
```

#### DELETE `/auth/social/unlink/{provider}`
```
Response: 200 OK
{
  "success": true,
  "data": {
    "message": "소셜 계정 연동이 해제되었습니다."
  }
}

Errors:
- 400 CANNOT_UNLINK: 일반 로그인 수단이 없어 소셜 연동을 해제할 수 없습니다.
- 404 SOCIAL_NOT_LINKED: 해당 소셜 서비스가 연동되어 있지 않습니다.
```

---

## 2. 회원 (User)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/users/me` | 내 정보 조회 | User | - | `UserResponse` |
| PATCH | `/users/me` | 내 정보 수정 | User | `{ name?, phone?, password? }` | `UserResponse` |
| DELETE | `/users/me` | 회원 탈퇴 | User | - | `{ message }` |
| GET | `/users` | 회원 목록 | Admin | `?page&limit&search` | `UserResponse[]` |
| PATCH | `/users/:id/status` | 회원 상태 변경 | Admin | `{ status }` | `UserResponse` |
| GET | `/users/:id/profile` | 타인 프로필 조회 | Public | - | `ProfileResponse` |
| PATCH | `/users/me/profile` | 닉네임, 소개글 수정 | User | `{ nickname?, bio? }` | `ProfileResponse` |
| POST | `/users/me/profile-image` | 프로필 이미지 업로드/수정 | User | `multipart/form-data` | `{ imageUrl }` |
| DELETE | `/users/me/profile-image` | 프로필 이미지 삭제 (기본 이미지로) | User | - | `{ message }` |

### 상세

#### GET `/users/me`
```
Response: 200 OK
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "홍길동",
    "phone": "010-1234-5678",
    "role": "USER",
    "status": "ACTIVE",
    "point": 15000,
    "badges": [
      { "id": 1, "name": "리뷰 마스터", "iconUrl": "..." }
    ],
    "createdAt": "2026-01-01T00:00:00.000Z"
  }
}
```

#### GET `/users` (Admin)
```
Query Parameters:
- page: number (default: 1)
- limit: number (default: 20, max: 100)
- search: string (이메일, 이름 검색)
- status: ACTIVE | INACTIVE | BLOCKED
- role: USER | SELLER | ADMIN

Response: 200 OK
{
  "success": true,
  "data": [ UserResponse, ... ],
  "meta": { "page": 1, "limit": 20, "totalCount": 150, "totalPages": 8 }
}
```

---

## 3. 카테고리 (Category)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/categories` | 전체 목록 (트리) | Public | - | `CategoryTree[]` |
| GET | `/categories/:id` | 단일 조회 | Public | - | `Category` |
| POST | `/categories` | 생성 | Admin | `{ name, parentId? }` | `Category` |
| PATCH | `/categories/:id` | 수정 | Admin | `{ name?, sortOrder? }` | `Category` |
| DELETE | `/categories/:id` | 삭제 | Admin | - | `{ message }` |

### 상세

#### GET `/categories`
```
Response: 200 OK
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "노트북",
      "sortOrder": 1,
      "children": [
        {
          "id": 2,
          "name": "게이밍 노트북",
          "sortOrder": 1,
          "children": []
        }
      ]
    }
  ]
}
```

---

## 4. 상품 (Product)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/products` | 상품 목록 | Public | QueryParams | `ProductSummary[]` |
| GET | `/products/:id` | 상품 상세 | Public | - | `ProductDetail` |
| POST | `/products` | 상품 등록 | Admin | Body | `ProductDetail` |
| PATCH | `/products/:id` | 상품 수정 | Admin | Body | `ProductDetail` |
| DELETE | `/products/:id` | 상품 삭제 | Admin | - | `{ message }` |
| POST | `/products/:id/options` | 옵션 추가 | Admin | Body | `ProductOption` |
| PATCH | `/products/:id/options/:optionId` | 옵션 수정 | Admin | Body | `ProductOption` |
| DELETE | `/products/:id/options/:optionId` | 옵션 삭제 | Admin | - | `{ message }` |
| POST | `/products/:id/images` | 이미지 업로드 | Admin | multipart | `ProductImage` |
| DELETE | `/products/:id/images/:imageId` | 이미지 삭제 | Admin | - | `{ message }` |

### 상세

#### GET `/products`
```
Query Parameters:
- page: number (default: 1)
- limit: number (default: 20, max: 100)
- categoryId: number (카테고리 필터)
- search: string (상품명 검색)
- minPrice: number
- maxPrice: number
- sort: "price_asc" | "price_desc" | "newest" | "popularity" | "rating" (default: "newest")
- specs: string (스펙 필터, JSON 형식 "{"cpu":"i7","ram":"16GB"}")

Response: 200 OK
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "삼성 갤럭시북4 프로",
      "lowestPrice": 1590000,
      "sellerCount": 12,
      "thumbnailUrl": "/uploads/products/thumb_1.jpg",
      "reviewCount": 85,
      "averageRating": 4.5,
      "priceDiff": -50000,
      "priceDiffPercent": -3.05,
      "createdAt": "2026-01-15T00:00:00.000Z"
    }
  ],
  "meta": { "page": 1, "limit": 20, "totalCount": 85, "totalPages": 5 }
}
```

#### 정렬 파라미터 (`sort`)
상품 목록 및 검색 결과 API에서 사용 가능한 정렬 옵션:
- `popularity`: 인기순 (기본값) - (조회수×0.3 + 리뷰수×0.5 + 판매량×0.2)
- `price_asc`: 낮은 가격순 (최저가 기준)
- `price_desc`: 높은 가격순 (최저가 기준)
- `rating_desc`: 평점 높은순 (동일 평점 시 리뷰수 우선)
- `rating_asc`: 평점 낮은순

#### GET `/products/:id`
```
Response: 200 OK
{
  "success": true,
  "data": {
    "id": 1,
    "name": "삼성 갤럭시북4 프로",
    "description": "14인치 AMOLED 고성능 노트북",
    "lowestPrice": 1590000,
    "highestPrice": 1890000,
    "averagePrice": 1720000,
    "stock": 150,
    "status": "ON_SALE",
    "category": { "id": 2, "name": "게이밍 노트북" },
    "options": [
      { "id": 1, "name": "색상", "values": ["실버", "그라파이트"] },
      { "id": 2, "name": "저장공간", "values": ["256GB", "512GB", "1TB"] }
    ],
    "images": [
      { "id": 1, "url": "/uploads/products/1_main.jpg", "isMain": true, "sortOrder": 1 }
    ],
    "specs": [
      { "name": "CPU", "value": "Intel Core i7-1360P" },
      { "name": "RAM", "value": "16GB" },
      { "name": "디스플레이", "value": "14인치 AMOLED 2880x1800" }
    ],
    "priceEntries": [
      { "seller": { "id": 1, "name": "쿠팡", "logoUrl": "...", "trustScore": 95 }, "price": 1590000, "url": "...", "shipping": "무료배송" },
      { "seller": { "id": 2, "name": "11번가", "logoUrl": "...", "trustScore": 90 }, "price": 1650000, "url": "...", "shipping": "무료배송" }
    ],
    "reviewCount": 85,
    "averageRating": 4.5,
    "createdAt": "2026-01-15T00:00:00.000Z"
  }
}
```

---

## 5. 스펙 (Spec)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/specs/definitions` | 카테고리별 스펙 정의 목록 | Public | `?categoryId` | `SpecDefinition[]` |
| POST | `/specs/definitions` | 스펙 정의 생성 | Admin | Body | `SpecDefinition` |
| PATCH | `/specs/definitions/:id` | 스펙 정의 수정 | Admin | Body | `SpecDefinition` |
| DELETE | `/specs/definitions/:id` | 스펙 정의 삭제 | Admin | - | `{ message }` |
| PUT | `/products/:id/specs` | 상품 스펙 값 설정 | Admin | Body | `ProductSpec[]` |
| GET | `/products/:id/specs` | 상품 스펙 조회 | Public | - | `ProductSpec[]` |
| POST | `/specs/compare` | 상품 스펙 비교 | Public | Body | `CompareResult` |
| POST | `/specs/compare/scored` | 점수화 스펙 비교 | Public | Body | `ScoredCompareResult` |
| PUT | `/specs/scores/:specDefId` | 스펙 점수 매핑 설정 | Admin | Body | `SpecScore[]` |

### 상세

#### GET `/specs/definitions?categoryId=2`
```
Response: 200 OK
{
  "success": true,
  "data": [
    { "id": 1, "name": "CPU", "type": "SELECT", "options": ["i5", "i7", "i9", "Ryzen 5", "Ryzen 7"], "unit": null },
    { "id": 2, "name": "RAM", "type": "SELECT", "options": ["8GB", "16GB", "32GB", "64GB"], "unit": "GB" },
    { "id": 3, "name": "무게", "type": "NUMBER", "options": null, "unit": "kg" }
  ]
}
```

#### POST `/specs/compare`
```
Request:
{
  "productIds": [1, 2, 3]       // 필수, 2~4개
}

Response: 200 OK
{
  "success": true,
  "data": {
    "products": [
      { "id": 1, "name": "갤럭시북4 프로", "thumbnailUrl": "...", "lowestPrice": 1590000 },
      { "id": 2, "name": "맥북 프로 14", "thumbnailUrl": "...", "lowestPrice": 2390000 },
      { "id": 3, "name": "LG 그램 14", "thumbnailUrl": "...", "lowestPrice": 1490000 }
    ],
    "specs": [
      { "name": "CPU", "values": ["i7-1360P", "M3 Pro", "i7-1360P"] },
      { "name": "RAM", "values": ["16GB", "18GB", "16GB"] },
      { "name": "무게", "values": ["1.23kg", "1.55kg", "0.99kg"] }
    ]
  }
}
```

#### POST `/specs/compare/scored`
```
Request:
{
  "productIds": [1, 2, 3],
  "weights": { "CPU": 30, "RAM": 25, "GPU": 25, "무게": 20 }  // 선택, 가중치 (합계 100)
}

Response: 200 OK
{
  "success": true,
  "data": {
    "products": [
      { "id": 1, "name": "갤럭시북4 프로", "totalScore": 82, "rank": 2 },
      { "id": 2, "name": "맥북 프로 14", "totalScore": 91, "rank": 1 },
      { "id": 3, "name": "LG 그램 14", "totalScore": 75, "rank": 3 }
    ],
    "specScores": [
      { "name": "CPU", "scores": [78, 92, 78], "winner": 2 },
      { "name": "RAM", "scores": [70, 80, 70], "winner": 2 },
      { "name": "GPU", "scores": [85, 95, 60], "winner": 2 },
      { "name": "무게", "scores": [90, 70, 98], "winner": 3 }
    ],
    "recommendation": "맥북 프로 14이(가) 종합 점수가 가장 높습니다."
  }
}
```

---

## 6. 판매처 (Seller)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/sellers` | 판매처 목록 | Public | `?page&limit` | `Seller[]` |
| GET | `/sellers/:id` | 판매처 상세 | Public | - | `Seller` |
| POST | `/sellers` | 판매처 등록 | Admin | Body | `Seller` |
| PATCH | `/sellers/:id` | 판매처 수정 | Admin | Body | `Seller` |
| DELETE | `/sellers/:id` | 판매처 삭제 | Admin | - | `{ message }` |

### 상세

#### POST `/sellers`
```
Request:
{
  "name": "쿠팡",                    // 필수
  "url": "https://www.coupang.com", // 필수
  "logoUrl": "/uploads/sellers/coupang.png",
  "description": "로켓배송"
}

Response: 201 Created
```

---

## 7. 가격비교 / 가격추이 (Price)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/products/:id/prices` | 판매처별 가격비교 | Public | - | `PriceEntry[]` |
| POST | `/products/:id/prices` | 판매처 가격 등록 | Seller, Admin | Body | `PriceEntry` |
| PATCH | `/prices/:id` | 가격 수정 | Seller, Admin | Body | `PriceEntry` |
| DELETE | `/prices/:id` | 가격 삭제 | Admin | - | `{ message }` |
| GET | `/products/:id/price-history` | 가격 추이 | Public | QueryParams | `PriceHistory[]` |
| GET | `/price-alerts` | 내 알림 목록 | User | - | `PriceAlert[]` |
| POST | `/price-alerts` | 최저가 알림 등록 | User | Body | `PriceAlert` |
| DELETE | `/price-alerts/:id` | 알림 삭제 | User | - | `{ message }` |

### 상세

#### GET `/products/:id/prices`
```
Response: 200 OK
{
  "success": true,
  "data": {
    "lowestPrice": 1590000,
    "averagePrice": 1720000,
    "highestPrice": 1890000,
    "entries": [
      {
        "id": 1,
        "seller": { "id": 1, "name": "쿠팡", "logoUrl": "...", "trustScore": 95 },
        "price": 1590000,
        "shippingCost": 0,
        "shippingInfo": "로켓배송 (무료)",
        "productUrl": "https://www.coupang.com/...",
        "updatedAt": "2026-02-11T08:00:00.000Z"
      }
    ]
  }
}
```

#### GET `/products/:id/price-history`
```
Query Parameters:
- period: "1w" | "1m" | "3m" | "6m" | "1y" (default: "3m")
- type: "daily" | "weekly" | "monthly" (default: "daily")

Response: 200 OK
{
  "success": true,
  "data": {
    "productId": 1,
    "productName": "삼성 갤럭시북4 프로",
    "allTimeLowest": 1490000,
    "allTimeHighest": 1990000,
    "history": [
      { "date": "2026-02-10", "lowestPrice": 1590000, "averagePrice": 1720000 },
      { "date": "2026-02-09", "lowestPrice": 1610000, "averagePrice": 1730000 }
    ]
  }
}
```

#### POST `/price-alerts`
```
Request:
{
  "productId": 1,          // 필수
  "targetPrice": 1500000   // 필수, 목표 가격
}

Response: 201 Created
{
  "success": true,
  "data": {
    "id": 1,
    "productId": 1,
    "productName": "삼성 갤럭시북4 프로",
    "targetPrice": 1500000,
    "currentLowestPrice": 1590000,
    "isTriggered": false,
    "createdAt": "2026-02-11T10:00:00.000Z"
  }
}

Errors:
- 409 ALERT_EXISTS: 해당 상품에 대한 알림이 이미 존재합니다.
```

---

## 8. 장바구니 (Cart)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/cart` | 장바구니 조회 | User | - | `CartItem[]` |
| POST | `/cart` | 항목 추가 | User | Body | `CartItem` |
| PATCH | `/cart/:itemId` | 수량 변경 | User | `{ quantity }` | `CartItem` |
| DELETE | `/cart/:itemId` | 항목 삭제 | User | - | `{ message }` |
| DELETE | `/cart` | 전체 비우기 | User | - | `{ message }` |

### 상세

#### POST `/cart`
```
Request:
{
  "productId": 1,                  // 필수
  "sellerId": 1,                   // 필수, 구매할 판매처
  "quantity": 2,                   // 필수, 1 이상
  "selectedOptions": "실버 / 512GB"  // 선택
}

Response: 201 Created
```

---

## 9. 배송지 (Address)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/addresses` | 배송지 목록 | User | - | `Address[]` |
| POST | `/addresses` | 배송지 추가 | User | Body | `Address` |
| PATCH | `/addresses/:id` | 배송지 수정 | User | Body | `Address` |
| DELETE | `/addresses/:id` | 배송지 삭제 | User | - | `{ message }` |

---

## 10. 주문 (Order)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| POST | `/orders` | 주문 생성 | User | Body | `OrderDetail` |
| GET | `/orders` | 내 주문 목록 | User | QueryParams | `OrderSummary[]` |
| GET | `/orders/:id` | 주문 상세 | User | - | `OrderDetail` |
| POST | `/orders/:id/cancel` | 주문 취소 | User | - | `OrderDetail` |
| GET | `/admin/orders` | 전체 주문 관리 | Admin | QueryParams | `OrderSummary[]` |
| PATCH | `/admin/orders/:id/status` | 주문 상태 변경 | Admin | `{ status }` | `OrderDetail` |

### 상세

#### POST `/orders`
```
Request:
{
  "addressId": 1,
  "items": [
    { "productId": 1, "sellerId": 1, "quantity": 2, "selectedOptions": "실버 / 512GB" }
  ],
  "fromCart": true,
  "cartItemIds": [1, 2, 3],
  "usePoint": 5000,                    // 선택, 사용 포인트
  "memo": "부재 시 문 앞에 놓아주세요"
}

→ Transaction Isolation: SERIALIZABLE (재고 차감 + 포인트 사용 동시성 보호)

Response: 201 Created
{
  "success": true,
  "data": {
    "id": 1,
    "orderNumber": "ORD-20260211-A3F9K2",
    "status": "ORDER_PLACED",
    "items": [...],
    "totalAmount": 3180000,
    "pointUsed": 5000,
    "finalAmount": 3175000,
    "shippingAddress": { ... },
    "createdAt": "2026-02-11T10:00:00.000Z"
  }
}
```

---

## 11. 결제 (Payment)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| POST | `/payments` | 결제 요청 | User | Body | `Payment` |
| GET | `/payments/:id` | 결제 상세 | User | - | `Payment` |
| POST | `/payments/:id/refund` | 환불 요청 | User | - | `Payment` |

---

## 12. 리뷰 (Review)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/products/:productId/reviews` | 상품 리뷰 목록 | Public | QueryParams | `Review[]` |
| POST | `/products/:productId/reviews` | 리뷰 작성 | User | Body | `Review` |
| PATCH | `/reviews/:id` | 리뷰 수정 | User | Body | `Review` |
| DELETE | `/reviews/:id` | 리뷰 삭제 | User, Admin | - | `{ message }` |

### 상세

#### POST `/products/:productId/reviews`
```
Request:
{
  "orderId": 1,
  "rating": 5,
  "content": "화면이 정말 선명하고 가볍습니다!"
}

Response: 201 Created
→ 작성 완료 시 500P 자동 적립
→ 배지 체크: 리뷰 10개 이상 시 "리뷰 마스터" 배지 자동 부여
```

---

## 13. 위시리스트 (Wishlist)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/wishlist` | 위시리스트 조회 | User | `?page&limit` | `WishlistItem[]` |
| POST | `/wishlist/:productId` | 찜하기 (토글) | User | - | `{ wishlisted: boolean }` |
| DELETE | `/wishlist/:productId` | 찜 해제 | User | - | `{ message }` |

---

## 14. 포인트 (Point)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/points/balance` | 포인트 잔액 | User | - | `{ balance }` |
| GET | `/points/transactions` | 포인트 내역 | User | QueryParams | `PointTransaction[]` |
| POST | `/admin/points/grant` | 관리자 포인트 지급 | Admin | Body | `PointTransaction` |

### 상세

#### GET `/points/balance`
```
Response: 200 OK
{
  "success": true,
  "data": {
    "balance": 15000,
    "expiringSoon": 3000,
    "expiringDate": "2026-03-01"
  }
}
```

#### GET `/points/transactions`
```
Query Parameters:
- page: number (default: 1)
- limit: number (default: 20)
- type: "EARN" | "USE" | "REFUND" | "EXPIRE" | "ADMIN_GRANT" (필터)

→ Transaction Isolation: REPEATABLE READ (포인트 잔액 정합성)

Response: 200 OK
{
  "success": true,
  "data": [
    { "id": 1, "type": "EARN", "amount": 31750, "balance": 46750, "description": "주문 확정 적립 (ORD-20260211-A3F9K2)", "createdAt": "..." },
    { "id": 2, "type": "EARN", "amount": 500, "balance": 15000, "description": "리뷰 작성 적립", "createdAt": "..." }
  ],
  "meta": { ... }
}
```

---

## 15. 커뮤니티 / 게시판 (Community)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/boards` | 게시판 목록 | Public | - | `Board[]` |
| GET | `/boards/:boardId/posts` | 게시글 목록 | Public | QueryParams | `PostSummary[]` |
| GET | `/posts/:id` | 게시글 상세 | Public | - | `PostDetail` |
| POST | `/boards/:boardId/posts` | 게시글 작성 | User | Body | `PostDetail` |
| PATCH | `/posts/:id` | 게시글 수정 | User | Body | `PostDetail` |
| DELETE | `/posts/:id` | 게시글 삭제 | User, Admin | - | `{ message }` |
| POST | `/posts/:id/like` | 좋아요 토글 | User | - | `{ liked: boolean, likeCount: number }` |
| GET | `/posts/:id/comments` | 댓글 목록 | Public | - | `Comment[]` |
| POST | `/posts/:id/comments` | 댓글 작성 | User | Body | `Comment` |
| DELETE | `/comments/:id` | 댓글 삭제 | User, Admin | - | `{ message }` |

### 상세

#### GET `/boards/:boardId/posts`
```
Query Parameters:
- page, limit
- search: string (제목+내용 검색)
- sort: "newest" | "popular" | "most_commented" (default: "newest")

Response: 200 OK
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "갤럭시북4 프로 3개월 사용후기",
      "author": { "id": 1, "name": "홍길동", "badges": [...] },
      "viewCount": 1520,
      "likeCount": 45,
      "commentCount": 12,
      "createdAt": "2026-02-10T..."
    }
  ],
  "meta": { ... }
}
```

---

## 16. 상품 문의 (Inquiry)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/products/:productId/inquiries` | 상품 문의 목록 | Public | QueryParams | `Inquiry[]` |
| POST | `/products/:productId/inquiries` | 문의 작성 | User | Body | `Inquiry` |
| POST | `/inquiries/:id/answer` | 문의 답변 | Seller, Admin | Body | `Inquiry` |
| GET | `/inquiries/me` | 내 문의 목록 | User | QueryParams | `Inquiry[]` |
| DELETE | `/inquiries/:id` | 문의 삭제 | User | - | `{ message }` |

### 상세

#### POST `/products/:productId/inquiries`
```
Request:
{
  "title": "배터리 용량이 어떻게 되나요?",
  "content": "공식 스펙에는 없어서 문의드립니다.",
  "isSecret": false
}

Response: 201 Created
```

---

## 17. 고객센터 (Support)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/support/tickets` | 내 문의 목록 | User | QueryParams | `Ticket[]` |
| POST | `/support/tickets` | 1:1 문의 작성 | User | Body (multipart) | `Ticket` |
| GET | `/support/tickets/:id` | 문의 상세 (스레드) | User | - | `TicketDetail` |
| POST | `/support/tickets/:id/reply` | 답변 작성 | User, Admin | Body | `TicketReply` |
| GET | `/admin/support/tickets` | 전체 문의 관리 | Admin | QueryParams | `Ticket[]` |
| PATCH | `/admin/support/tickets/:id/status` | 문의 상태 변경 | Admin | `{ status }` | `Ticket` |

### 상세

#### POST `/support/tickets`
```
Request (multipart/form-data):
{
  "category": "ORDER",               // ORDER | PAYMENT | DELIVERY | ACCOUNT | OTHER
  "title": "결제 오류 문의",
  "content": "카드 결제 시 오류가 발생합니다.",
  "attachments": [File, File]         // 선택, 첨부파일 (최대 3개, 5MB)
}

Response: 201 Created
{
  "success": true,
  "data": {
    "id": 1,
    "ticketNumber": "TK-20260211-001",
    "category": "ORDER",
    "title": "결제 오류 문의",
    "status": "OPEN",
    "createdAt": "..."
  }
}
```

---

## 18. FAQ / 공지사항 (Help)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/faqs` | FAQ 목록 | Public | `?category&search` | `FAQ[]` |
| POST | `/faqs` | FAQ 등록 | Admin | Body | `FAQ` |
| PATCH | `/faqs/:id` | FAQ 수정 | Admin | Body | `FAQ` |
| DELETE | `/faqs/:id` | FAQ 삭제 | Admin | - | `{ message }` |
| GET | `/notices` | 공지사항 목록 | Public | `?page&limit` | `Notice[]` |
| GET | `/notices/:id` | 공지사항 상세 | Public | - | `Notice` |
| POST | `/notices` | 공지사항 등록 | Admin | Body | `Notice` |
| PATCH | `/notices/:id` | 공지사항 수정 | Admin | Body | `Notice` |
| DELETE | `/notices/:id` | 공지사항 삭제 | Admin | - | `{ message }` |

---

## 19. 활동 내역 (Activity)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/activity/views` | 최근 본 상품 | User | `?page&limit` | `ViewHistory[]` |
| DELETE | `/activity/views` | 전체 삭제 | User | - | `{ message }` |
| GET | `/activity/searches` | 검색 기록 | User | - | `SearchHistory[]` |
| DELETE | `/activity/searches` | 전체 삭제 | User | - | `{ message }` |
| DELETE | `/activity/searches/:id` | 개별 삭제 | User | - | `{ message }` |

---

## 20. 실시간 채팅 (Chat)

### REST API

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| POST | `/chat/rooms` | 채팅방 생성 | User | - | `ChatRoom` |
| GET | `/chat/rooms` | 채팅방 목록 | User, Admin | - | `ChatRoom[]` |
| GET | `/chat/rooms/:id/messages` | 메시지 기록 | User, Admin | `?page&limit` | `ChatMessage[]` |
| PATCH | `/chat/rooms/:id/close` | 채팅방 종료 | User, Admin | - | `ChatRoom` |

### WebSocket 이벤트 (Socket.IO)

| 이벤트 | 방향 | Payload | 설명 |
|--------|------|---------|------|
| `joinRoom` | Client → Server | `{ roomId }` | 채팅방 입장 |
| `leaveRoom` | Client → Server | `{ roomId }` | 채팅방 퇴장 |
| `sendMessage` | Client → Server | `{ roomId, content }` | 메시지 전송 |
| `newMessage` | Server → Client | `{ id, senderId, content, createdAt }` | 새 메시지 수신 |
| `messageRead` | Client → Server | `{ roomId, messageId }` | 읽음 처리 |
| `readReceipt` | Server → Client | `{ roomId, messageId, readBy }` | 읽음 알림 |
| `typing` | Client → Server | `{ roomId }` | 타이핑 표시 |
| `userTyping` | Server → Client | `{ roomId, userId }` | 상대방 타이핑 중 |
| `priceAlert` | Server → Client | `{ productId, productName, targetPrice, currentPrice }` | 최저가 알림 |

---

## 21. 랭킹 / 인기차트 (Ranking)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/rankings/products` | 인기 상품 랭킹 | Public | QueryParams | `RankedProduct[]` |
| GET | `/rankings/searches` | 실시간 인기 검색어 | Public | - | `SearchRank[]` |
| GET | `/rankings/price-drops` | 가격 하락 랭킹 | Public | QueryParams | `PriceDropProduct[]` |

### 상세

#### GET `/rankings/products`
```
Query Parameters:
- categoryId: number (카테고리별)
- period: "daily" | "weekly" | "monthly" (default: "daily")
- limit: number (default: 20, max: 100)

Response: 200 OK
{
  "success": true,
  "data": [
    {
      "rank": 1,
      "rankChange": 2,
      "product": { "id": 1, "name": "삼성 갤럭시북4 프로", "lowestPrice": 1590000, "thumbnailUrl": "..." },
      "score": 15230
    }
  ]
}
```

#### GET `/rankings/searches`
```
Response: 200 OK
{
  "success": true,
  "data": [
    { "rank": 1, "keyword": "갤럭시북", "searchCount": 5230, "rankChange": 0 },
    { "rank": 2, "keyword": "맥북 프로", "searchCount": 4120, "rankChange": 1 }
  ]
}
```

---

## 22. 추천 (Recommendation)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/recommendations/today` | 오늘의 추천 상품 | Public | - | `ProductSummary[]` |
| GET | `/recommendations/personalized` | 맞춤 추천 | User | `?limit` | `ProductSummary[]` |
| POST | `/admin/recommendations` | 추천 상품 설정 | Admin | Body | `Recommendation` |
| DELETE | `/admin/recommendations/:id` | 추천 해제 | Admin | - | `{ message }` |
| GET | `/admin/recommendations` | 추천 관리 목록 | Admin | - | `Recommendation[]` |

---

## 23. 특가 세일 (Deal)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/deals` | 진행 중인 특가 목록 | Public | `?type` | `Deal[]` |
| GET | `/deals/:id` | 특가 상세 | Public | - | `DealDetail` |
| POST | `/deals` | 특가 생성 | Admin | Body | `Deal` |
| PATCH | `/deals/:id` | 특가 수정 | Admin | Body | `Deal` |
| DELETE | `/deals/:id` | 특가 삭제 | Admin | - | `{ message }` |

### 상세

#### POST `/deals`
```
Request:
{
  "title": "봄맞이 노트북 특가전",
  "type": "SPECIAL",
  "description": "최대 30% 할인!",
  "discountRate": 15,
  "startDate": "2026-02-10T00:00:00.000Z",
  "endDate": "2026-02-17T23:59:59.000Z",
  "bannerUrl": "/uploads/deals/spring_sale.jpg",
  "products": [
    { "productId": 1, "dealPrice": 1350000, "stock": 50 },
    { "productId": 2, "dealPrice": 2090000, "stock": 30 }
  ]
}

Response: 201 Created
```

---

## 24. 파일 업로드 (Upload)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| POST | `/upload/image` | 이미지 업로드 (레거시) | User, Admin | multipart/form-data | `{ url }` |

---

## 25. 통합 검색 (Search) — Elasticsearch

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/search` | 통합 검색 | Public | QueryParams | `SearchResult` |
| GET | `/search/autocomplete` | 자동완성 | Public | `?q` | `AutocompleteResult` |
| GET | `/search/popular` | 인기 검색어 | Public | `?limit` | `PopularKeyword[]` |
| POST | `/search/recent` | 최근 검색어 저장 | User | `{ keyword }` | `{ id, keyword, createdAt }` |
| GET | `/search/recent` | 최근 검색어 조회 | User | - | `RecentKeyword[]` |
| DELETE | `/search/recent/:id` | 최근 검색어 개별 삭제 | User | - | `{ message }` |
| DELETE | `/search/recent` | 최근 검색어 전체 삭제 | User | - | `{ message }` |
| PATCH | `/search/preferences` | 최근 검색어 자동 저장 설정 | User | `{ saveRecentSearches }` | `{ saveRecentSearches }` |
| GET | `/search/admin/weights` | 검색 가중치 조회 | Admin | - | `SearchWeightConfig` |
| PATCH | `/search/admin/weights` | 검색 가중치 수정 | Admin | `{ nameWeight, keywordWeight, clickWeight }` | `SearchWeightConfig` |
| GET | `/search/admin/index/status` | 검색 인덱스 상태 조회 | Admin | - | `IndexStatusResponse` |
| POST | `/search/admin/index/reindex` | 전체 재색인 실행 | Admin | - | `{ message, queued }` |
| POST | `/search/admin/index/products/:id/reindex` | 단일 상품 재색인 실행 | Admin | - | `{ message, productId }` |
| GET | `/search/admin/index/outbox/summary` | 인덱스 동기화 Outbox 요약 | Admin | - | `OutboxSummary` |
| POST | `/search/admin/index/outbox/requeue-failed` | 실패 Outbox 재큐잉 | Admin | `?limit` | `{ requeuedCount }` |

### 상세

#### GET `/search`
```
Query Parameters:
- q: string (필수, 검색 키워드)
- categoryId: number (카테고리 필터)
- minPrice: number
- maxPrice: number
- specs: string (JSON 스펙 필터)
- sort: "relevance" | "price_asc" | "price_desc" | "newest" | "popularity" (default: "relevance")
- page: number (default: 1)
- limit: number (default: 20)

Response: 200 OK
{
  "success": true,
  "data": {
    "hits": [
      {
        "id": 1,
        "name": "삼성 <em>갤럭시북</em>4 프로",
        "lowestPrice": 1590000,
        "thumbnailUrl": "...",
        "categoryName": "노트북",
        "score": 15.23
      }
    ],
    "facets": {
      "categories": [
        { "id": 2, "name": "게이밍 노트북", "count": 35 },
        { "id": 3, "name": "울트라북", "count": 28 }
      ],
      "priceRanges": [
        { "label": "50만원 이하", "min": 0, "max": 500000, "count": 12 },
        { "label": "50~100만원", "min": 500000, "max": 1000000, "count": 45 },
        { "label": "100~200만원", "min": 1000000, "max": 2000000, "count": 38 }
      ],
      "specs": {
        "CPU": [
          { "value": "i7", "count": 25 },
          { "value": "i5", "count": 18 }
        ]
      }
    },
    "suggestions": ["갤럭시북4", "갤럭시북3"],
    "totalCount": 85
  },
  "meta": { "page": 1, "limit": 20, "totalPages": 5 }
}
```

#### GET `/search/autocomplete?q=갤럭시`
```
Response: 200 OK
{
  "success": true,
  "data": {
    "keywords": ["갤럭시북4 프로", "갤럭시북3", "갤럭시 탭 S9"],
    "products": [
      { "id": 1, "name": "삼성 갤럭시북4 프로", "thumbnailUrl": "...", "lowestPrice": 1590000 }
    ],
    "categories": [
      { "id": 2, "name": "노트북 > 삼성 갤럭시북" }
    ]
  }
}
```

#### POST `/search/admin/index/reindex`
```
Response: 201 Created
{
  "success": true,
  "data": {
    "message": "전체 재색인 작업이 큐에 등록되었습니다.",
    "queued": true
  }
}
```

---

## 26. 크롤링 / 데이터 파이프라인 (Crawler) — Admin

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/crawler/admin/jobs` | 크롤러 작업 목록 | Admin | `?status&page&limit` | `CrawlerJob[]` |
| POST | `/crawler/admin/jobs` | 크롤러 작업 생성 | Admin | `CreateCrawlerJobDto` | `CrawlerJob` |
| PATCH | `/crawler/admin/jobs/:id` | 작업 수정 | Admin | `UpdateCrawlerJobDto` | `CrawlerJob` |
| DELETE | `/crawler/admin/jobs/:id` | 작업 삭제 | Admin | - | `{ message }` |
| POST | `/crawler/admin/jobs/:id/run` | 작업 단위 수동 실행 | Admin | - | `{ message, runId }` |
| POST | `/crawler/admin/triggers` | 판매처/상품 수동 트리거 | Admin | `TriggerCrawlerDto` | `{ message, runId }` |
| GET | `/crawler/admin/runs` | 실행 이력 조회 | Admin | `?status&jobId&page&limit` | `CrawlerRun[]` |
| GET | `/crawler/admin/monitoring` | 큐/수집 모니터링 요약 | Admin | - | `CrawlerMonitoringSummary` |

### 상세

#### POST `/crawler/admin/jobs`
```
Request:
{
  "name": "쿠팡 노트북 크롤러",
  "sellerId": 1,                          // 대상 판매처
  "targetUrl": "https://www.coupang.com/np/categories/...",
  "categoryId": 2,                        // 매핑할 카테고리
  "schedule": "0 */6 * * *",             // Cron 표현식 (6시간마다)
  "parserType": "COUPANG",               // 파서 유형
  "config": {                            // 파서별 추가 설정
    "maxPages": 10,
    "timeout": 30000
  }
}

Response: 201 Created
{
  "success": true,
  "data": {
    "id": 1,
    "name": "쿠팡 노트북 크롤러",
    "status": "IDLE",
    "schedule": "0 */6 * * *",
    "lastRunAt": null,
    "isActive": true,
    "createdAt": "..."
  }
}
```

#### GET `/crawler/admin/runs`
```
Response: 200 OK
{
  "success": true,
  "data": [
    {
      "id": 1,
      "status": "SUCCESS",
      "startedAt": "2026-02-11T06:00:00.000Z",
      "finishedAt": "2026-02-11T06:02:35.000Z",
      "duration": 155000,
      "itemsProcessed": 245,
      "itemsCreated": 12,
      "itemsUpdated": 230,
      "itemsFailed": 3,
      "errorMessage": null
    }
  ]
}
```

---

## 27. 가격 변동 예측 (Prediction)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/predictions/products/:productId/price-trend` | 상품 가격 추세 예측 조회 | Public | `?days` | `PricePrediction` |

### 상세

#### GET `/predictions/products/:productId/price-trend`
```
Query Parameters:
- days: number (default: 30, max: 90, 예측 일수)

Response: 200 OK
{
  "success": true,
  "data": {
    "productId": 1,
    "productName": "삼성 갤럭시북4 프로",
    "currentPrice": 1590000,
    "predictions": [
      { "date": "2026-02-12", "predictedPrice": 1585000, "confidence": 0.85 },
      { "date": "2026-02-13", "predictedPrice": 1580000, "confidence": 0.82 },
      { "date": "2026-02-14", "predictedPrice": 1575000, "confidence": 0.78 }
    ],
    "trend": "FALLING",                   // RISING | FALLING | STABLE
    "trendStrength": 0.72,                // 0~1 추세 강도
    "movingAverage7d": 1605000,
    "movingAverage30d": 1640000,
    "seasonalityNote": "연말/연초 가격 하락 패턴 감지",
    "recommendation": "BUY_SOON",         // BUY_NOW | BUY_SOON | WAIT | HOLD
    "recommendationReason": "향후 2주간 추가 하락이 예상됩니다. 최저가 근접 시 구매를 권장합니다.",
    "updatedAt": "2026-02-11T06:00:00.000Z"
  }
}
```

---

## 28. 브라우저 푸시 알림 (Push)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| POST | `/push/subscribe` | 푸시 구독 등록 | User | Body | `{ subscriptionId }` |
| DELETE | `/push/subscribe` | 푸시 구독 해제 | User | Body | `{ message }` |
| GET | `/push/notifications` | 알림 내역 | User | `?page&limit` | `Notification[]` |
| PATCH | `/push/notifications/:id/read` | 읽음 처리 | User | - | `{ message }` |
| PATCH | `/push/notifications/read-all` | 전체 읽음 | User | - | `{ message }` |
| POST | `/admin/push/send` | 관리자 푸시 발송 | Admin | Body | `{ sentCount }` |
| GET | `/admin/push/stats` | 푸시 발송 통계 | Admin | QueryParams | `PushStats` |

### 상세

#### POST `/push/subscribe`
```
Request:
{
  "subscription": {
    "endpoint": "https://fcm.googleapis.com/fcm/send/...",
    "keys": {
      "p256dh": "BNcRd...",
      "auth": "tBHI..."
    }
  },
  "deviceName": "Chrome - Windows"       // 선택
}

Response: 201 Created
{
  "success": true,
  "data": {
    "subscriptionId": 1,
    "deviceName": "Chrome - Windows",
    "createdAt": "..."
  }
}
```

#### POST `/admin/push/send`
```
Request:
{
  "title": "특가 세일 시작!",
  "body": "봄맞이 노트북 최대 30% 할인",
  "url": "/deals/1",
  "iconUrl": "/icons/sale.png",
  "target": "ALL",                        // ALL | SEGMENT
  "segmentFilter": {                      // target=SEGMENT일 때
    "hasWishlistIn": [2],                  // 카테고리 2 위시리스트 보유자
    "lastActiveWithin": "30d"
  },
  "scheduledAt": "2026-02-12T09:00:00.000Z"  // 선택, 예약 발송
}

Response: 200 OK
{
  "success": true,
  "data": {
    "sentCount": 1523,
    "scheduledAt": null
  }
}
```

---

## 29. 판매처 신뢰도 (Trust)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/sellers/:id/trust` | 판매처 신뢰도 상세 | Public | - | `TrustDetail` |
| GET | `/sellers/:id/reviews` | 판매처 리뷰 목록 | Public | `?page&limit&sort` | `SellerReview[]` |
| POST | `/sellers/:id/reviews` | 판매처 리뷰 작성 | User | Body | `SellerReview` |
| PATCH | `/seller-reviews/:id` | 리뷰 수정 | User | Body | `SellerReview` |
| DELETE | `/seller-reviews/:id` | 리뷰 삭제 | User, Admin | - | `{ message }` |

### 상세

#### GET `/sellers/:id/trust`
```
Response: 200 OK
{
  "success": true,
  "data": {
    "sellerId": 1,
    "sellerName": "쿠팡",
    "overallScore": 95,
    "grade": "A+",                       // A+, A, B+, B, C, D, F
    "breakdown": {
      "deliveryScore": 97,               // 배송 정확도
      "priceAccuracy": 93,               // 가격 정확도
      "returnRate": 2.1,                 // 반품율 (%)
      "responseTime": 1.5,              // 평균 응답시간 (시간)
      "reviewScore": 4.3,               // 평균 리뷰 점수
      "orderCount": 152340,             // 총 주문 수
      "disputeRate": 0.3                // 분쟁 비율 (%)
    },
    "trend": "STABLE",                   // IMPROVING | STABLE | DECLINING
    "lastUpdatedAt": "2026-02-11T06:00:00.000Z"
  }
}
```

#### POST `/sellers/:id/reviews`
```
Request:
{
  "orderId": 1,                          // 필수, 해당 판매처에서 구매한 주문
  "rating": 5,                           // 1~5
  "deliveryRating": 5,                   // 배송 평가 1~5
  "content": "빠른 배송과 정확한 상품!"
}

Response: 201 Created
```

---

## 30. 다국어/다화폐 (i18n)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/i18n/translations` | 번역 조회 | Public | `?locale&namespace` | `Translation[]` |
| POST | `/admin/i18n/translations` | 번역 등록/수정 (Upsert) | Admin | Body | `Translation` |
| DELETE | `/admin/i18n/translations/:id` | 번역 삭제 | Admin | - | `{ message }` |
| GET | `/i18n/exchange-rates` | 환율 목록 | Public | - | `ExchangeRate[]` |
| GET | `/i18n/convert` | 금액 환산 | Public | `?amount&from&to` | `ConvertedAmount` |

### 상세

#### GET `/i18n/translations?locale=en&namespace=product`
```
Response: 200 OK
{
  "success": true,
  "data": [
    { "id": 1, "key": "product.lowest_price", "value": "Lowest Price", "locale": "en", "namespace": "product" },
    { "id": 2, "key": "product.add_to_cart", "value": "Add to Cart", "locale": "en", "namespace": "product" }
  ]
}
```

#### GET `/i18n/exchange-rates`
```
Response: 200 OK
{
  "success": true,
  "data": [
    { "baseCurrency": "KRW", "targetCurrency": "USD", "rate": 0.000748, "updatedAt": "2026-02-11T09:00:00.000Z" },
    { "baseCurrency": "KRW", "targetCurrency": "JPY", "rate": 0.112, "updatedAt": "2026-02-11T09:00:00.000Z" }
  ]
}
```

#### GET `/i18n/convert?amount=1590000&from=KRW&to=USD`
```
Response: 200 OK
{
  "success": true,
  "data": {
    "originalAmount": 1590000,
    "originalCurrency": "KRW",
    "convertedAmount": 1189.32,
    "targetCurrency": "USD",
    "rate": 0.000748
  }
}
```

---

## 31. 이미지 최적화 (Image)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| POST | `/images/upload` | 이미지 업로드 + 최적화 | User, Admin | multipart/form-data | `ImageUploadResult` |
| GET | `/images/:id/variants` | 이미지 변환본 조회 | Public | - | `ImageVariant[]` |
| DELETE | `/images/:id` | 이미지 삭제 (원본+변환본) | Admin | - | `{ message }` |

### 상세

#### POST `/images/upload`
```
Request (multipart/form-data):
- file: File (필수, 최대 10MB, jpg/png/webp/gif)
- category: "product" | "community" | "support" | "seller" (필수)

Response: 201 Created
{
  "success": true,
  "data": {
    "id": 1,
    "originalUrl": "/uploads/original/abc123.jpg",
    "variants": [
      { "type": "THUMBNAIL", "url": "/uploads/thumb/abc123.webp", "width": 200, "height": 200, "format": "webp", "size": 8540 },
      { "type": "MEDIUM", "url": "/uploads/medium/abc123.webp", "width": 600, "height": 600, "format": "webp", "size": 32100 },
      { "type": "LARGE", "url": "/uploads/large/abc123.webp", "width": 1200, "height": 1200, "format": "webp", "size": 78200 }
    ],
    "processingStatus": "COMPLETED"       // PENDING | PROCESSING | COMPLETED | FAILED
  }
}
```

---

## 32. 배지 시스템 (Badge)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/badges` | 전체 배지 목록 | Public | - | `Badge[]` |
| GET | `/badges/me` | 내 배지 목록 | User | - | `UserBadge[]` |
| GET | `/users/:id/badges` | 특정 유저 배지 | Public | - | `UserBadge[]` |
| POST | `/admin/badges` | 배지 생성 | Admin | Body | `Badge` |
| PATCH | `/admin/badges/:id` | 배지 수정 | Admin | Body | `Badge` |
| DELETE | `/admin/badges/:id` | 배지 삭제 | Admin | - | `{ message }` |
| POST | `/admin/badges/:id/grant` | 수동 배지 부여 | Admin | `{ userId }` | `UserBadge` |
| DELETE | `/admin/badges/:id/revoke/:userId` | 배지 회수 | Admin | - | `{ message }` |

### 상세

#### GET `/badges`
```
Response: 200 OK
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "리뷰 마스터",
      "description": "리뷰 10개 이상 작성",
      "iconUrl": "/badges/review-master.svg",
      "type": "AUTO",                     // AUTO | MANUAL
      "condition": { "metric": "review_count", "threshold": 10 },
      "rarity": "COMMON",                 // COMMON | UNCOMMON | RARE | EPIC | LEGENDARY
      "holderCount": 1523
    },
    {
      "id": 2,
      "name": "전문가",
      "description": "관리자가 인정한 분야 전문가",
      "iconUrl": "/badges/expert.svg",
      "type": "MANUAL",
      "condition": null,
      "rarity": "LEGENDARY",
      "holderCount": 12
    }
  ]
}
```

#### POST `/admin/badges`
```
Request:
{
  "name": "구매왕",
  "description": "주문 50건 이상 달성",
  "iconUrl": "/badges/purchase-king.svg",
  "type": "AUTO",
  "condition": {
    "metric": "order_count",              // review_count | post_count | order_count | point_total | login_streak
    "threshold": 50
  },
  "rarity": "RARE"
}

Response: 201 Created
```

---

## 33. PC 견적 짜기 (PC Builder)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/pc-builds` | 내 견적 목록 | User | `?page&limit` | `BuildSummary[]` |
| POST | `/pc-builds` | 견적 생성 | User | Body | `BuildDetail` |
| GET | `/pc-builds/:id` | 견적 상세 | Public | - | `BuildDetail` |
| PATCH | `/pc-builds/:id` | 견적 수정 (이름, 설명) | User | Body | `BuildDetail` |
| DELETE | `/pc-builds/:id` | 견적 삭제 | User | - | `{ message }` |
| POST | `/pc-builds/:id/parts` | 부품 추가 | User | Body | `BuildDetail` |
| DELETE | `/pc-builds/:id/parts/:partId` | 부품 제거 | User | - | `BuildDetail` |
| GET | `/pc-builds/:id/compatibility` | 호환성 체크 | Public | - | `CompatibilityResult` |
| GET | `/pc-builds/:id/share` | 공유 링크 생성 | User | - | `{ shareUrl }` |
| GET | `/pc-builds/shared/:shareCode` | 공유 견적 조회 | Public | - | `BuildDetail` |
| GET | `/pc-builds/popular` | 인기 견적 목록 | Public | `?page&limit` | `BuildSummary[]` |
| GET | `/admin/compatibility-rules` | 호환성 규칙 목록 | Admin | - | `CompatibilityRule[]` |
| POST | `/admin/compatibility-rules` | 호환성 규칙 추가 | Admin | Body | `CompatibilityRule` |
| PATCH | `/admin/compatibility-rules/:id` | 규칙 수정 | Admin | Body | `CompatibilityRule` |
| DELETE | `/admin/compatibility-rules/:id` | 규칙 삭제 | Admin | - | `{ message }` |

### 상세

#### POST `/pc-builds`
```
Request:
{
  "name": "게이밍 PC 2026",
  "description": "고사양 게이밍 목적 데스크탑",
  "purpose": "GAMING",                    // GAMING | OFFICE | DESIGN | DEVELOPMENT | STREAMING
  "budget": 3000000                       // 선택, 예산 (원)
}

Response: 201 Created
{
  "success": true,
  "data": {
    "id": 1,
    "name": "게이밍 PC 2026",
    "purpose": "GAMING",
    "budget": 3000000,
    "parts": [],
    "totalPrice": 0,
    "compatibility": { "status": "EMPTY", "issues": [] },
    "shareCode": null,
    "createdAt": "..."
  }
}
```

#### POST `/pc-builds/:id/parts`
```
Request:
{
  "productId": 101,                       // 필수, 상품 ID (카테고리가 PC 부품)
  "partType": "CPU",                      // CPU | MOTHERBOARD | RAM | GPU | SSD | HDD | PSU | CASE | COOLER | MONITOR
  "sellerId": 1,                          // 선택, 최저가 판매처 자동 선택
  "quantity": 1
}

Response: 200 OK
{
  "success": true,
  "data": {
    "id": 1,
    "parts": [
      {
        "id": 1,
        "partType": "CPU",
        "product": { "id": 101, "name": "AMD Ryzen 7 7800X3D", "lowestPrice": 350000 },
        "seller": { "id": 1, "name": "쿠팡", "price": 350000 },
        "quantity": 1
      }
    ],
    "totalPrice": 350000,
    "compatibility": {
      "status": "INCOMPLETE",              // OK | INCOMPLETE | WARNING | ERROR
      "issues": [],
      "warnings": ["메인보드를 선택해주세요", "RAM을 선택해주세요"],
      "missingParts": ["MOTHERBOARD", "RAM", "GPU", "SSD", "PSU", "CASE"]
    },
    "bottleneck": null
  }
}
```

#### GET `/pc-builds/:id/compatibility`
```
Response: 200 OK
{
  "success": true,
  "data": {
    "status": "WARNING",
    "issues": [],
    "warnings": [
      {
        "type": "BOTTLENECK",
        "message": "CPU 대비 GPU 성능이 낮습니다. 게이밍 목적이라면 GPU 업그레이드를 권장합니다.",
        "severity": "MEDIUM"
      }
    ],
    "errors": [],
    "powerEstimate": {
      "totalWattage": 450,
      "psuWattage": 650,
      "headroom": 200,
      "sufficient": true
    },
    "socketCompatible": true,
    "ramCompatible": true,
    "formFactorCompatible": true
  }
}
```

---

## 34. 친구 / 팔로우 (Friend)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| POST | `/friends/request/{userId}` | 친구 신청 | User | - | `{ message }` |
| PATCH | `/friends/request/{friendshipId}/accept` | 친구 수락 | User | - | `{ message }` |
| PATCH | `/friends/request/{friendshipId}/reject` | 친구 거절 | User | - | `{ message }` |
| GET | `/friends` | 내 친구 목록 | User | `?page&limit` | `FriendResponse[]` |
| GET | `/friends/requests/received` | 받은 친구 요청 | User | `?page&limit` | `FriendRequest[]` |
| GET | `/friends/requests/sent` | 보낸 친구 요청 | User | `?page&limit` | `FriendRequest[]` |
| GET | `/friends/feed` | 친구 활동 피드 | User | `?page&limit` | `ActivityFeed[]` |
| POST | `/friends/block/{userId}` | 유저 차단 | User | - | `{ message }` |
| DELETE | `/friends/block/{userId}` | 차단 해제 | User | - | `{ message }` |
| DELETE | `/friends/{userId}` | 친구 삭제 | User | - | `{ message }` |

---

## 35. 숏폼 (Short-form Video)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| POST | `/shortforms` | 숏폼 업로드 (60초 이내) | User | `multipart/form-data (video, title, productIds[])` | `ShortFormResponse` |
| GET | `/shortforms` | 숏폼 피드 (무한스크롤) | Public | `?cursor&limit` | `ShortFormResponse[]` |
| GET | `/shortforms/:id` | 숏폼 상세 (조회수 증가) | Public | - | `ShortFormResponse` |
| POST | `/shortforms/:id/like` | 좋아요 토글 | User | - | `{ liked, likeCount }` |
| POST | `/shortforms/:id/comments` | 댓글 작성 | User | `{ content }` | `CommentResponse` |
| GET | `/shortforms/:id/comments` | 댓글 목록 | Public | `?page&limit` | `CommentResponse[]` |
| GET | `/shortforms/ranking/list` | 인기 숏폼 랭킹 | Public | `?period=day,week,month` | `ShortFormResponse[]` |
| GET | `/shortforms/:id/transcode-status` | 트랜스코딩 상태 조회 | Public | - | `{ status, errorMessage, transcodedAt }` |
| POST | `/shortforms/:id/transcode/retry` | 트랜스코딩 재시도 | User | - | `{ message, queued }` |
| DELETE | `/shortforms/:id` | 숏폼 삭제 | Owner | - | `{ message }` |
| GET | `/shortforms/user/:userId` | 특정 유저의 숏폼 | Public | `?page&limit` | `ShortFormResponse[]` |

---

## 36. 멀티미디어 리소스 (Media)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| POST | `/media/upload` | 파일 업로드 (이미지/영상/음원/문서) | User | `multipart/form-data (files[], ownerType, ownerId)` | `AttachmentResponse[]` |
| POST | `/media/presigned-url` | S3 Pre-signed URL 발급 (대용량) | User | `{ fileName, fileType, fileSize }` | `{ uploadUrl, fileKey }` |
| GET | `/media/stream/:id` | 음원/영상 스트리밍 (Range Request) | Public | `Range` header | Partial Content (206) |
| DELETE | `/media/:id` | 업로드 파일 삭제 | Owner | - | `{ message }` |
| GET | `/media/:id/metadata` | 파일 메타데이터 조회 | Public | - | `{ mime, size, duration, resolution }` |

---

## 37. 뉴스 (News)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/news` | 뉴스 목록 (탭별 필터링) | Public | `?category&page&limit` | `NewsResponse[]` |
| GET | `/news/categories` | 뉴스 카테고리 목록 | Public | - | `NewsCategoryResponse[]` |
| GET | `/news/:id` | 뉴스 상세 + 관련 상품 | Public | - | `NewsDetailResponse` |
| POST | `/news` | 뉴스 작성 | Admin | `{ title, content, categoryId, thumbnailUrl, productIds[] }` | `NewsResponse` |
| PATCH | `/news/:id` | 뉴스 수정 | Admin | `{ title?, content?, categoryId? }` | `NewsResponse` |
| DELETE | `/news/:id` | 뉴스 삭제 | Admin | - | `{ message }` |
| POST | `/news/categories` | 뉴스 카테고리 추가 | Admin | `{ name, slug }` | `NewsCategoryResponse` |
| DELETE | `/news/categories/:id` | 뉴스 카테고리 삭제 | Admin | - | `{ message }` |

---

## 38. 상품 매핑 (Product Matching)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/matching/pending` | 매핑 대기 목록 | Admin | `?page&limit` | `MappingResponse[]` |
| PATCH | `/matching/:id/approve` | 매핑 승인 | Admin | `{ productId }` | `{ message }` |
| PATCH | `/matching/:id/reject` | 매핑 거절 | Admin | `{ reason }` | `{ message }` |
| POST | `/matching/auto-match` | 자동 매핑 실행 | Admin | - | `{ matchedCount, pendingCount }` |
| GET | `/matching/stats` | 매핑 통계 (승인/대기/거절) | Admin | - | `MappingStatsResponse` |

---

## 39. 가격 신뢰성 (Fraud Detection)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/fraud/alerts` | 이상 가격 알림 목록 | Admin | `?status&page&limit` | `FraudAlertResponse[]` |
| PATCH | `/fraud/alerts/:id/approve` | 이상 가격 승인 (데이터 반영) | Admin | - | `{ message }` |
| PATCH | `/fraud/alerts/:id/reject` | 이상 가격 거절 (데이터 삭제) | Admin | - | `{ message }` |
| GET | `/products/:id/real-price` | 배송비 포함 실제 가격 조회 | Public | `?sellerId` | `{ productPrice, shippingFee, totalPrice, shippingType }` |

---

## 40. 가격 분석 (Price Analytics)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/analytics/products/:id/lowest-ever` | 역대 최저가 여부 확인 | Public | - | `{ isLowestEver, currentPrice, lowestPrice, lowestDate }` |
| GET | `/analytics/products/:id/unit-price` | 용량/수량당 단가 계산 | Public | - | `{ unitPrice, unit, quantity }` |

---

## 41. 중고 마켓 (Used Market)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/used-market/products/:id/price` | 특정 상품 중고 시세 | Public | - | `{ averagePrice, minPrice, maxPrice, trend }` |
| GET | `/used-market/categories/:id/prices` | 카테고리별 중고 시세 | Public | `?page&limit` | `UsedPriceResponse[]` |
| POST | `/used-market/pc-builds/:buildId/estimate` | PC 견적 기반 중고 매입가 산정 | User | - | `{ estimatedPrice, partBreakdown[] }` |

---

## 42. 자동차 (Auto)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/auto/models` | 자동차 모델 목록 | Public | `?brand&type` | `CarModelResponse[]` |
| GET | `/auto/models/:id/trims` | 트림/옵션 목록 | Public | - | `TrimResponse[]` |
| POST | `/auto/estimate` | 신차 견적 계산 | Public | `{ modelId, trimId, optionIds[] }` | `{ basePrice, optionPrice, tax, totalPrice, monthlyPayment }` |
| GET | `/auto/models/:id/lease-offers` | 렌트/리스 비교 | Public | - | `LeaseOfferResponse[]` |

---

## 43. 역경매 (Reverse Auction)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| POST | `/auctions` | 역경매 등록 | User | `{ title, description, categoryId, specs, budget }` | `AuctionResponse` |
| GET | `/auctions` | 역경매 목록 | Public | `?status&categoryId&page&limit` | `AuctionResponse[]` |
| GET | `/auctions/:id` | 역경매 상세 + 입찰 목록 | Public | - | `AuctionDetailResponse` |
| POST | `/auctions/:id/bids` | 입찰 등록 | Seller | `{ price, description, deliveryDays }` | `BidResponse` |
| PATCH | `/auctions/:id/bids/:bidId/select` | 낙찰 선택 | User(Owner) | - | `{ message }` |
| DELETE | `/auctions/:id` | 역경매 취소 | User(Owner) | - | `{ message }` |
| PATCH | `/auctions/:id/bids/:bidId` | 입찰 수정 | Seller(Owner) | `{ price?, description? }` | `BidResponse` |
| DELETE | `/auctions/:id/bids/:bidId` | 입찰 취소 | Seller(Owner) | - | `{ message }` |

---

## 44. 비교함 (Product Comparison Bar)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| POST | `/compare/add` | 비교함에 상품 추가 (최대 4개) | Public | `{ productId }` | `{ compareList[] }` |
| DELETE | `/compare/:productId` | 비교함에서 상품 제거 | Public | - | `{ compareList[] }` |
| GET | `/compare` | 비교함 현재 목록 | Public | - | `{ compareList[] }` |
| GET | `/compare/detail` | 비교 상세 (스펙 나란히 비교, 차이점 강조) | Public | - | `CompareDetailResponse` |

---

## 45. 관리자 시스템 설정 (Admin Settings)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/admin/settings/extensions` | 허용 확장자 목록 조회 | Admin | - | `{ extensions[] }` |
| POST | `/admin/settings/extensions` | 허용 확장자 추가/변경 | Admin | `{ extensions: ["jpg","png","mp4","mp3"] }` | `{ extensions[] }` |
| GET | `/admin/settings/upload-limits` | 미디어 타입별 용량 제한 조회 | Admin | - | `UploadLimitsResponse` |
| PATCH | `/admin/settings/upload-limits` | 용량 제한 변경 | Admin | `{ image: 5, video: 100, audio: 20 }` (MB) | `UploadLimitsResponse` |
| GET | `/admin/settings/review-policy` | 리뷰 정책 조회 | Admin | - | `{ maxImageCount, pointAmount }` |
| PATCH | `/admin/settings/review-policy` | 리뷰 정책 변경 | Admin | `{ maxImageCount: 10, pointAmount: 500 }` | `{ maxImageCount, pointAmount }` |

---

## 46. 헬스체크 (Health)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/health` | 전체 시스템 상태 확인 | Public | - | `{ status, checks: { db, redis, elasticsearch } }` |

---

## 47. 장애복원력 (Resilience / Circuit Breaker) — Admin

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/resilience/circuit-breakers` | Circuit Breaker 상태 목록 | Admin | - | `{ items[] }` |
| GET | `/resilience/circuit-breakers/policies` | Circuit Breaker 자동 튜닝 정책/통계 조회 | Admin | - | `{ items: [{ name, options, stats }] }` |
| GET | `/resilience/circuit-breakers/:name` | Circuit Breaker 단건 상태 | Admin | - | `CircuitBreakerSnapshot` |
| POST | `/resilience/circuit-breakers/:name/reset` | Circuit Breaker 수동 초기화 | Admin | - | `{ message, name }` |

---

## 48. 에러 코드 카탈로그 (Error Code Catalog)

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/errors/codes` | 시스템 에러코드 전체 목록 | Public | - | `{ total, items[] }` |
| GET | `/errors/codes/:key` | 시스템 에러코드 단건 조회 | Public | - | `ErrorCode \| null` |

---

## 49. 큐 운영 복구 (Queue Admin) — Admin

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/admin/queues/supported` | 운영 대상 큐 목록 조회 | Admin | - | `{ items[] }` |
| GET | `/admin/queues/stats` | 큐별 상태 통계 조회 | Admin | - | `{ total, items: [{ queueName, paused, counts }] }` |
| POST | `/admin/queues/auto-retry` | 전체 큐 실패 Job 자동 재시도 | Admin | `?perQueueLimit&maxTotal` | `{ retriedTotal, items[] }` |
| GET | `/admin/queues/:queueName/failed` | 실패 Job 목록 조회 | Admin | `?page&limit&newestFirst` | `FailedJob[]` |
| POST | `/admin/queues/:queueName/failed/retry` | 실패 Job 일괄 재시도 | Admin | `?limit` | `{ requested, requeuedCount, jobIds[] }` |
| POST | `/admin/queues/:queueName/jobs/:jobId/retry` | 실패 Job 개별 재시도 | Admin | - | `{ retried: true }` |
| DELETE | `/admin/queues/:queueName/jobs/:jobId` | Job 개별 삭제 | Admin | - | `{ removed: true }` |

운영 규칙:
- 개별 재시도 API(`POST /admin/queues/:queueName/jobs/:jobId/retry`)는 `failed` 상태 Job에서만 허용됩니다.
- `failed` 외 상태(`waiting`, `active`, `completed`, `delayed` 등) 재시도 요청 시 `400 VALIDATION_FAILED`를 반환합니다.

---

## 50. 운영 통합 대시보드 (Ops Dashboard) — Admin

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/admin/ops-dashboard/summary` | 운영 핵심 지표 통합 조회 | Admin | - | `{ checkedAt, overallStatus, health, searchSync, crawler, queue, errors, alerts[], alertCount }` |

운영 규칙:
- 하위 지표 일부 조회가 실패해도 API는 200 응답을 유지하고 `overallStatus: degraded`로 반환합니다.
- 실패한 지표는 `null`로 내려가며 원인은 `errors.{key}`에 포함됩니다.
- 집계 결과를 바탕으로 경보 목록 `alerts`를 제공하며, 경보 개수는 `alertCount`에 포함됩니다.
- 경보 임계치는 환경변수로 제어합니다.
  - `OPS_ALERT_SEARCH_FAILED_THRESHOLD`
  - `OPS_ALERT_CRAWLER_FAILED_RUNS_THRESHOLD`
  - `OPS_ALERT_QUEUE_FAILED_THRESHOLD`
- 임계치 판정은 `현재값 >= 임계치` 기준이며, 임계치가 `0` 이하인 경우 해당 경보는 비활성화됩니다.

---

## 51. 관측성 대시보드 (Observability) — Admin

| Method | Endpoint | 설명 | 권한 | Request | Response |
|--------|----------|------|------|---------|----------|
| GET | `/admin/observability/metrics` | 최근 요청 메트릭 요약 조회 | Admin | - | `{ totalRequests, errorRate, avgLatencyMs, p95LatencyMs, p99LatencyMs, statusBuckets }` |
| GET | `/admin/observability/traces` | 최근 HTTP 트레이스 조회 | Admin | `?limit&pathContains` | `{ items: [{ requestId, method, path, statusCode, durationMs, ... }] }` |
| GET | `/admin/observability/dashboard` | 관측성 통합 대시보드 조회 | Admin | - | `{ process, metrics, queue, resilience, searchSync, crawler, opsSummary }` |

운영 규칙:
- 트레이스는 인메모리 순환 버퍼(`OBS_TRACE_BUFFER_LIMIT`)에 보관됩니다.
- 메트릭은 최근 15분 요청 기준으로 계산됩니다.
- 대시보드는 큐/서킷브레이커/검색동기화/크롤러/Ops Dashboard를 통합 조회합니다.

---

## 공통 에러 코드

| HTTP Status | 코드 | 설명 |
|-------------|------|------|
| 400 | VALIDATION_ERROR | 입력값 검증 실패 |
| 400 | INSUFFICIENT_STOCK | 재고 부족 |
| 400 | INSUFFICIENT_POINT | 포인트 부족 |
| 400 | INVALID_STATUS_TRANSITION | 유효하지 않은 상태 변경 |
| 400 | AMOUNT_MISMATCH | 결제 금액 불일치 |
| 400 | INCOMPATIBLE_PARTS | PC 부품 호환성 오류 |
| 400 | INVALID_PROVIDER | 지원하지 않는 소셜 로그인 공급자 |
| 400 | CANNOT_UNLINK | 일반 로그인 수단이 없어 소셜 연동 해제 불가 |
| 400 | AUCTION_CLOSED | 종료된 역경매 |
| 400 | BID_TOO_LOW | 입찰 금액이 너무 낮음 |
| 401 | UNAUTHORIZED | 인증 필요 |
| 401 | SOCIAL_AUTH_FAILED | 소셜 인증 실패 |
| 401 | TOKEN_EXPIRED | 토큰 만료 |
| 403 | FORBIDDEN | 권한 없음 |
| 404 | NOT_FOUND | 리소스 없음 |
| 409 | CONFLICT | 중복/충돌 |
| 409 | SOCIAL_ALREADY_LINKED | 이미 다른 계정에 연동된 소셜 계정 |
| 413 | FILE_TOO_LARGE | 파일 용량 초과 |
| 415 | MEDIA_TYPE_NOT_ALLOWED | 허용되지 않은 미디어 타입 |
| 422 | PUSH_SUBSCRIPTION_INVALID | 유효하지 않은 푸시 구독 |
| 422 | FRAUD_DETECTED | 이상 가격 감지 |
| 429 | TOO_MANY_REQUESTS | 요청 횟수 초과 |
| 500 | INTERNAL_ERROR | 서버 내부 오류 |
| 503 | SEARCH_UNAVAILABLE | 검색 서비스 일시 장애 |
| 503 | CRAWLER_BUSY | 크롤러 큐 초과 |
