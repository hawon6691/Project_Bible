import http from 'k6/http';
import { check } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://127.0.0.1:8000';

export const options = {
  vus: 1,
  iterations: 10,
};

export default function () {
  const response = http.get(`${baseUrl}/api/v1/compare`);
  check(response, { 'compare ok': (r) => r.status === 200 });
}
