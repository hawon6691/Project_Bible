import {
  getMyPushPreference,
  getMyPushSubscriptions,
  registerPushSubscription,
  unregisterPushSubscription,
  updateMyPushPreference,
} from "./push.service.js";
import { success } from "../utils/response.js";

export async function registerPushSubscriptionController(req, res) {
  const data = await registerPushSubscription(req.user.id, req.body);
  res.status(201).json(success(data));
}

export async function unregisterPushSubscriptionController(req, res) {
  const data = await unregisterPushSubscription(req.user.id, req.body);
  res.status(200).json(success(data));
}

export async function getMyPushSubscriptionsController(req, res) {
  const data = await getMyPushSubscriptions(req.user.id);
  res.status(200).json(success(data));
}

export async function getMyPushPreferenceController(req, res) {
  const data = await getMyPushPreference(req.user.id);
  res.status(200).json(success(data));
}

export async function updateMyPushPreferenceController(req, res) {
  const data = await updateMyPushPreference(req.user.id, req.body ?? {});
  res.status(200).json(success(data));
}
