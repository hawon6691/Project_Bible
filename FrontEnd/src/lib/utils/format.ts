import dayjs from 'dayjs';

export function formatPrice(price: number | null | undefined): string {
  if (price == null) return '-';
  return price.toLocaleString('ko-KR') + '원';
}

export function formatDate(date: string | null | undefined): string {
  if (!date) return '-';
  return dayjs(date).format('YYYY.MM.DD');
}

export function formatDateTime(date: string | null | undefined): string {
  if (!date) return '-';
  return dayjs(date).format('YYYY.MM.DD HH:mm');
}

export function formatRating(rating: number): string {
  return rating.toFixed(1);
}

export function formatPriceDiff(diff: number | undefined): string {
  if (!diff) return '';
  const sign = diff > 0 ? '+' : '';
  return `${sign}${diff.toLocaleString('ko-KR')}원`;
}

export function truncate(str: string, maxLength: number): string {
  if (str.length <= maxLength) return str;
  return str.slice(0, maxLength) + '...';
}
