import { createRequestId, recordHttpTrace } from "../observability/observability.service.js";

export function observabilityTrace(req, res, next) {
  const requestId = createRequestId();
  const startedAt = Date.now();

  res.setHeader("x-request-id", requestId);

  res.on("finish", () => {
    recordHttpTrace({
      requestId,
      method: req.method,
      path: req.originalUrl?.split("?")[0] ?? req.path,
      statusCode: res.statusCode,
      durationMs: Date.now() - startedAt,
      timestamp: new Date().toISOString(),
      userAgent: req.get("user-agent") ?? null,
      ip: req.ip ?? null,
    });
  });

  next();
}
