export function validateCreateCategory(req) {
  return req.body?.name ? null : "name is required";
}

export function validateUpdateCategory(req) {
  const { name, parentId, sortOrder } = req.body ?? {};
  return name !== undefined || parentId !== undefined || sortOrder !== undefined
    ? null
    : "At least one field is required";
}
