export function validateCreateSeller(req) {
  const { name, url } = req.body ?? {};
  return name && url ? null : "name and url are required";
}

export function validateUpdateSeller(req) {
  const { name, url, logoUrl, trustScore, trustGrade, description, isActive } = req.body ?? {};
  return (
    name !== undefined ||
    url !== undefined ||
    logoUrl !== undefined ||
    trustScore !== undefined ||
    trustGrade !== undefined ||
    description !== undefined ||
    isActive !== undefined
  )
    ? null
    : "At least one field is required";
}
