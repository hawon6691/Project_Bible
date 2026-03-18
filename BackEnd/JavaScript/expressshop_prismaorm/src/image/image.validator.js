export function validateImageUploadBody(req) {
  const { category } = req.body ?? {};

  if (typeof category !== "string" || category.trim() === "") {
    return "category is required";
  }
  if (!["product", "community", "support", "seller"].includes(category)) {
    return "category is invalid";
  }

  return null;
}
