export function validateAddCartItem(req) {
  const { productId, quantity } = req.body ?? {};
  if (!productId || !quantity) return "productId and quantity are required";
  return null;
}

export function validateUpdateCartItem(req) {
  const quantity = Number(req.body?.quantity);
  return quantity > 0 ? null : "quantity must be greater than 0";
}
