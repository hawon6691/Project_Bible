export function validateSignup(req) {
  const { email, password, name, phone } = req.body ?? {};
  if (!email || !password || !name || !phone) return "email, password, name, phone are required";
  return null;
}

export function validateLogin(req) {
  const { email, password } = req.body ?? {};
  if (!email || !password) return "email and password are required";
  return null;
}

export function validateRefresh(req) {
  return req.body?.refreshToken ? null : "refreshToken is required";
}
