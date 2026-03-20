export function validateCreateFaq(req) {
  const { category, question, answer } = req.body ?? {};
  if (!category || !question || !answer) return "category, question, answer are required";
  return null;
}
