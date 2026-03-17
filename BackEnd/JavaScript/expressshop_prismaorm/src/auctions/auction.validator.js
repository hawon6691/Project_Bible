export function validateCreateAuction(req) {
  const { title, description, categoryId } = req.body ?? {};
  return title && description && categoryId ? null : "title, description, categoryId are required";
}

export function validateCreateAuctionBid(req) {
  const { price, deliveryDays } = req.body ?? {};
  return price !== undefined && deliveryDays !== undefined ? null : "price and deliveryDays are required";
}

export function validateUpdateAuctionBid(req) {
  const { price, description, deliveryDays } = req.body ?? {};
  return price !== undefined || description !== undefined || deliveryDays !== undefined
    ? null
    : "At least one field is required";
}
