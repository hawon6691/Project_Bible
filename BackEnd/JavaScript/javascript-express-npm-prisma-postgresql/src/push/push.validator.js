export function validateRegisterPushSubscription(req) {
  const { endpoint, p256dhKey, authKey } = req.body ?? {};
  return endpoint && p256dhKey && authKey ? null : "endpoint, p256dhKey and authKey are required";
}

export function validateUnregisterPushSubscription(req) {
  return req.body?.endpoint ? null : "endpoint is required";
}

export function validateUpdatePushPreference(req) {
  const body = req.body ?? {};
  const keys = ["priceAlertEnabled", "orderStatusEnabled", "chatMessageEnabled", "dealEnabled"];
  const provided = keys.filter((key) => body[key] !== undefined);

  if (provided.length === 0) {
    return "At least one preference field is required";
  }

  const invalid = provided.find((key) => typeof body[key] !== "boolean");
  return invalid ? `${invalid} must be boolean` : null;
}
