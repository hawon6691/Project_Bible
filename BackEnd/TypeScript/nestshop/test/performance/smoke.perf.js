import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:3000/api/v1';

export const options = {
  vus: 1,
  iterations: 10,
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1500'],
  },
};

export default function () {
  const health = http.get(`${BASE_URL}/health`);
  check(health, {
    'health status 200': (res) => res.status === 200,
  });

  const search = http.get(`${BASE_URL}/search?keyword=cpu&page=1&limit=20`);
  check(search, {
    'search status 200': (res) => res.status === 200,
  });
}
