import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 8,
  duration: '30s',
  thresholds: {
    http_req_failed: ['rate<0.03'],
    http_req_duration: ['p(95)<1000', 'p(99)<2000'],
  },
};

const baseUrl = __ENV.BASE_URL || 'http://127.0.0.1:8000';

export default function () {
  const res = http.get(`${baseUrl}/api/v1/rankings/products/popular`);
  check(res, { 'popular ranking ok': (r) => r.status === 200 || r.status === 401 || r.status === 403 });
  sleep(1);
}
