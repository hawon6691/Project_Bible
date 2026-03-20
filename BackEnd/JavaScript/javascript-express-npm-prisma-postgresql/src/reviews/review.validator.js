export function validateCreateReview(req) {
  const { orderId, rating, content } = req.body ?? {};
  if (!orderId || !rating || !content) return "orderId, rating, content are required";
  return null;
}
