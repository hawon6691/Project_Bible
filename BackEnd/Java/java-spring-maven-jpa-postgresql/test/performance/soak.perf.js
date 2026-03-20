import http from 'k6/http';
import { check, sleep } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://127.0.0.1:8000';
const durationSeconds = Number.parseInt(__ENV.SOAK_SECONDS || '30', 10);

export const options = {
  vus: 1,
  duration: `${durationSeconds}s`,
};

export default function () {
  const response = http.get(`${baseUrl}/api/v1/health`);
  check(response, { 'health ok': (r) => r.status === 200 });
  sleep(1);
}
