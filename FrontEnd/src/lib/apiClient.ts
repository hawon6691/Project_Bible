import { API_BASE_URL } from '@/lib/config';
import { clearAuth, getRefreshToken, setAuthTokens } from '@/lib/auth';
import type { ApiEnvelope, ApiErrorEnvelope, TokenResponse } from '@/lib/types';

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PATCH' | 'PUT' | 'DELETE';
  query?: Record<string, unknown>;
  body?: unknown;
  token?: string | null;
  headers?: Record<string, string>;
  retryOnAuthError?: boolean;
}

function buildUrl(path: string, query?: RequestOptions['query']) {
  const url = new URL(`${API_BASE_URL}${path}`);

  if (query) {
    for (const [key, value] of Object.entries(query)) {
      if (value === undefined || value === null || value === '') {
        continue;
      }

      if (typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean') {
        url.searchParams.set(key, String(value));
      }
    }
  }

  return url.toString();
}

function extractMessage(raw: unknown, status: number) {
  const err = raw as ApiErrorEnvelope;
  const message = Array.isArray(err.message) ? err.message.join(', ') : err.message;
  return message || `HTTP ${status}`;
}

async function tryRefreshToken(): Promise<string | null> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) return null;

  const res = await fetch(`${API_BASE_URL}/auth/refresh`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ refreshToken }),
    credentials: 'include',
  });

  if (!res.ok) {
    clearAuth();
    return null;
  }

  const raw = await res.json().catch(() => null);
  if (!raw || typeof raw !== 'object' || !('data' in raw)) {
    clearAuth();
    return null;
  }

  const envelope = raw as ApiEnvelope<TokenResponse>;
  if (!envelope.data?.accessToken || !envelope.data?.refreshToken) {
    clearAuth();
    return null;
  }

  setAuthTokens(envelope.data.accessToken, envelope.data.refreshToken);
  return envelope.data.accessToken;
}

export async function request<T>(path: string, options: RequestOptions = {}): Promise<{ data: T; meta?: ApiEnvelope<T>['meta'] }> {
  const execute = async (tokenOverride?: string | null) => {
    const res = await fetch(buildUrl(path, options.query), {
      method: options.method || 'GET',
      headers: {
        'Content-Type': 'application/json',
        ...(tokenOverride ? { Authorization: `Bearer ${tokenOverride}` } : {}),
        ...(options.headers || {}),
      },
      body: options.body ? JSON.stringify(options.body) : undefined,
      credentials: 'include',
    });

    const raw = await res.json().catch(() => ({}));
    return { res, raw };
  };

  const first = await execute(options.token);

  if (
    first.res.status === 401 &&
    options.retryOnAuthError !== false &&
    options.token
  ) {
    const newAccessToken = await tryRefreshToken();
    if (newAccessToken) {
      const retried = await execute(newAccessToken);
      if (!retried.res.ok) {
        throw new Error(extractMessage(retried.raw, retried.res.status));
      }

      if (retried.raw && typeof retried.raw === 'object' && 'success' in retried.raw && 'data' in retried.raw) {
        const envelope = retried.raw as ApiEnvelope<T>;
        return { data: envelope.data, meta: envelope.meta };
      }

      return { data: retried.raw as T };
    }
  }

  if (!first.res.ok) {
    throw new Error(extractMessage(first.raw, first.res.status));
  }

  if (first.raw && typeof first.raw === 'object' && 'success' in first.raw && 'data' in first.raw) {
    const envelope = first.raw as ApiEnvelope<T>;
    return { data: envelope.data, meta: envelope.meta };
  }

  return { data: first.raw as T };
}
