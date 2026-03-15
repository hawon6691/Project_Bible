export function validateUpdateMe(req) {
  const { name, phone, password } = req.body ?? {};
  return name || phone || password ? null : "At least one field is required";
}
