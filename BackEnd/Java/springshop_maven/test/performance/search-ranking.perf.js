import http from 'k6/http';
import { check } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://127.0.0.1:8000';

export const options = {
  vus: 1,
  iterations: 10,
};

export default function () {
  const ranking = http.get(`${baseUrl}/api/v1/rankings/keywords/popular`);
  check(ranking, { 'ranking ok': (r) => r.status === 200 });

  const search = http.get(`${baseUrl}/api/v1/products?search=PB`);
  check(search, { 'search ok': (r) => r.status === 200 });
}
