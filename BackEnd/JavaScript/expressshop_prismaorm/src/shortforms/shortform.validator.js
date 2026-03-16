export function validateCreateShortform(req) {
  const { title, videoUrl } = req.body ?? {};
  if (!title || !videoUrl) return "title and videoUrl are required";
  return null;
}

export function validateCreateShortformComment(req) {
  return req.body?.content ? null : "content is required";
}
