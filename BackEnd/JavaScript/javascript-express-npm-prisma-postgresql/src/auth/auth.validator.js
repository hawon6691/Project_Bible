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

export function validateVerifyEmail(req) {
  const { email, code } = req.body ?? {};
  return email && code ? null : "email and code are required";
}

export function validateResendVerification(req) {
  return req.body?.email ? null : "email is required";
}

export function validatePasswordResetRequest(req) {
  const { email, phone } = req.body ?? {};
  return email && phone ? null : "email and phone are required";
}

export function validatePasswordResetVerify(req) {
  const { email, code } = req.body ?? {};
  return email && code ? null : "email and code are required";
}

export function validatePasswordResetConfirm(req) {
  const { resetToken, newPassword } = req.body ?? {};
  return resetToken && newPassword ? null : "resetToken and newPassword are required";
}
