import { randomUUID } from 'crypto';
import { Request, Response } from 'express';

export type TracedRequest = Request & { requestId?: string };

const REQUEST_ID_HEADER = 'x-request-id';

export function getOrCreateRequestId(
  request: Request,
  response?: Response,
): string {
  const tracedRequest = request as TracedRequest;

  if (tracedRequest.requestId) {
    return tracedRequest.requestId;
  }

  const incoming = request.headers[REQUEST_ID_HEADER];
  const requestId =
    typeof incoming === 'string' && incoming.trim().length > 0
      ? incoming.trim()
      : generateRequestId();

  tracedRequest.requestId = requestId;
  response?.setHeader('X-Request-Id', requestId);

  return requestId;
}

function generateRequestId(): string {
  try {
    return randomUUID();
  } catch {
    // randomUUID 미지원 런타임 대응 fallback
    const random = Math.random().toString(36).slice(2, 10);
    return `${Date.now().toString(36)}-${random}`;
  }
}
