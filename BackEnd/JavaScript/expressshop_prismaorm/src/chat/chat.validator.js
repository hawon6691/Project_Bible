export function validateCreateRoom(req) {
  const name = req.body?.name?.trim();
  return name ? null : "name is required";
}
