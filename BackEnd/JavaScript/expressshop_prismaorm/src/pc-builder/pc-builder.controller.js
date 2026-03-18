import { success } from "../utils/response.js";
import {
  addPcBuildPart,
  createCompatibilityRule,
  createPcBuild,
  deleteCompatibilityRule,
  deletePcBuild,
  getCompatibilityRules,
  getMyPcBuilds,
  getPcBuild,
  getPcBuildCompatibility,
  getPopularPcBuilds,
  getSharedPcBuild,
  removePcBuildPart,
  sharePcBuild,
  updateCompatibilityRule,
  updatePcBuild,
} from "./pc-builder.service.js";

export async function getMyPcBuildsController(req, res) {
  const { items, meta } = await getMyPcBuilds(req.user.id, req.query);
  res.status(200).json(success(items, meta));
}

export async function createPcBuildController(req, res) {
  const data = await createPcBuild(req.user.id, req.body);
  res.status(201).json(success(data));
}

export async function getPcBuildController(req, res) {
  const data = await getPcBuild(req.params.id);
  res.status(200).json(success(data));
}

export async function updatePcBuildController(req, res) {
  const data = await updatePcBuild(req.user.id, req.params.id, req.body);
  res.status(200).json(success(data));
}

export async function deletePcBuildController(req, res) {
  const data = await deletePcBuild(req.user.id, req.params.id);
  res.status(200).json(success(data));
}

export async function addPcBuildPartController(req, res) {
  const data = await addPcBuildPart(req.user.id, req.params.id, req.body);
  res.status(200).json(success(data));
}

export async function removePcBuildPartController(req, res) {
  const data = await removePcBuildPart(req.user.id, req.params.id, req.params.partId);
  res.status(200).json(success(data));
}

export async function getPcBuildCompatibilityController(req, res) {
  const data = await getPcBuildCompatibility(req.params.id);
  res.status(200).json(success(data));
}

export async function sharePcBuildController(req, res) {
  const data = await sharePcBuild(req.user.id, req.params.id);
  res.status(200).json(success(data));
}

export async function getSharedPcBuildController(req, res) {
  const data = await getSharedPcBuild(req.params.shareCode);
  res.status(200).json(success(data));
}

export async function getPopularPcBuildsController(req, res) {
  const { items, meta } = await getPopularPcBuilds(req.query);
  res.status(200).json(success(items, meta));
}

export async function getCompatibilityRulesController(_req, res) {
  const data = await getCompatibilityRules();
  res.status(200).json(success(data));
}

export async function createCompatibilityRuleController(req, res) {
  const data = await createCompatibilityRule(req.body);
  res.status(201).json(success(data));
}

export async function updateCompatibilityRuleController(req, res) {
  const data = await updateCompatibilityRule(req.params.id, req.body);
  res.status(200).json(success(data));
}

export async function deleteCompatibilityRuleController(req, res) {
  const data = await deleteCompatibilityRule(req.params.id);
  res.status(200).json(success(data));
}
