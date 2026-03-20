function isPresent(value) {
  return value !== undefined && value !== null;
}

export function validateUpdateExtensions(req) {
  const { extensions } = req.body ?? {};
  if (!Array.isArray(extensions) || extensions.length === 0) {
    return "extensions must be a non-empty array";
  }

  return extensions.every((item) => String(item ?? "").trim() !== "")
    ? null
    : "extensions must not contain empty values";
}

export function validateUpdateUploadLimits(req) {
  const { image, video, audio } = req.body ?? {};
  if (!isPresent(image) || !isPresent(video) || !isPresent(audio)) {
    return "image, video, audio are required";
  }

  const values = { image, video, audio };
  for (const [key, value] of Object.entries(values)) {
    const parsed = Number(value);
    if (!Number.isInteger(parsed) || parsed <= 0) {
      return `${key} must be a positive integer`;
    }
  }

  return null;
}

export function validateUpdateReviewPolicy(req) {
  const { maxImageCount, pointAmount } = req.body ?? {};
  if (!isPresent(maxImageCount) || !isPresent(pointAmount)) {
    return "maxImageCount and pointAmount are required";
  }

  const values = { maxImageCount, pointAmount };
  for (const [key, value] of Object.entries(values)) {
    const parsed = Number(value);
    if (!Number.isInteger(parsed) || parsed <= 0) {
      return `${key} must be a positive integer`;
    }
  }

  return null;
}
