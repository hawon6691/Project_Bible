export function validateCreateRoom(req) {
  const name = req.body?.name?.trim();
  return name ? null : "name is required";
}

export function validateSendMessage(req) {
  const message = req.body?.message?.trim();
  return message ? null : "message is required";
}
