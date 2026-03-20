export function validateCreateInquiry(req) {
  const { title, content } = req.body ?? {};
  if (!title || !content) return "title and content are required";
  return null;
}
