import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '10s', target: 5 },
    { duration: '10s', target: 40 },
    { duration: '10s', target: 5 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.03'],
    http_req_duration: ['p(95)<1000', 'p(99)<2000'],
  },
};

const baseUrl = __ENV.BASE_URL || 'http://127.0.0.1:8000';

export default function () {
  const res = http.get(`${baseUrl}/api/v1/products`);
  check(res, { 'products 200': (r) => r.status === 200 });
  sleep(1);
}
