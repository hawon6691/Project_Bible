export const ERROR_CODES = {
  BAD_REQUEST: {
    code: "HTTP_400",
    message: "The request payload or parameters are invalid.",
  },
  UNAUTHORIZED: {
    code: "HTTP_401",
    message: "Authentication is required to access this resource.",
  },
  FORBIDDEN: {
    code: "HTTP_403",
    message: "You do not have permission to access this resource.",
  },
  NOT_FOUND: {
    code: "HTTP_404",
    message: "The requested resource could not be found.",
  },
  CONFLICT: {
    code: "HTTP_409",
    message: "The request conflicts with the current resource state.",
  },
  GONE: {
    code: "HTTP_410",
    message: "The requested resource is no longer available.",
  },
  TOO_MANY_REQUESTS: {
    code: "HTTP_429",
    message: "Too many requests were sent in a short period of time.",
  },
  INTERNAL_SERVER_ERROR: {
    code: "HTTP_500",
    message: "An unexpected server error occurred.",
  },
};

export const ERROR_CODE_CATALOG = Object.entries(ERROR_CODES)
  .map(([key, value]) => ({
    key,
    code: value.code,
    message: value.message,
  }))
  .sort((left, right) => left.code.localeCompare(right.code));

export function getErrorCodeItem(key) {
  if (!key) {
    return null;
  }

  const normalizedKey = String(key).trim().toUpperCase();
  const item = ERROR_CODES[normalizedKey];

  if (!item) {
    return null;
  }

  return {
    key: normalizedKey,
    code: item.code,
    message: item.message,
  };
}
