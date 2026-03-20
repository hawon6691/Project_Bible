export function validateCreatePost(req) {
  const { boardId, title, content } = req.body ?? {};
  if (!boardId || !title || !content) return "boardId, title, content are required";
  return null;
}

export function validateCreateComment(req) {
  return req.body?.content ? null : "content is required";
}
