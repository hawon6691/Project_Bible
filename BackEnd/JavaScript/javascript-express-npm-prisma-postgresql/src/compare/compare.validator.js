export function validateAddCompareItem(req) {
  return req.body?.productId ? null : "productId is required";
}
