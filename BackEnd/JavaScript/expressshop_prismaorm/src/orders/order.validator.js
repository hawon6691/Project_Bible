export function validateCreateOrder(req) {
  const { recipientName, recipientPhone, zipCode, address, items } = req.body ?? {};
  if (!recipientName || !recipientPhone || !zipCode || !address || !Array.isArray(items) || items.length === 0) {
    return "recipient info and items are required";
  }
  return null;
}

export function validateOrderStatus(req) {
  return req.body?.status ? null : "status is required";
}
