import { success } from "../utils/response.js";
import {
  getAdminExtensions,
  getAdminReviewPolicy,
  getAdminUploadLimits,
  updateAdminExtensions,
  updateAdminReviewPolicy,
  updateAdminUploadLimits,
} from "./admin-settings.service.js";

export async function getAdminExtensionsController(_req, res) {
  const data = await getAdminExtensions();
  res.status(200).json(success(data));
}

export async function updateAdminExtensionsController(req, res) {
  const data = await updateAdminExtensions(req.body, req.user.id);
  res.status(200).json(success(data));
}

export async function getAdminUploadLimitsController(_req, res) {
  const data = await getAdminUploadLimits();
  res.status(200).json(success(data));
}

export async function updateAdminUploadLimitsController(req, res) {
  const data = await updateAdminUploadLimits(req.body, req.user.id);
  res.status(200).json(success(data));
}

export async function getAdminReviewPolicyController(_req, res) {
  const data = await getAdminReviewPolicy();
  res.status(200).json(success(data));
}

export async function updateAdminReviewPolicyController(req, res) {
  const data = await updateAdminReviewPolicy(req.body, req.user.id);
  res.status(200).json(success(data));
}
