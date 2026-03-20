import {
  createBadgeItem,
  deleteBadgeItem,
  getBadges,
  getMyBadges,
  getUserBadges,
  grantBadgeItem,
  revokeBadgeItem,
  updateBadgeItem,
} from "./badge.service.js";
import { success } from "../utils/response.js";

export async function getBadgesController(_req, res) {
  const data = await getBadges();
  res.status(200).json(success(data));
}

export async function getMyBadgesController(req, res) {
  const data = await getMyBadges(req.user.id);
  res.status(200).json(success(data));
}

export async function getUserBadgesController(req, res) {
  const data = await getUserBadges(req.params.id);
  res.status(200).json(success(data));
}

export async function createBadgeController(req, res) {
  const data = await createBadgeItem(req.body);
  res.status(201).json(success(data));
}

export async function updateBadgeController(req, res) {
  const data = await updateBadgeItem(req.params.id, req.body ?? {});
  res.status(200).json(success(data));
}

export async function deleteBadgeController(req, res) {
  const data = await deleteBadgeItem(req.params.id);
  res.status(200).json(success(data));
}

export async function grantBadgeController(req, res) {
  const data = await grantBadgeItem(req.user.id, req.params.id, req.body);
  res.status(201).json(success(data));
}

export async function revokeBadgeController(req, res) {
  const data = await revokeBadgeItem(req.params.id, req.params.userId);
  res.status(200).json(success(data));
}
