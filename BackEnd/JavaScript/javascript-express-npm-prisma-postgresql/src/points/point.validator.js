export function validateGrantPoints(req) {
  const { userId, amount, description } = req.body ?? {};
  if (!userId || !amount || !description) return "userId, amount, description are required";
  return null;
}
