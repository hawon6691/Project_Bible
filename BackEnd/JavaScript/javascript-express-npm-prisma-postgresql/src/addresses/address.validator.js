export function validateAddress(req) {
  const { label, recipientName, phone, zipCode, address } = req.body ?? {};
  if (!label || !recipientName || !phone || !zipCode || !address) {
    return "label, recipientName, phone, zipCode, address are required";
  }
  return null;
}
