/**
 * 문자열을 URL 슬러그로 변환
 * 예: "삼성 갤럭시 S25 Ultra" → "삼성-갤럭시-s25-ultra"
 */
export function generateSlug(text: string): string {
  return text
    .toLowerCase()
    .trim()
    .replace(/[^\w\s가-힣-]/g, '')
    .replace(/[\s_]+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '');
}

/**
 * 고유 슬러그 생성 (suffix 추가)
 */
export function generateUniqueSlug(text: string): string {
  const base = generateSlug(text);
  const suffix = Date.now().toString(36);
  return `${base}-${suffix}`;
}
