import { Router } from "express";

import {
  getMyPushPreferenceController,
  getMyPushSubscriptionsController,
  registerPushSubscriptionController,
  unregisterPushSubscriptionController,
  updateMyPushPreferenceController,
} from "../push/push.controller.js";
import { requireAuth } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";
import {
  validateRegisterPushSubscription,
  validateUnregisterPushSubscription,
  validateUpdatePushPreference,
} from "../push/push.validator.js";

const router = Router();

router.post(
  "/push/subscriptions",
  requireAuth,
  validate(validateRegisterPushSubscription),
  asyncHandler(registerPushSubscriptionController),
);
router.post(
  "/push/subscriptions/unsubscribe",
  requireAuth,
  validate(validateUnregisterPushSubscription),
  asyncHandler(unregisterPushSubscriptionController),
);
router.get("/push/subscriptions", requireAuth, asyncHandler(getMyPushSubscriptionsController));
router.get("/push/preferences", requireAuth, asyncHandler(getMyPushPreferenceController));
router.post(
  "/push/preferences",
  requireAuth,
  validate(validateUpdatePushPreference),
  asyncHandler(updateMyPushPreferenceController),
);

export default router;
