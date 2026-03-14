/**
 * 날짜를 YYYY-MM-DD 형식으로 변환
 */
export function formatDate(date: Date): string {
  return date.toISOString().split('T')[0];
}

/**
 * 날짜를 YYYY-MM-DD HH:mm:ss 형식으로 변환
 */
export function formatDateTime(date: Date): string {
  return date.toISOString().replace('T', ' ').substring(0, 19);
}

/**
 * 현재 시각으로부터 N분 후의 Date 반환
 */
export function addMinutes(minutes: number, from: Date = new Date()): Date {
  return new Date(from.getTime() + minutes * 60 * 1000);
}

/**
 * 현재 시각으로부터 N일 후의 Date 반환
 */
export function addDays(days: number, from: Date = new Date()): Date {
  return new Date(from.getTime() + days * 24 * 60 * 60 * 1000);
}

/**
 * 만료 여부 확인
 */
export function isExpired(expiresAt: Date): boolean {
  return new Date() > expiresAt;
}
