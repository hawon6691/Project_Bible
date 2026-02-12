export const ERROR_CODES = {
  // ── Auth ──
  AUTH_INVALID_CREDENTIALS: { code: 'AUTH_001', message: '이메일 또는 비밀번호가 올바르지 않습니다.' },
  AUTH_EMAIL_ALREADY_EXISTS: { code: 'AUTH_002', message: '이미 등록된 이메일입니다.' },
  AUTH_TOKEN_EXPIRED: { code: 'AUTH_003', message: '토큰이 만료되었습니다.' },
  AUTH_TOKEN_INVALID: { code: 'AUTH_004', message: '유효하지 않은 토큰입니다.' },
  AUTH_EMAIL_NOT_VERIFIED: { code: 'AUTH_005', message: '이메일 인증이 완료되지 않았습니다.' },
  AUTH_VERIFICATION_CODE_INVALID: { code: 'AUTH_006', message: '인증코드가 올바르지 않습니다.' },
  AUTH_VERIFICATION_CODE_EXPIRED: { code: 'AUTH_007', message: '인증코드가 만료되었습니다.' },
  AUTH_SOCIAL_ACCOUNT_ALREADY_LINKED: { code: 'AUTH_008', message: '이미 연동된 소셜 계정입니다.' },
  AUTH_UNAUTHORIZED: { code: 'AUTH_009', message: '인증이 필요합니다.' },
  AUTH_FORBIDDEN: { code: 'AUTH_010', message: '접근 권한이 없습니다.' },

  // ── User ──
  USER_NOT_FOUND: { code: 'USER_001', message: '사용자를 찾을 수 없습니다.' },
  USER_NICKNAME_DUPLICATE: { code: 'USER_002', message: '이미 사용 중인 닉네임입니다.' },
  USER_PHONE_MISMATCH: { code: 'USER_003', message: '전화번호가 일치하지 않습니다.' },

  // ── Category ──
  CATEGORY_NOT_FOUND: { code: 'CAT_001', message: '카테고리를 찾을 수 없습니다.' },
  CATEGORY_HAS_CHILDREN: { code: 'CAT_002', message: '하위 카테고리가 있어 삭제할 수 없습니다.' },

  // ── Product ──
  PRODUCT_NOT_FOUND: { code: 'PROD_001', message: '상품을 찾을 수 없습니다.' },
  PRODUCT_ALREADY_EXISTS: { code: 'PROD_002', message: '이미 등록된 상품입니다.' },
  PRODUCT_OUT_OF_STOCK: { code: 'PROD_003', message: '재고가 부족합니다.' },

  // ── Order ──
  ORDER_NOT_FOUND: { code: 'ORD_001', message: '주문을 찾을 수 없습니다.' },
  ORDER_ALREADY_CANCELLED: { code: 'ORD_002', message: '이미 취소된 주문입니다.' },
  ORDER_CANNOT_CANCEL: { code: 'ORD_003', message: '취소할 수 없는 주문 상태입니다.' },

  // ── Review ──
  REVIEW_NOT_FOUND: { code: 'REV_001', message: '리뷰를 찾을 수 없습니다.' },
  REVIEW_ALREADY_EXISTS: { code: 'REV_002', message: '이미 작성한 리뷰가 있습니다.' },
  REVIEW_NOT_OWNER: { code: 'REV_003', message: '본인의 리뷰만 수정/삭제할 수 있습니다.' },

  // ── Payment ──
  PAYMENT_FAILED: { code: 'PAY_001', message: '결제에 실패했습니다.' },
  PAYMENT_ALREADY_REFUNDED: { code: 'PAY_002', message: '이미 환불된 결제입니다.' },

  // ── Common ──
  VALIDATION_FAILED: { code: 'COMMON_001', message: '입력값 검증에 실패했습니다.' },
  RESOURCE_NOT_FOUND: { code: 'COMMON_002', message: '요청한 리소스를 찾을 수 없습니다.' },
  INTERNAL_SERVER_ERROR: { code: 'COMMON_003', message: '서버 내부 오류가 발생했습니다.' },
  TOO_MANY_REQUESTS: { code: 'COMMON_004', message: '요청이 너무 많습니다. 잠시 후 다시 시도해주세요.' },
  FILE_UPLOAD_FAILED: { code: 'COMMON_005', message: '파일 업로드에 실패했습니다.' },
  FILE_TYPE_NOT_ALLOWED: { code: 'COMMON_006', message: '허용되지 않은 파일 형식입니다.' },
} as const;

export type ErrorCode = keyof typeof ERROR_CODES;
