import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:3000/api/v1';
const PRODUCT_ID_MIN = Number(__ENV.PRODUCT_ID_MIN || 1);
const PRODUCT_ID_MAX = Number(__ENV.PRODUCT_ID_MAX || 50);

export const options = {
  stages: [
    { duration: '30s', target: 10 },
    { duration: '1m', target: 30 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.02'],
    http_req_duration: ['p(95)<1500', 'p(99)<2500'],
  },
};

function randomProductId() {
  return Math.floor(Math.random() * (PRODUCT_ID_MAX - PRODUCT_ID_MIN + 1)) + PRODUCT_ID_MIN;
}

export default function () {
  const productId = randomProductId();

  const compareRes = http.get(`${BASE_URL}/prices/${productId}`);
  check(compareRes, {
    'price compare status 200/404': (res) => res.status === 200 || res.status === 404,
  });

  const historyRes = http.get(`${BASE_URL}/prices/${productId}/history?period=3m`);
  check(historyRes, {
    'price history status 200/404': (res) => res.status === 200 || res.status === 404,
  });

  const realPriceRes = http.get(`${BASE_URL}/products/${productId}/real-price`);
  check(realPriceRes, {
    'real price status 200/404': (res) => res.status === 200 || res.status === 404,
  });

  sleep(0.2);
}
