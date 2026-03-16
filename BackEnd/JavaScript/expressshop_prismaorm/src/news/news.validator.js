export function validateCreateNews(req) {
  const { title, content, categoryId } = req.body ?? {};
  if (!title || !content || !categoryId) return "title, content, categoryId are required";
  return null;
}

export function validateCreateNewsCategory(req) {
  const { name, slug } = req.body ?? {};
  if (!name || !slug) return "name and slug are required";
  return null;
}
