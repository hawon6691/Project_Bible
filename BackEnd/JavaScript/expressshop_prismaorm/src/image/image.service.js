import crypto from "crypto";

import { badRequest, notFound } from "../utils/http-error.js";
import {
  createImageAssetWithVariants,
  deleteImageAssetWithVariants,
  findImageAssetById,
  findImageVariants,
} from "./image.repository.js";

const ALLOWED_CATEGORIES = new Set(["product", "community", "support", "seller"]);
const ALLOWED_MIME_TYPES = new Set(["image/jpeg", "image/png", "image/webp", "image/gif"]);

function getExtension(filename) {
  const parts = String(filename ?? "").split(".");
  return parts.length > 1 ? parts.at(-1).toLowerCase() : "bin";
}

function createStoredToken() {
  return `${Date.now()}-${crypto.randomBytes(4).toString("hex")}`;
}

function toVariantDto(item) {
  return {
    id: item.id,
    type: item.type,
    url: item.url,
    width: item.width,
    height: item.height,
    format: item.format,
    size: item.size,
    createdAt: item.createdAt,
  };
}

function buildVariants(token, size) {
  return [
    {
      type: "THUMBNAIL",
      url: `/uploads/thumb/${token}.webp`,
      format: "WEBP",
      width: 200,
      height: 200,
      size: Math.max(1, Math.floor(size * 0.12)),
    },
    {
      type: "MEDIUM",
      url: `/uploads/medium/${token}.webp`,
      format: "WEBP",
      width: 600,
      height: 600,
      size: Math.max(1, Math.floor(size * 0.35)),
    },
    {
      type: "LARGE",
      url: `/uploads/large/${token}.webp`,
      format: "WEBP",
      width: 1200,
      height: 1200,
      size: Math.max(1, Math.floor(size * 0.65)),
    },
  ];
}

export async function uploadImage(userId, file, body) {
  if (!file) {
    throw badRequest("file is required");
  }
  if (!ALLOWED_MIME_TYPES.has(file.mimetype)) {
    throw badRequest("file type is not allowed");
  }
  if (!ALLOWED_CATEGORIES.has(body?.category)) {
    throw badRequest("category is invalid");
  }

  const token = createStoredToken();
  const storedFilename = `${token}.${getExtension(file.originalname)}`;
  const image = await createImageAssetWithVariants(
    {
      uploadedByUserId: Number(userId),
      originalFilename: file.originalname,
      storedFilename,
      originalUrl: `/uploads/original/${storedFilename}`,
      mimeType: file.mimetype,
      size: Number(file.size ?? 0),
      category: body.category,
      processingStatus: "PROCESSING",
    },
    buildVariants(token, Number(file.size ?? 0)),
  );

  return {
    id: image.id,
    originalUrl: image.originalUrl,
    variants: image.variants.map(toVariantDto),
    processingStatus: image.processingStatus,
  };
}

export async function uploadImageWithCategory(userId, file, category) {
  return uploadImage(userId, file, { category });
}

export async function getImageVariants(imageId) {
  const image = await findImageAssetById(imageId);
  if (!image) {
    throw notFound("Image not found");
  }

  const variants = await findImageVariants(imageId);
  return variants.map(toVariantDto);
}

export async function deleteImage(imageId) {
  const image = await findImageAssetById(imageId);
  if (!image) {
    throw notFound("Image not found");
  }

  await deleteImageAssetWithVariants(imageId);
  return { message: "Image deleted" };
}
