import { Router } from "express";

import {
  autoRetryFailedController,
  getFailedJobsController,
  getQueueStatsController,
  getSupportedQueuesController,
  removeJobController,
  retryFailedJobsController,
  retryJobController,
} from "../queue-admin/queue-admin.controller.js";
import {
  validateAutoRetryQuery,
  validateFailedJobsQuery,
  validateRetryFailedJobsQuery,
} from "../queue-admin/queue-admin.validator.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.get(
  "/admin/queues/supported",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(getSupportedQueuesController),
);
router.get(
  "/admin/queues/stats",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(getQueueStatsController),
);
router.post(
  "/admin/queues/auto-retry",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateAutoRetryQuery),
  asyncHandler(autoRetryFailedController),
);
router.get(
  "/admin/queues/:queueName/failed",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateFailedJobsQuery),
  asyncHandler(getFailedJobsController),
);
router.post(
  "/admin/queues/:queueName/failed/retry",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateRetryFailedJobsQuery),
  asyncHandler(retryFailedJobsController),
);
router.post(
  "/admin/queues/:queueName/jobs/:jobId/retry",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(retryJobController),
);
router.delete(
  "/admin/queues/:queueName/jobs/:jobId",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(removeJobController),
);

export default router;
