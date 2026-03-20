export function validateCreateSeller(req) {
  const { name, url } = req.body ?? {};
  return name && url ? null : "name and url are required";
}

export function validateUpdateSeller(req) {
  const { name, url, logoUrl, trustScore, trustGrade, description, isActive } = req.body ?? {};
  return (
    name !== undefined ||
    url !== undefined ||
    logoUrl !== undefined ||
    trustScore !== undefined ||
    trustGrade !== undefined ||
    description !== undefined ||
    isActive !== undefined
  )
    ? null
    : "At least one field is required";
}

function isRating(value) {
  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed >= 1 && parsed <= 5;
}

export function validateCreateSellerReview(req) {
  const { orderId, rating, deliveryRating, content } = req.body ?? {};
  if (!orderId || !rating || !deliveryRating || !content) {
    return "orderId, rating, deliveryRating, content are required";
  }
  if (!isRating(rating)) return "rating must be between 1 and 5";
  if (!isRating(deliveryRating)) return "deliveryRating must be between 1 and 5";
  return null;
}

export function validateUpdateSellerReview(req) {
  const { rating, deliveryRating, content } = req.body ?? {};
  if (rating === undefined && deliveryRating === undefined && content === undefined) {
    return "At least one field is required";
  }
  if (rating !== undefined && !isRating(rating)) return "rating must be between 1 and 5";
  if (deliveryRating !== undefined && !isRating(deliveryRating)) return "deliveryRating must be between 1 and 5";
  if (content !== undefined && !String(content).trim()) return "content must not be empty";
  return null;
}
