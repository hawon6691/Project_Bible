export function validateObservabilityTracesQuery(req) {
  const { limit, pathContains } = req.query ?? {};

  if (limit !== undefined) {
    const parsedLimit = Number(limit);
    if (!Number.isInteger(parsedLimit) || parsedLimit <= 0) {
      return "limit must be a positive integer";
    }
  }

  if (pathContains !== undefined && String(pathContains).trim() === "") {
    return "pathContains must not be empty";
  }

  return null;
}
