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

export function notFound(message = "Resource not found") {
  return new HttpError(404, "NOT_FOUND", message);
}
