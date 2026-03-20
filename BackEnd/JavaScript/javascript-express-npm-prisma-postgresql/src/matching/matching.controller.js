import {
  approveMapping,
  autoMatch,
  getMatchingStats,
  getPendingMappings,
  rejectMapping,
} from "./matching.service.js";
import { toMappingDto } from "./matching.mapper.js";
import { success } from "../utils/response.js";

export async function getPendingMappingsController(req, res) {
  const { items, meta } = await getPendingMappings(req.query);
  res.status(200).json(success(items.map(toMappingDto), meta));
}

export async function approveMappingController(req, res) {
  const data = await approveMapping(req.params.id, req.user.id, req.body);
  res.status(200).json(success(toMappingDto(data)));
}

export async function rejectMappingController(req, res) {
  const data = await rejectMapping(req.params.id, req.user.id, req.body);
  res.status(200).json(success(toMappingDto(data)));
}

export async function autoMatchController(req, res) {
  const data = await autoMatch(req.user.id);
  res.status(200).json(success(data));
}

export async function getMatchingStatsController(_req, res) {
  const data = await getMatchingStats();
  res.status(200).json(success(data));
}
