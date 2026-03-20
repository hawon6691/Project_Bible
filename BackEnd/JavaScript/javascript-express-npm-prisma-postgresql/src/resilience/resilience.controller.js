import { success } from "../utils/response.js";
import {
  getCircuitBreakerPolicies,
  getCircuitBreakerSnapshot,
  getCircuitBreakerSnapshots,
  resetCircuitBreaker,
} from "./resilience.service.js";

export async function getCircuitBreakerSnapshotsController(_req, res) {
  res.status(200).json(success({ items: getCircuitBreakerSnapshots() }));
}

export async function getCircuitBreakerPoliciesController(_req, res) {
  res.status(200).json(success({ items: getCircuitBreakerPolicies() }));
}

export async function getCircuitBreakerSnapshotController(req, res) {
  res.status(200).json(success(getCircuitBreakerSnapshot(req.params.name)));
}

export async function resetCircuitBreakerController(req, res) {
  res.status(200).json(success(resetCircuitBreaker(req.params.name)));
}
