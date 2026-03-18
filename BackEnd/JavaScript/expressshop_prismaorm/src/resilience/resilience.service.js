import { notFound } from "../utils/http-error.js";

const DEFAULT_OPTIONS = {
  failureThreshold: 3,
  openTimeoutMs: 5000,
  halfOpenSuccessThreshold: 2,
};

const DEFAULT_BREAKERS = [
  { name: "search-sync", status: "CLOSED", failureCount: 0, successCount: 0, nextAttemptAt: null, lastFailureReason: null },
  { name: "crawler", status: "HALF_OPEN", failureCount: 1, successCount: 0, nextAttemptAt: null, lastFailureReason: "Recent crawl timeout" },
  { name: "payment", status: "CLOSED", failureCount: 0, successCount: 0, nextAttemptAt: null, lastFailureReason: null },
];

const states = new Map(
  DEFAULT_BREAKERS.map((item) => [
    item.name,
    {
      status: item.status,
      failureCount: item.failureCount,
      successCount: item.successCount,
      nextAttemptAt: item.nextAttemptAt,
      lastFailureReason: item.lastFailureReason,
    },
  ]),
);

const adaptiveOptions = new Map([
  [
    "crawler",
    {
      failureThreshold: 2,
      openTimeoutMs: 7500,
      halfOpenSuccessThreshold: 2,
    },
  ],
]);

const healthStats = new Map([
  ["search-sync", { success: 12, failure: 1, lastTunedAt: 0 }],
  ["crawler", { success: 8, failure: 4, lastTunedAt: Date.now() - 60000 }],
  ["payment", { success: 15, failure: 0, lastTunedAt: 0 }],
]);

function getDefaultOptions() {
  return { ...DEFAULT_OPTIONS };
}

function getOrCreateState(name) {
  const key = String(name).trim();
  let state = states.get(key);
  if (!state) {
    state = {
      status: "CLOSED",
      failureCount: 0,
      successCount: 0,
      nextAttemptAt: null,
      lastFailureReason: null,
    };
    states.set(key, state);
  }
  return state;
}

function getEffectiveOptions(name) {
  return adaptiveOptions.get(name) ?? getDefaultOptions();
}

function toSnapshot(name, state) {
  return {
    name,
    status: state.status,
    failureCount: state.failureCount,
    successCount: state.successCount,
    nextAttemptAt: state.nextAttemptAt,
    lastFailureReason: state.lastFailureReason,
    options: getEffectiveOptions(name),
  };
}

export function getCircuitBreakerSnapshots() {
  return [...states.entries()].map(([name, state]) => toSnapshot(name, state));
}

export function getCircuitBreakerSnapshot(name) {
  const key = String(name).trim();
  if (!states.has(key)) {
    throw notFound("Circuit breaker not found");
  }

  return toSnapshot(key, getOrCreateState(key));
}

export function getCircuitBreakerPolicies() {
  return [...states.keys()].map((name) => ({
    name,
    options: getEffectiveOptions(name),
    stats: healthStats.get(name) ?? { success: 0, failure: 0, lastTunedAt: 0 },
  }));
}

export function resetCircuitBreaker(name) {
  const key = String(name).trim();
  const state = getOrCreateState(key);
  state.status = "CLOSED";
  state.failureCount = 0;
  state.successCount = 0;
  state.nextAttemptAt = null;
  state.lastFailureReason = null;

  return {
    message: "Circuit Breaker reset",
    name: key,
  };
}
