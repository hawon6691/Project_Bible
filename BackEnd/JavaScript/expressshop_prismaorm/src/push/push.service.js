import { badRequest } from "../utils/http-error.js";
import {
  createPushPreference,
  deactivatePushSubscription,
  findActivePushSubscriptionsByUserId,
  findPushPreferenceByUserId,
  savePushSubscription,
  updatePushPreference,
} from "./push.repository.js";

const DEFAULT_PREFERENCE = {
  priceAlertEnabled: true,
  orderStatusEnabled: true,
  chatMessageEnabled: true,
  dealEnabled: true,
};

function parseExpirationTime(expirationTime) {
  if (expirationTime == null || expirationTime === "") {
    return null;
  }

  const raw = String(expirationTime).trim();
  if (!/^\d+$/.test(raw)) {
    throw badRequest("expirationTime must be an epoch milliseconds string");
  }

  return BigInt(raw);
}

function toSubscriptionDto(item) {
  return {
    id: item.id,
    endpoint: item.endpoint,
    expirationTime: item.expirationTime == null ? null : item.expirationTime.toString(),
    isActive: item.isActive,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
  };
}

function toPreferenceDto(item) {
  return {
    id: item.id,
    priceAlertEnabled: item.priceAlertEnabled,
    orderStatusEnabled: item.orderStatusEnabled,
    chatMessageEnabled: item.chatMessageEnabled,
    dealEnabled: item.dealEnabled,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
  };
}

async function getOrCreatePreference(userId) {
  const existing = await findPushPreferenceByUserId(userId);
  if (existing) {
    return existing;
  }

  return createPushPreference(userId, DEFAULT_PREFERENCE);
}

export async function registerPushSubscription(userId, payload) {
  const item = await savePushSubscription(userId, {
    endpoint: String(payload.endpoint).trim(),
    p256dhKey: String(payload.p256dhKey).trim(),
    authKey: String(payload.authKey).trim(),
    expirationTime: parseExpirationTime(payload.expirationTime),
  });

  return toSubscriptionDto(item);
}

export async function unregisterPushSubscription(userId, payload) {
  const result = await deactivatePushSubscription(userId, String(payload.endpoint).trim());

  if (result.count === 0) {
    return { message: "Already unsubscribed or subscription not found" };
  }

  return { message: "Push subscription unsubscribed" };
}

export async function getMyPushSubscriptions(userId) {
  const items = await findActivePushSubscriptionsByUserId(userId);
  return items.map(toSubscriptionDto);
}

export async function getMyPushPreference(userId) {
  const item = await getOrCreatePreference(userId);
  return toPreferenceDto(item);
}

export async function updateMyPushPreference(userId, payload) {
  await getOrCreatePreference(userId);

  const updates = {};
  if (payload.priceAlertEnabled !== undefined) updates.priceAlertEnabled = payload.priceAlertEnabled;
  if (payload.orderStatusEnabled !== undefined) updates.orderStatusEnabled = payload.orderStatusEnabled;
  if (payload.chatMessageEnabled !== undefined) updates.chatMessageEnabled = payload.chatMessageEnabled;
  if (payload.dealEnabled !== undefined) updates.dealEnabled = payload.dealEnabled;

  const item = Object.keys(updates).length === 0
    ? await findPushPreferenceByUserId(userId)
    : await updatePushPreference(userId, updates);

  return toPreferenceDto(item);
}
