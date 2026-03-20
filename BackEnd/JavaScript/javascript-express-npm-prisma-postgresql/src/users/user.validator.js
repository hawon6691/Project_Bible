export function validateUpdateMe(req) {
  const { name, phone, password } = req.body ?? {};
  return name || phone || password ? null : "At least one field is required";
}

export function validateUpdateUserStatus(req) {
  return req.body?.status ? null : "status is required";
}

export function validateUpdateMyProfile(req) {
  const { nickname, bio } = req.body ?? {};
  return nickname || bio !== undefined ? null : "nickname or bio is required";
}
