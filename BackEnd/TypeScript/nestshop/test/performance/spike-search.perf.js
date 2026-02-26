import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://127.0.0.1:3310';

export const options = {
  scenarios: {
    spike_search: {
      executor: 'ramping-arrival-rate',
      startRate: 5,
      timeUnit: '1s',
      preAllocatedVUs: 20,
      maxVUs: 120,
      stages: [
        { target: 5, duration: '30s' },
        { target: 80, duration: '45s' },
        { target: 10, duration: '45s' },
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.03'],
    http_req_duration: ['p(95)<1000', 'p(99)<2000'],
  },
};

export default function () {
  const query = encodeURIComponent('rtx 4070');
  const search = http.get(`${BASE_URL}/search?keyword=${query}&page=1&limit=12`);
  check(search, {
    'search status 200/503': (r) => r.status === 200 || r.status === 503,
  });

  sleep(0.1);
}
