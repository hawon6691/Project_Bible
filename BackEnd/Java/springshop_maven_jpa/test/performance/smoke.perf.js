import http from 'k6/http';
import { check, sleep } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://127.0.0.1:8000';

export const options = {
  vus: 1,
  iterations: 10,
};

export default function () {
  const health = http.get(`${baseUrl}/api/v1/health`);
  check(health, { 'health ok': (r) => r.status === 200 });

  const products = http.get(`${baseUrl}/api/v1/products`);
  check(products, { 'products ok': (r) => r.status === 200 });
  sleep(0.2);
}
