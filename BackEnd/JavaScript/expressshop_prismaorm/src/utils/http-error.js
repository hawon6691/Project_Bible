export class HttpError extends Error {
  constructor(status, code, message, details) {
    super(message);
    this.status = status;
    this.code = code;
    this.details = details;
  }
}

export function badRequest(message, details) {
  return new HttpError(400, "BAD_REQUEST", message, details);
}

export function unauthorized(message = "Authentication required") {
  return new HttpError(401, "UNAUTHORIZED", message);
}

export function forbidden(message = "Forbidden") {
  return new HttpError(403, "FORBIDDEN", message);
}

export function conflict(message = "Conflict") {
  return new HttpError(409, "CONFLICT", message);
}

export function gone(message = "Gone") {
  return new HttpError(410, "GONE", message);
}

export function tooManyRequests(message = "Too many requests") {
  return new HttpError(429, "TOO_MANY_REQUESTS", message);
}

export function notFound(message = "Resource not found") {
  return new HttpError(404, "NOT_FOUND", message);
}
