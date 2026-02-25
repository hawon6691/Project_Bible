export interface HttpTrace {
  requestId: string;
  method: string;
  path: string;
  statusCode: number;
  durationMs: number;
  ip?: string | null;
  userId?: number | null;
  timestamp: string;
}

export interface MetricsSummary {
  totalRequests: number;
  errorRequests: number;
  errorRate: number;
  avgLatencyMs: number;
  p95LatencyMs: number;
  p99LatencyMs: number;
  statusBuckets: {
    s2xx: number;
    s3xx: number;
    s4xx: number;
    s5xx: number;
  };
}
