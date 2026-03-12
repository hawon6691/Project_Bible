import { INestApplication } from '@nestjs/common';

export interface HttpResult {
  status: number;
  body: any;
}

export interface RequestOptions {
  payload?: unknown;
  headers?: Record<string, string>;
}

export async function startTestServer(app: INestApplication) {
  await app.listen(0);
  const server = app.getHttpServer();
  const address = server.address() as { port: number };
  const baseUrl = `http://127.0.0.1:${address.port}`;

  async function request(method: string, path: string, options: RequestOptions = {}): Promise<HttpResult> {
    const headers = new Headers(options.headers);

    if (options.payload !== undefined && !headers.has('Content-Type')) {
      headers.set('Content-Type', 'application/json');
    }

    const res = await fetch(`${baseUrl}${path}`, {
      method,
      headers,
      body: options.payload !== undefined ? JSON.stringify(options.payload) : undefined,
    });
    return { status: res.status, body: await res.json() };
  }

  return {
    async get(path: string, options: RequestOptions = {}): Promise<HttpResult> {
      return request('GET', path, options);
    },
    async post(path: string, payload: unknown, options: Omit<RequestOptions, 'payload'> = {}): Promise<HttpResult> {
      return request('POST', path, { ...options, payload });
    },
    async put(path: string, payload: unknown, options: Omit<RequestOptions, 'payload'> = {}): Promise<HttpResult> {
      return request('PUT', path, { ...options, payload });
    },
    async patch(path: string, payload: unknown, options: Omit<RequestOptions, 'payload'> = {}): Promise<HttpResult> {
      return request('PATCH', path, { ...options, payload });
    },
    async delete(path: string, options: RequestOptions = {}): Promise<HttpResult> {
      return request('DELETE', path, options);
    },
  };
}
