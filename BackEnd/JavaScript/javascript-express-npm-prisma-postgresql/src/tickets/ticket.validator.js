export function validateCreateTicket(req) {
  const { category, title, content } = req.body ?? {};
  if (!category || !title || !content) return "category, title, content are required";
  return null;
}

export function validateCreateTicketReply(req) {
  return req.body?.content ? null : "content is required";
}

export function validateTicketStatus(req) {
  return req.body?.status ? null : "status is required";
}
