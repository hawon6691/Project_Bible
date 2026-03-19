import { getErrorCodeItem } from "../error-codes/error-code.catalog.js";

export class HttpError extends Error {
  constructor(status, code, message, details) {
    super(message);
    this.status = status;
    this.code = code;
    this.details = details;
  }
}

function createHttpError(status, key, fallbackMessage, details) {
  const item = getErrorCodeItem(key);
  return new HttpError(status, key, fallbackMessage ?? item?.message ?? key, details);
}

export function badRequest(message, details) {
  return createHttpError(400, "BAD_REQUEST", message, details);
}

export function unauthorized(message = "Authentication required") {
  return createHttpError(401, "UNAUTHORIZED", message);
}

export function forbidden(message = "Forbidden") {
  return createHttpError(403, "FORBIDDEN", message);
}

export function conflict(message = "Conflict") {
  return createHttpError(409, "CONFLICT", message);
}

export function gone(message = "Gone") {
  return createHttpError(410, "GONE", message);
}

export function tooManyRequests(message = "Too many requests") {
  return createHttpError(429, "TOO_MANY_REQUESTS", message);
}

export function notFound(message = "Resource not found") {
  return createHttpError(404, "NOT_FOUND", message);
}
