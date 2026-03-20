import {
  autoApproveMappings,
  countMappingStatuses,
  countPendingMappings,
  findMappingById,
  findPendingMappings,
  updateMapping,
} from "./matching.repository.js";
import { badRequest, notFound } from "../utils/http-error.js";

export async function getPendingMappings(query) {
  const page = Math.max(Number(query?.page ?? 1), 1);
  const limit = Math.min(Math.max(Number(query?.limit ?? 20), 1), 100);
  const [items, total] = await Promise.all([
    findPendingMappings(page, limit),
    countPendingMappings(),
  ]);
  return { items, meta: { total, page, limit } };
}

export async function approveMapping(id, adminUserId, payload) {
  if (!payload?.productId) throw badRequest("productId is required");
  const existing = await findMappingById(id);
  if (!existing) throw notFound("Mapping not found");
  return updateMapping(id, {
    productId: Number(payload.productId),
    status: "APPROVED",
    reviewedBy: adminUserId,
  });
}

export async function rejectMapping(id, adminUserId, payload) {
  const existing = await findMappingById(id);
  if (!existing) throw notFound("Mapping not found");
  return updateMapping(id, {
    status: "REJECTED",
    reviewedBy: adminUserId,
  });
}

export async function autoMatch(adminUserId) {
  const updated = await autoApproveMappings(adminUserId);
  const [, pendingCount] = await countMappingStatuses();
  return { matchedCount: updated.count, pendingCount };
}

export async function getMatchingStats() {
  const [approved, pending, rejected] = await countMappingStatuses();
  return { approved, pending, rejected };
}
