import {
  createPresignedUrl,
  deleteMedia,
  getMediaMetadata,
  streamMedia,
  uploadMedia,
} from "./media.service.js";
import { toMediaAssetDto } from "./media.mapper.js";
import { success } from "../utils/response.js";

export async function uploadMediaController(req, res) {
  const data = await uploadMedia(req.user.id, req.body);
  res.status(201).json(success(data.map(toMediaAssetDto)));
}

export async function createPresignedUrlController(req, res) {
  const data = await createPresignedUrl(req.body);
  res.status(201).json(success(data));
}

export async function getMediaMetadataController(req, res) {
  const data = await getMediaMetadata(req.params.id);
  res.status(200).json(success(data));
}

export async function deleteMediaController(req, res) {
  const data = await deleteMedia(req.user, req.params.id);
  res.status(200).json(success(data));
}

export async function streamMediaController(req, res) {
  const data = await streamMedia(req.params.id);
  res.status(206).json(success(toMediaAssetDto(data)));
}
