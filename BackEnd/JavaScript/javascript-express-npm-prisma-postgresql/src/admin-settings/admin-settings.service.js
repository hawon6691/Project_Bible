import { badRequest } from "../utils/http-error.js";
import { findAdminSettingByKey, upsertAdminSetting } from "./admin-settings.repository.js";

const SETTING_KEYS = {
  extensions: "extensions",
  uploadLimits: "upload_limits",
  reviewPolicy: "review_policy",
};

const SETTING_DESCRIPTIONS = {
  [SETTING_KEYS.extensions]: "Allowed upload extensions",
  [SETTING_KEYS.uploadLimits]: "Upload limits by media type in MB",
  [SETTING_KEYS.reviewPolicy]: "Review media and point policy",
};

const DEFAULT_EXTENSIONS = ["jpg", "png", "mp4", "mp3"];
const DEFAULT_UPLOAD_LIMITS = { image: 5, video: 100, audio: 20 };
const DEFAULT_REVIEW_POLICY = { maxImageCount: 10, pointAmount: 500 };

function normalizeExtensions(extensions) {
  const normalized = [...new Set(
    extensions.map((item) => String(item).trim().replace(/^\./, "").toLowerCase()).filter(Boolean),
  )];

  if (normalized.length === 0) {
    throw badRequest("extensions must contain at least one value");
  }

  return normalized;
}

function normalizePositiveInteger(value, fieldName) {
  const parsed = Number(value);
  if (!Number.isInteger(parsed) || parsed <= 0) {
    throw badRequest(`${fieldName} must be a positive integer`);
  }
  return parsed;
}

async function readSetting(settingKey, fallbackValue) {
  const item = await findAdminSettingByKey(settingKey);
  return item?.settingValue ?? fallbackValue;
}

function readPositiveIntegerCandidate(value, fallbackValue) {
  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : fallbackValue;
}

function toExtensionsResponse(value) {
  if (Array.isArray(value)) {
    return { extensions: normalizeExtensions(value) };
  }

  if (Array.isArray(value?.extensions)) {
    return { extensions: normalizeExtensions(value.extensions) };
  }

  return { extensions: DEFAULT_EXTENSIONS };
}

function toUploadLimitsResponse(value) {
  const source = value && typeof value === "object" ? value : {};
  return {
    image: readPositiveIntegerCandidate(source.image ?? source.imageMb, DEFAULT_UPLOAD_LIMITS.image),
    video: readPositiveIntegerCandidate(source.video ?? source.videoMb, DEFAULT_UPLOAD_LIMITS.video),
    audio: readPositiveIntegerCandidate(source.audio ?? source.audioMb, DEFAULT_UPLOAD_LIMITS.audio),
  };
}

function toReviewPolicyResponse(value) {
  const source = value && typeof value === "object" ? value : {};
  return {
    maxImageCount: readPositiveIntegerCandidate(
      source.maxImageCount ?? source.maxImages,
      DEFAULT_REVIEW_POLICY.maxImageCount,
    ),
    pointAmount: readPositiveIntegerCandidate(source.pointAmount, DEFAULT_REVIEW_POLICY.pointAmount),
  };
}

export async function getAdminExtensions() {
  const value = await readSetting(SETTING_KEYS.extensions, DEFAULT_EXTENSIONS);
  return toExtensionsResponse(value);
}

export async function updateAdminExtensions(payload, updatedBy) {
  const extensions = normalizeExtensions(payload.extensions ?? []);

  await upsertAdminSetting(
    SETTING_KEYS.extensions,
    extensions,
    updatedBy,
    SETTING_DESCRIPTIONS[SETTING_KEYS.extensions],
  );

  return { extensions };
}

export async function getAdminUploadLimits() {
  const value = await readSetting(SETTING_KEYS.uploadLimits, DEFAULT_UPLOAD_LIMITS);
  return toUploadLimitsResponse(value);
}

export async function updateAdminUploadLimits(payload, updatedBy) {
  const value = {
    image: normalizePositiveInteger(payload.image, "image"),
    video: normalizePositiveInteger(payload.video, "video"),
    audio: normalizePositiveInteger(payload.audio, "audio"),
  };

  await upsertAdminSetting(
    SETTING_KEYS.uploadLimits,
    value,
    updatedBy,
    SETTING_DESCRIPTIONS[SETTING_KEYS.uploadLimits],
  );

  return value;
}

export async function getAdminReviewPolicy() {
  const value = await readSetting(SETTING_KEYS.reviewPolicy, DEFAULT_REVIEW_POLICY);
  return toReviewPolicyResponse(value);
}

export async function updateAdminReviewPolicy(payload, updatedBy) {
  const value = {
    maxImageCount: normalizePositiveInteger(payload.maxImageCount, "maxImageCount"),
    pointAmount: normalizePositiveInteger(payload.pointAmount, "pointAmount"),
  };

  await upsertAdminSetting(
    SETTING_KEYS.reviewPolicy,
    value,
    updatedBy,
    SETTING_DESCRIPTIONS[SETTING_KEYS.reviewPolicy],
  );

  return value;
}
