export function success(data, meta) {
  return {
    success: true,
    data,
    ...(meta ? { meta } : {}),
  };
}

export function failure(code, message, details) {
  return {
    success: false,
    error: {
      code,
      message,
      ...(details ? { details } : {}),
    },
  };
}
