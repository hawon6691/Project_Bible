export function validateApproveMapping(req) {
  return req.body?.productId ? null : "productId is required";
}

export function validateRejectMapping(req) {
  return req.body?.reason ? null : "reason is required";
}
