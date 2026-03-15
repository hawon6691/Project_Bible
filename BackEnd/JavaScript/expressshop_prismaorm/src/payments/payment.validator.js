export function validateCreatePayment(req) {
  const { orderId, method, amount } = req.body ?? {};
  if (!orderId || !method || !amount) return "orderId, method, amount are required";
  return null;
}
