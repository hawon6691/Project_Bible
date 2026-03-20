import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://127.0.0.1:3310';

export const options = {
  scenarios: {
    soak_health_and_ops: {
      executor: 'constant-arrival-rate',
      rate: 8,
      timeUnit: '1s',
      duration: __ENV.SOAK_DURATION || '5m',
      preAllocatedVUs: 10,
      maxVUs: 30,
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.02'],
    http_req_duration: ['p(95)<800', 'p(99)<1500'],
  },
};

export default function () {
  const health = http.get(`${BASE_URL}/health`);
  check(health, {
    'health status 200': (r) => r.status === 200,
  });

  const ops = http.get(`${BASE_URL}/admin/ops-dashboard/summary`);
  check(ops, {
    'ops summary status 200/401': (r) => r.status === 200 || r.status === 401,
  });

  sleep(0.2);
}
