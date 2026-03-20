export function validateCreateRecommendation(req) {
  const { productId, type } = req.body ?? {};
  if (!productId || !type) return "productId and type are required";
  return null;
}
