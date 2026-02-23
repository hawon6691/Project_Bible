import { INestApplication } from '@nestjs/common';

export interface HttpResult {
  status: number;
  body: any;
}

export async function startTestServer(app: INestApplication) {
  await app.listen(0);
  const server = app.getHttpServer();
  const address = server.address() as { port: number };
  const baseUrl = `http://127.0.0.1:${address.port}`;

  return {
    async get(path: string): Promise<HttpResult> {
      const res = await fetch(`${baseUrl}${path}`);
      return { status: res.status, body: await res.json() };
    },
    async post(path: string, payload: unknown): Promise<HttpResult> {
      const res = await fetch(`${baseUrl}${path}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
      return { status: res.status, body: await res.json() };
    },
  };
}
