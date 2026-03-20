export function validateCreateDeal(req) {
  const { title, startDate, endDate } = req.body ?? {};
  if (!title || !startDate || !endDate) return "title, startDate, endDate are required";
  return null;
}
