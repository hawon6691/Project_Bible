import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:3000/api/v1';

export const options = {
  stages: [
    { duration: '30s', target: 20 },
    { duration: '1m', target: 50 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.02'],
    http_req_duration: ['p(95)<1200', 'p(99)<2000'],
  },
};

function randomKeyword() {
  const keywords = ['cpu', 'gpu', 'ssd', 'ram', 'monitor', 'keyboard', 'laptop'];
  return keywords[Math.floor(Math.random() * keywords.length)];
}

export default function () {
  const keyword = randomKeyword();
  const page = Math.floor(Math.random() * 3) + 1;

  const searchRes = http.get(`${BASE_URL}/search?keyword=${keyword}&page=${page}&limit=20`);
  check(searchRes, {
    'search status 200': (res) => res.status === 200,
  });

  const autoRes = http.get(`${BASE_URL}/search/autocomplete?q=${keyword}&limit=10`);
  check(autoRes, {
    'autocomplete status 200': (res) => res.status === 200,
  });

  const rankProductRes = http.get(`${BASE_URL}/rankings/products/popular?limit=20`);
  check(rankProductRes, {
    'ranking products status 200': (res) => res.status === 200,
  });

  const rankKeywordRes = http.get(`${BASE_URL}/rankings/keywords/popular?limit=20`);
  check(rankKeywordRes, {
    'ranking keywords status 200': (res) => res.status === 200,
  });

  sleep(0.2);
}
