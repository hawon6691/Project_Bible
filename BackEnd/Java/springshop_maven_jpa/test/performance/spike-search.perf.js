import http from 'k6/http';
import { check } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://127.0.0.1:8000';

export const options = {
  stages: [
    { duration: '5s', target: 1 },
    { duration: '5s', target: 10 },
    { duration: '5s', target: 0 },
  ],
};

export default function () {
  const response = http.get(`${baseUrl}/api/v1/products?search=PB`);
  check(response, { 'search ok': (r) => r.status === 200 });
}
